package com.example.jsdemo;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class JsDemoApplication {

	@GetMapping("/user")
	public Map<String, Object> user() {
		return Collections.singletonMap("name", "fred");
	}

	public static void main(String[] args) {
		SpringApplication.run(JsDemoApplication.class, args);
	}

}
