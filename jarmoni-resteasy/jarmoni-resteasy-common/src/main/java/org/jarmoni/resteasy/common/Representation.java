/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.common;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class Representation<T extends AbstractItem> {

	private List<Link> links = Lists.newArrayList();

	private List<T> items = Lists.newArrayList();

	public Representation() {
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(final List<Link> links) {
		this.links = links;
	}

	public List<T> getItems() {
		return items;
	}

	public void setItems(final List<T> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(Representation.class).add("links", this.links).add("items", this.items).toString();
	}

	public RepresentationBuilder<T> builder() {
		return new RepresentationBuilder<>();
	}

	public static final class RepresentationBuilder<T extends AbstractItem> {

		private final Representation<T> representation;

		private RepresentationBuilder() {
			this.representation = new Representation<>();
		}

		public Representation<T> build() {
			return this.representation;
		}

		public RepresentationBuilder<T> links(final List<Link> links) {
			this.representation.setLinks(links);
			return this;
		}

		public RepresentationBuilder<T> addLink(final Link link) {
			this.representation.links.add(link);
			return this;
		}

		public RepresentationBuilder<T> items(final List<T> items) {
			this.representation.setItems(items);
			return this;
		}

		public RepresentationBuilder<T> addItem(final T item) {
			this.representation.items.add(item);
			return this;
		}
	}
}
