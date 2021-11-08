package com.example.jsdemo;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

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
	public Mono<String> greet(ServerWebExchange request) {
		return request.getFormData().map(map -> "Hello " + map.getFirst("value") + "!");
	}

	@GetMapping("/time")
	public String time() {
		return "Time: " + System.currentTimeMillis();
	}

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> stream() {
		return Flux.interval(Duration.ofSeconds(20)).map(value -> value.toString());
	}

	public static void main(String[] args) {
		SpringApplication.run(JsDemoApplication.class, args);
	}

}
