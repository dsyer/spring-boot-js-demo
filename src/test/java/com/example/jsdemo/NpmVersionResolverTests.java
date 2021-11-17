package com.example.jsdemo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class NpmVersionResolverTests {

	private NpmVersionResolver resolver = new NpmVersionResolver();

	@Test
	void resolvesModuleInWebjar() {
		ResponseEntity<Void> response = resolver.module("bootstrap");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).startsWith("/webjars");
		assertThat(location).contains("/dist/js/bootstrap.esm.js");
	}

	@Test
	void notFoundGarbagePath() {
		ResponseEntity<Void> response = resolver.module("bootstrap/garbage.js");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void notFoundInWebjar() {
		ResponseEntity<Void> response = resolver.remainder("bootstrap", "notthere.js");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void resolvesPathInWebjar() {
		ResponseEntity<Void> response = resolver.remainder("bootstrap", "/dist/css/bootstrap.min.css");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).startsWith("/webjars");
		assertThat(location).contains("/dist/css/bootstrap.min.css");
	}

}
