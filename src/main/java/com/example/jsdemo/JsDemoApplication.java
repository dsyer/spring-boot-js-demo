package com.example.jsdemo;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.result.method.InvocableHandlerMethod;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.reactive.result.view.View;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@NativeHint(resources = @ResourceHint(patterns = { "^META-INF/resources/webjars/.*",
		"^META-INF/maven/org.webjars.npm/.*/pom.properties$" }))
public class JsDemoApplication {

	@GetMapping("/user")
	public String user() {
		return "Fred";
	}

	@PostMapping("/greet")
	public String greet(@ModelAttribute Greeting values) {
		return "Hello " + values.getValue() + "!";
	}

	@GetMapping("/time")
	public String time() {
		return "Time: " + System.currentTimeMillis();
	}

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> stream() {
		return Flux.interval(Duration.ofSeconds(20)).map(value -> value.toString());
	}

	@GetMapping(path = "/test")
	public Flux<Rendering> test() {
		return Flux.just(Rendering.view("test").modelAttribute("value", "Test").build(),
				Rendering.view("test").modelAttribute("value", "Foo").build());
	}

	public static void main(String[] args) {
		SpringApplication.run(JsDemoApplication.class, args);
	}

	static class Greeting {
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CompositeViewRenderer implements HandlerResultHandler {

	private final ViewResolver resolver;

	public CompositeViewRenderer(ViewResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public boolean supports(HandlerResult result) {
		if (Publisher.class.isAssignableFrom(result.getReturnType().toClass())) {
			if (Rendering.class.isAssignableFrom(result.getReturnType().getGeneric(0).toClass())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
		String[] methodAnnotation = ((InvocableHandlerMethod) result.getHandler())
				.getMethodAnnotation(RequestMapping.class).produces();
		MediaType type = methodAnnotation.length > 0 ? MediaType.valueOf(methodAnnotation[0]) : MediaType.TEXT_HTML;
		exchange.getResponse().getHeaders().setContentType(type);
		@SuppressWarnings("unchecked")
		Flux<Rendering> renderings = Flux.from((Publisher<Rendering>) result.getReturnValue());
		final ServerWebExchange wrapper = new ExchangeWrapper(exchange);
		return render(wrapper, renderings).then(Mono.defer(
				() -> exchange.getResponse().writeAndFlushWith(((ResponseWrapper) wrapper.getResponse()).getBody())));
	}

	private Mono<Void> render(ServerWebExchange exchange, Flux<Rendering> renderings) {
		return renderings.map(rendering -> render(exchange, rendering))
				.flatMap(thing -> thing.concatWith(closer(exchange))).then();
	}

	private Mono<Void> closer(ServerWebExchange exchange) {
		DataBuffer buffer = exchange.getResponse().bufferFactory().allocateBuffer();
		buffer.write("\n\n", StandardCharsets.UTF_8);
		return exchange.getResponse().writeWith(Mono.just(buffer));
	}

	private Mono<Void> render(ServerWebExchange exchange, Rendering rendering) {
		Mono<View> view = null;
		if (rendering.view() instanceof View) {
			view = Mono.just((View) rendering.view());
		} else {
			view = resolver.resolveViewName((String) rendering.view(), exchange.getLocaleContext().getLocale());
		}
		return view.flatMap(actual -> actual.render(rendering.modelAttributes(), null, exchange));
	}

	static class ExchangeWrapper extends ServerWebExchangeDecorator {

		private ServerHttpResponse response;

		protected ExchangeWrapper(ServerWebExchange delegate) {
			super(delegate);
			this.response = new ResponseWrapper(super.getResponse());
		}

		@Override
		public ServerHttpResponse getResponse() {
			return this.response;
		}

	}

	static class ResponseWrapper extends ServerHttpResponseDecorator {

		private Flux<Flux<DataBuffer>> body = Flux.empty();

		public Flux<Flux<DataBuffer>> getBody() {
			return body;
		}

		public ResponseWrapper(ServerHttpResponse delegate) {
			super(delegate);
		}

		@Override
		public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
			return writeAndFlushWith(Mono.just(body));
		}

		@Override
		public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
			Flux<Flux<DataBuffer>> map = Flux.from(body).map(publisher -> Flux.from(publisher));
			this.body = this.body.concatWith(map);
			return Mono.empty();
		}

	}

}