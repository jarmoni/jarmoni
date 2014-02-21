/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.common;

import static org.jarmoni.util.Asserts.notNullSimple;

/**
 * @author ms Creates link-instances
 * 
 */
public final class LinkBuilder {

	public static final String SELF_REF = "self";

	private final IUrlResolver urlResolver;

	public LinkBuilder(final IUrlResolver urlResolver) {
		this.urlResolver = notNullSimple(urlResolver, "urlResolver");
	}

	/**
	 * @param rel type of relation (self, next,...)
	 * @param relativeUrl relative part of url starting with '/' (/my/path,...)
	 * @return
	 */
	public Link createLink(final String rel, final String relativeUrl) {

		return new Link(rel, this.urlResolver.getRootUrl() + relativeUrl);
	}

}
