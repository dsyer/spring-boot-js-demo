package com.example.jsdemo;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@NativeHint(resources = @ResourceHint(patterns = {"^META-INF/resources/webjars/.*", "^META-INF/maven/org.webjars.npm/.*/pom.properties$"}))
public class JsDemoApplication {

	@GetMapping("/user")
	public Map<String, Object> user() {
		return Collections.singletonMap("name", "Fred");
	}

	public static void main(String[] args) {
		SpringApplication.run(JsDemoApplication.class, args);
	}

}
