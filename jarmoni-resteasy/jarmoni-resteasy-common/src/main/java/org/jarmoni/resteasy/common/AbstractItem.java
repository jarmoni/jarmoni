/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.common;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public abstract class AbstractItem {

	private List<Link> links = Lists.newArrayList();

	public AbstractItem() {
	}

	public AbstractItem(final List<Link> links) {
		super();
		this.links = links;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(final List<Link> links) {
		this.links = links;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(AbstractItem.class).add("links", this.links).toString();
	}

}
