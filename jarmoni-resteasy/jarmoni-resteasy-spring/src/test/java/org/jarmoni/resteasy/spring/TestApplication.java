package org.jarmoni.resteasy.spring;

import org.jarmoni.resteasy.common.IUrlResolver;
import org.jarmoni.resteasy.common.LinkFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@EnableAutoConfiguration
@Configuration
public class TestApplication {

	@Bean
	public IUrlResolver urlResolver() {
		return new ServletRequestUrlResolver();
	}

	@Bean
	public LinkFactory linkBuilder() {
		return new LinkFactory(this.urlResolver());
	}

	public static void main(final String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}
}
