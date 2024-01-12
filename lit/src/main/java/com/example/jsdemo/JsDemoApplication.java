package com.example.jsdemo;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Flux;

@SpringBootApplication
@RestController
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

	@GetMapping(path = "/test")
	public Map<String, Object> test() {
		return Map.of("hello", "Hello", "world", "World");
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