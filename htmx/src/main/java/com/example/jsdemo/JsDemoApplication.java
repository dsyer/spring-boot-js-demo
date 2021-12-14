package com.example.jsdemo;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Flux;

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