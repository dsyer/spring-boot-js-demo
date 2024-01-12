package com.example.jsdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Flux;

@SpringBootApplication
@Controller
public class JsDemoApplication {

	private static int counter = 0;

	@GetMapping("/user")
	@ResponseBody
	public String user(@RequestHeader HttpHeaders headers) {
		return "<div id=\"auth\">Fred</div>";
	}

	@PostMapping("/greet")
	@ResponseBody
	public String greet(@ModelAttribute Greeting values, @RequestHeader HttpHeaders headers) {
		return "<div id=\"greeting\">Hello " + values.getValue() + "!</div>";
	}

	@GetMapping(path = "/stream")
	public Rendering stream() {
		return Rendering.view("time").modelAttribute("value", counter++)
				.modelAttribute("time", System.currentTimeMillis()).build();
	}

	@GetMapping(path = "/test")
	@ResponseBody
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