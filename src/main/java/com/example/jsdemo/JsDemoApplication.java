package com.example.jsdemo;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.samskivert.mustache.Mustache.Compiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mustache.MustacheProperties;
import org.springframework.boot.web.reactive.result.view.MustacheViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Flux;

@SpringBootApplication
@Controller
@NativeHint(resources = @ResourceHint(patterns = { "^META-INF/resources/webjars/.*",
		"^META-INF/maven/org.webjars.npm/.*/pom.properties$" }))
public class JsDemoApplication {

	private int count = 0;

	@GetMapping("/user")
	@ResponseBody
	public Map<String, Object> user() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", "Fred");
		return map;
	}

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Rendering> stream() {
		return Flux.interval(Duration.ofSeconds(5)).map(value -> Rendering.view("time").modelAttribute("value", value)
				.modelAttribute("time", System.currentTimeMillis()).build());
	}

	@PostMapping(path = "/test", produces = "text/vnd.turbo-stream.html")
	public String test(Model model) {
		model.addAttribute("value", "Test " + (count++));
		return "test";
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


	@Bean
	@ConditionalOnMissingBean
	MustacheViewResolver mustacheViewResolver(Compiler mustacheCompiler, MustacheProperties mustache) {
		MustacheViewResolver resolver = new MustacheViewResolver(mustacheCompiler);
		resolver.setPrefix(mustache.getPrefix());
		resolver.setSuffix(mustache.getSuffix());
		resolver.setViewNames(mustache.getViewNames());
		resolver.setRequestContextAttribute(mustache.getRequestContextAttribute());
		resolver.setCharset(mustache.getCharsetName());
		resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
		resolver.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_HTML, MediaType.valueOf("text/vnd.turbo-stream.html")));
		return resolver;
	}
}