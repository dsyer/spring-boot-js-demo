package com.example.jsdemo;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@SpringBootApplication
@RestController
@NativeHint(resources = @ResourceHint(patterns = { "^META-INF/resources/webjars/.*",
		"^META-INF/maven/org.webjars.npm/.*/pom.properties$" }))
public class JsDemoApplication {

	@GetMapping("/user")
	public Map<String, Object> user() {
		return Collections.singletonMap("name", "Fred");
	}

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> stream() {
		return Flux.interval(Duration.ofSeconds(2)).map(value -> event(value));
	}

	private String event(long value) {
		return "<turbo-stream action=\"append\" target=\"load\"><template><div>" //
				+ "Index: " + value + " Time: " + System.currentTimeMillis() //
				+ "</div></temlate></turbo-stream>";
	}

	public static void main(String[] args) {
		SpringApplication.run(JsDemoApplication.class, args);
	}

}
