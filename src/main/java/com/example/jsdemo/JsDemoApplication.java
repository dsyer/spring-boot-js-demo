package com.example.jsdemo;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.ResourceHandlerRegistrationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

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

	@Bean
	public WebFluxConfigurer configurer() {
		return new WebFluxConfigurer() {
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				registry.addResourceHandler("/npm/**").addResourceLocations("classpath:META-INF/resources/webjars/").resourceChain(true).addResolver(new NpmResourceResolver());
			}
		};
	}

}
