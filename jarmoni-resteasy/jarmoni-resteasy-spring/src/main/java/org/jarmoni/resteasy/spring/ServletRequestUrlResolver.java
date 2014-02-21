/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.spring;

import javax.servlet.http.HttpServletRequest;

import org.jarmoni.resteasy.common.IUrlResolver;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ServletRequestUrlResolver implements IUrlResolver {

	@Override
	public String getRootUrl() {

		final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		final HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		final String scheme = servletRequest.getScheme();
		final int port = servletRequest.getServerPort();
		final String host = servletRequest.getServerName();
		return new StringBuilder().append(scheme).append("://").append(host).append(":").append(port).toString();
	}

}
