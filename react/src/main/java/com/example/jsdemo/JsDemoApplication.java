package com.example.jsdemo;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

	@GetMapping("/pops")
	@ResponseBody
	public Mono<Chart> bar() {
		return Mono.just(new Chart());
	}

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Rendering> stream() {
		return Flux.interval(Duration.ofSeconds(5)).map(value -> Rendering.view("time").modelAttribute("value", value)
				.modelAttribute("time", System.currentTimeMillis()).build());
	}

	@GetMapping(path = "/test")
	public Flux<Rendering> test() {
		return Flux.just(Rendering.view("test").modelAttribute("id", "hello").modelAttribute("value", "Hello").build(),
				Rendering.view("test").modelAttribute("id", "world").modelAttribute("value", "World").build());
	}

	public static void main(String[] args) {
		SpringApplication.run(JsDemoApplication.class, args);
	}

	public static class Chart {
		public Data data = new Data();
		public Options options = new Options();

		public static class Data {
			public List<String> labels = Arrays.asList("Africa", "Asia", "Europe", "Latin America", "North America");
			public List<Map<String, Object>> datasets = Arrays.asList(Map.of("label", "Population (millions)", //
					"backgroundColor", Arrays.asList("#3e95cd", "#8e5ea2", "#3cba9f", "#e8c3b9", "#c45850"), //
					"data", Arrays.asList(2478, 5267, 734, 784, 433)));
		};

		public static class Options {
			public Map<String, Object> plugins = new HashMap<>();
			{
				plugins.put("legend", Map.of("display", false));
				plugins.put("title", Map.of("display", true, "text", "Predicted world population (millions) in 2050"));
			}
		};
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
	public WebFluxConfigurer configurer() {
		return new WebFluxConfigurer() {
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				registry.addResourceHandler("/node_modules/**").addResourceLocations("file:node_modules/");
			}
		};
	}

}