/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.common;

import com.google.common.base.Objects;

public class Link {

	private String rel;
	private String href;

	public Link() {
	}

	public Link(final String rel, final String href) {
		super();
		this.rel = rel;
		this.href = href;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(final String rel) {
		this.rel = rel;
	}

	public String getHref() {
		return href;
	}

	public void setHref(final String href) {
		this.href = href;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(Link.class).add("rel", this.rel).add("href", this.href).toString();
	}

}
