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
		assertThat(location).matches("/webjars/bootstrap/[0-9\\.]*/.*");
		assertThat(location).contains("/dist/js/bootstrap.esm.js");
	}

	@Test
	void resolvesVersionInWebjar() {
		ResponseEntity<Void> response = resolver.module("bootstrap@5");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).startsWith("/webjars");
	}

	@Test
	void notFoundInWebjar() {
		ResponseEntity<Void> response = resolver.remainder("bootstrap", "/notthere.js");
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

	@Test
	void resolvesAtModule() {
		ResponseEntity<Void> response = resolver.remainder("@popperjs", "/core");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).startsWith("/webjars");
		assertThat(location).contains("/popperjs__core");
		assertThat(location).contains("/lib/index.js");
		assertThat(location).doesNotContain("//");
	}

	@Test
	void resolvesAtModuleSubPath() {
		ResponseEntity<Void> response = resolver.remainder("@popperjs", "/core/lib/index.js");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).startsWith("/webjars");
		assertThat(location).contains("/popperjs__core");
		assertThat(location).contains("/lib/index.js");
		assertThat(location).doesNotContain("//");
	}

	@Test
	void resolvesUnpkgModuleWhenWebjarNotAvailable() {
		ResponseEntity<Void> response = resolver.module("jquery");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).isEqualTo("https://unpkg.com/jquery");
	}

	@Test
	void resolvesUnpkgAtModuleWhenWebjarNotAvailable() {
		ResponseEntity<Void> response = resolver.module("@hotwired/stimulus");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).isEqualTo("https://unpkg.com/@hotwired/stimulus");
	}

	@Test
	void resolvesUnpkgPathWhenWebjarNotAvailable() {
		ResponseEntity<Void> response = resolver.module("jquery/index.js");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).isEqualTo("https://unpkg.com/jquery/index.js");
	}

	@Test
	void resolvesUnpkgVersionWhenWebjarNotAvailable() {
		ResponseEntity<Void> response = resolver.module("jquery@2");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).isEqualTo("https://unpkg.com/jquery@2");
	}

	@Test
	void resolvesUnpkgVersionAndPathWhenWebjarNotAvailable() {
		ResponseEntity<Void> response = resolver.module("jquery@2/jquery/index.js");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		String location = response.getHeaders().getFirst("location");
		assertThat(location).isEqualTo("https://unpkg.com/jquery@2/jquery/index.js");
	}

}
