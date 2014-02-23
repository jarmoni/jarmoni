/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 23, 2014
 */
package org.jarmoni.resteasy.spring;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jarmoni.resteasy.common.Representation;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public final class RestTemplateFactory {

	private RestTemplateFactory() {
	}

	public static RestTemplate createTemplate(final Class<?> paramClass) {
		final RestTemplate restTemplate = new RestTemplate();
		final HttpMessageConverter<?> messageConverter = new MappingJackson2HttpMessageConverter() {
			@Override
			protected JavaType getJavaType(final Type type, final Class<?> contextClass) {
				return TypeFactory.defaultInstance().constructParametricType(Representation.class, paramClass);
			}
		};
		final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(messageConverter);
		restTemplate.setMessageConverters(messageConverters);
		return restTemplate;
	}

}
