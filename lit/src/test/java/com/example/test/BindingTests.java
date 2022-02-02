package com.example.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;

public class BindingTests {
	@Test
	public void test() {
		MyBean bean = new SpringApplicationBuilder(MyConfiguration.class).web(WebApplicationType.NONE).run().getBean(MyBean.class);
		assertThat(bean.getValue()).endsWith(".config").doesNotContain("$");
	}

	@Test
	public void bind() {
		MyBean bean = new SpringApplicationBuilder(MyConfiguration.class).web(WebApplicationType.NONE).run("--my.value=foo").getBean(MyBean.class);
		assertThat(bean.getValue()).endsWith("foo");
	}
}

@Configuration
@EnableConfigurationProperties(MyBean.class)
class MyConfiguration {
}

@ConfigurationProperties("my")
@ConstructorBinding
class MyBean {
	private String value;

	MyBean(@DefaultValue("${user.home}/.config") String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}