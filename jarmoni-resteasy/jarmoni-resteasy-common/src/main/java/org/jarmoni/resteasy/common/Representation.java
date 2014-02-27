/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.common;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class Representation<T> {

	private String version;

	private List<Link> links = Lists.newArrayList();

	private List<Item<T>> items = Lists.newArrayList();

	public Representation() {
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(final List<Link> links) {
		this.links = links;
	}

	public List<Item<T>> getItems() {
		return items;
	}

	public void setItems(final List<Item<T>> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(Representation.class).add("links", this.links).add("items", this.items).toString();
	}

	public RepresentationBuilder<T> builder() {
		return new RepresentationBuilder<>();
	}

	public static final class RepresentationBuilder<T> {

		private final Representation<T> representation;

		private RepresentationBuilder() {
			this.representation = new Representation<>();
		}

		public Representation<T> build() {
			return this.representation;
		}

		public RepresentationBuilder<T> version(final String version) {
			this.representation.version = version;
			return this;
		}

		public RepresentationBuilder<T> links(final List<Link> links) {
			this.representation.setLinks(links);
			return this;
		}

		public RepresentationBuilder<T> addLink(final Link link) {
			this.representation.links.add(link);
			return this;
		}

		public RepresentationBuilder<T> items(final List<Item<T>> items) {
			this.representation.setItems(items);
			return this;
		}

		public RepresentationBuilder<T> addItem(final Item<T> item) {
			this.representation.items.add(item);
			return this;
		}
	}
}
