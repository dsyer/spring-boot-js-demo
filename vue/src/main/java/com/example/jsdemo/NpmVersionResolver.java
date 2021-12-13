/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jsdemo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * A {@code Controller} that redirects to a more precise webjars path that can
 * be handled by the resource resolver.
 */
@RestController
public class NpmVersionResolver {

	private static final Log logger = LogFactory.getLog(NpmVersionResolver.class);

	private static final Set<String> ALERTS = new HashSet<>();

	private static final String PROPERTIES_ROOT = "META-INF/maven/";
	private static final String RESOURCE_ROOT = "META-INF/resources/webjars/";
	private static final String NPM = "org.webjars.npm/";
	private static final String PLAIN = "org.webjars/";
	private static final String POM_PROPERTIES = "/pom.properties";
	private static final String PACKAGE_JSON = "/package.json";

	@GetMapping("/npm/{webjar}")
	public ResponseEntity<Void> module(@PathVariable String webjar) {
		String spec = null;
		int index = webjar.indexOf("@");
		if (index>0) {
			if (index < webjar.length()) {
				spec = webjar.substring(index + 1);
			}
			webjar = webjar.substring(0, index);
		}
		String path = findWebJarResourcePath(webjar, spec, "/");
		if (path == null) {
			path = findUnpkgPath(webjar, spec, "");
			return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(path)).build();
		}
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/webjars/" + path)).build();
	}

	@GetMapping("/npm/{webjar}/{*remainder}")
	public ResponseEntity<Void> remainder(@PathVariable String webjar, @PathVariable String remainder) {
		String spec = null;
		int index = webjar.indexOf("@");
		if (index>0) {
			if (index < webjar.length()) {
				spec = webjar.substring(index + 1);
			}
			webjar = webjar.substring(0, index);
		}
		if (webjar.startsWith("@")) {
			int slash = remainder.indexOf("/",1);
			String path = slash < 0 ? remainder.substring(1) : remainder.substring(1, slash);
			webjar = webjar.substring(1) + "__" + path;
			if (slash < 0 || slash == remainder.length() - 1) {
				return module(webjar);
			}
			remainder = remainder.substring(slash);
		}
		String path = findWebJarResourcePath(webjar, spec, remainder);
		if (path == null) {
			if (version(webjar, spec) == null) {
				path = findUnpkgPath(webjar, spec, remainder);
			} else {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(path)).build();
		}
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/webjars/" + path)).build();
	}

	private String findUnpkgPath(String webjar, String spec, String remainder) {
		if (!StringUtils.hasText(remainder)) {
			remainder = "";
		} else if (!remainder.startsWith("/")) {
			remainder = "/" + remainder;
		}
		if (webjar.contains("__")) {
			webjar = "@" + webjar.replace("__", "/");
		}
		if (spec != null) {
			webjar = webjar + "@" + spec;
		}
		String local = findLocalPath(webjar+remainder);
		if (local != null) {
			
		}
		if (logger.isInfoEnabled() && !ALERTS.contains(webjar)) {
			ALERTS.add(webjar);
			logger.info("Resolving webjar to unpkg.com: " + webjar);
		}
		return "https://unpkg.com/" + webjar + remainder;
	}

	private String findLocalPath(String path) {
		File module = new File("node_modules", path);
		if (module.exists() && module.isDirectory()) {
			return "/" + module.getPath();
		}
		return null;
	}

	@Nullable
	protected String findWebJarResourcePath(String webjar, String spec, String path) {
		if (webjar.length() > 0) {
			String version = version(webjar, spec);
			if (version != null) {
				String partialPath = path(webjar, version, path);
				if (partialPath != null) {
					String webJarPath = webjar + "/" + version + partialPath;
					return webJarPath;
				}
			}
		}
		return null;
	}

	private String path(String webjar, String version, String path) {
		if (path.equals("/")) {
			String module = module(webjar, version, path);
			if (module != null) {
				return module;
			} else {
				return null;
			}
		}
		if (path.equals("/main.js")) {
			String module = module(webjar, version, path);
			if (module != null) {
				return module;
			}
		}
		if (new ClassPathResource(RESOURCE_ROOT + webjar + "/" + version + path).isReadable()) {
			return path;
		}
		return null;
	}

	private String module(String webjar, String version, String path) {
		Resource resource = new ClassPathResource(RESOURCE_ROOT + webjar + "/" + version + PACKAGE_JSON);
		if (resource.isReadable()) {
			try {
				JsonParser parser = JsonParserFactory.getJsonParser();
				Map<String, Object> map = parser
						.parseMap(StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8));
				if (!path.equals("/main.js") && map.containsKey("module")) {
					return "/" + (String) map.get("module");
				}
				if (!map.containsKey("main") && map.containsKey("jspm")) {
					String stem = resolve(map, "jspm.directories.lib", "dist");
					String main = resolve(map, "jspm.main", "index.js");
					return "/" + stem + "/" + main + (main.endsWith(".js") ? "" : ".js");
				}
				return "/" + (String) map.get("main");
			} catch (IOException e) {
			}
		}
		return null;
	}

	private static String resolve(Map<String, Object> map, String path, String defaultValue) {
		Map<String, Object> sub = map;
		String[] elements = StringUtils.delimitedListToStringArray(path, ".");
		for (int i = 0; i < elements.length - 1; i++) {
			@SuppressWarnings("unchecked")
			Map<String, Object> tmp = (Map<String, Object>) sub.get(elements[i]);
			sub = tmp;
			if (sub == null) {
				return defaultValue;
			}
		}
		return (String) sub.getOrDefault(elements[elements.length - 1], defaultValue);
	}

	private String version(String webjar, String spec) {
		Resource resource = new ClassPathResource(PROPERTIES_ROOT + NPM + webjar + POM_PROPERTIES);
		if (!resource.isReadable()) {
			resource = new ClassPathResource(PROPERTIES_ROOT + PLAIN + webjar + POM_PROPERTIES);
		}
		if (resource.isReadable()) {
			Properties properties;
			try {
				properties = PropertiesLoaderUtils.loadProperties(resource);
				String value = properties.getProperty("version");
				if (spec==null || value.startsWith(spec)) {
					return value;
				}
			} catch (IOException e) {
			}
		}
		return null;
	}

}
