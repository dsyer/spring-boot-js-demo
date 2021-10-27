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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import reactor.core.publisher.Mono;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.resource.AbstractResourceResolver;
import org.springframework.web.reactive.resource.ResourceResolverChain;
import org.springframework.web.server.ServerWebExchange;

/**
 * A {@code ResourceResolver} that delegates to the chain to locate a resource
 * and then attempts to find a matching versioned resource contained in a WebJar
 * JAR file.
 *
 * <p>
 * This allows WebJars.org users to write version agnostic paths in their
 * templates, like {@code <script src="/jquery/jquery.min.js"/>}. This path will
 * be resolved to the unique version
 * {@code <script src="/jquery/1.2.0/jquery.min.js"/>}, which is a better fit
 * for HTTP caching and version management in applications.
 *
 * <p>
 * This also resolves resources for version agnostic HTTP requests
 * {@code "GET /jquery/jquery.min.js"}.
 *
 * <p>
 * This resolver requires the {@code org.webjars:webjars-locator-core} library
 * on the classpath and is automatically registered if that library is present.
 *
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @since 5.0
 * @see <a href="https://www.webjars.org">webjars.org</a>
 */
public class NpmResourceResolver extends AbstractResourceResolver {

	private static final String PROPERTIES_ROOT = "META-INF/maven/";
	private static final String RESOURCE_ROOT = "META-INF/resources/webjars/";
	private static final String NPM = "org.webjars.npm/";
	private static final String PLAIN = "org.webjars/";
	private static final String POM_PROPERTIES = "/pom.properties";
	private static final String PACKAGE_JSON = "/package.json";

	@Override
	protected Mono<Resource> resolveResourceInternal(@Nullable ServerWebExchange exchange, String requestPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {

		return chain.resolveResource(exchange, requestPath, locations).switchIfEmpty(Mono.defer(() -> {
			String webJarsResourcePath = findWebJarResourcePath(requestPath);
			if (webJarsResourcePath != null) {
				return chain.resolveResource(exchange, webJarsResourcePath, locations);
			} else {
				return Mono.empty();
			}
		}));
	}

	@Override
	protected Mono<String> resolveUrlPathInternal(String resourceUrlPath, List<? extends Resource> locations,
			ResourceResolverChain chain) {

		return chain.resolveUrlPath(resourceUrlPath, locations).switchIfEmpty(Mono.defer(() -> {
			String webJarResourcePath = findWebJarResourcePath(resourceUrlPath);
			if (webJarResourcePath != null) {
				return chain.resolveUrlPath(webJarResourcePath, locations);
			} else {
				return Mono.empty();
			}
		}));
	}

	@Nullable
	protected String findWebJarResourcePath(String path) {
		int startOffset = (path.startsWith("/") ? 1 : 0);
		int endOffset = path.indexOf('/', 1);
		String webjar = endOffset != -1 ? 
			path.substring(startOffset, endOffset) : path;
		if (endOffset == -1) {
			path = "module.js";
		}
		if (webjar.length()>0) {	
			String version = version(webjar);
			if (version != null) {
				String partialPath = path(webjar, version, path.substring(endOffset + 1));
				if (partialPath != null) {
					String webJarPath = webjar + File.separator + version + File.separator + partialPath;
					return webJarPath;
				}
			}
		}
		return null;
	}

	private String path(String webjar, String version, String path) {
		if (path.endsWith("module.js") || path.endsWith("main.js")) {
			Resource resource = new ClassPathResource(RESOURCE_ROOT + webjar + File.separator + version + PACKAGE_JSON);
			if (resource.isReadable()) {
				try {
					JsonParser parser = JsonParserFactory.getJsonParser();
					Map<String, Object> map = parser
							.parseMap(StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8));
					if (!path.endsWith("main.js") && map.containsKey("module")) {
						return (String) map.get("module");
					}
					if (!map.containsKey("main") && map.containsKey("jspm")) {
						String stem = resolve(map, "jspm.directories.lib", "dist");
						String main = resolve(map, "jspm.main", "dist");
						return stem + File.separator + main + (main.endsWith(".js") ? "" : ".js");
					}
					return (String) map.get("main");
				} catch (IOException e) {
				}
			}
		}
		if (new ClassPathResource(RESOURCE_ROOT + webjar + File.separator + version + File.separator + path).isReadable()) {
			return path;
		}
		return null;
	}

	private static String resolve(Map<String,Object> map, String path, String defaultValue) {
		Map<String,Object> sub = map;
		String[] elements = StringUtils.delimitedListToStringArray(path, ".");
		for (int i=0; i<elements.length-1; i++) {
			@SuppressWarnings("unchecked")
			Map<String,Object> tmp = (Map<String, Object>) sub.get(elements[i]);
			sub = tmp;
			if (sub == null) {
				return defaultValue;
			}
		}
		return (String) sub.getOrDefault(elements[elements.length-1], defaultValue);
	}

	private String version(String webjar) {
		Resource resource = new ClassPathResource(PROPERTIES_ROOT + NPM + webjar + POM_PROPERTIES);
		if (!resource.isReadable()) {
			resource = new ClassPathResource(PROPERTIES_ROOT + PLAIN + webjar + POM_PROPERTIES);
		}
		if (resource.isReadable()) {
			Properties properties;
			try {
				properties = PropertiesLoaderUtils.loadProperties(resource);
				return properties.getProperty("version");
			} catch (IOException e) {
			}
		}
		return null;
	}

}
