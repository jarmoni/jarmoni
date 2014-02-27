/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 27, 2014
 */
package org.jarmoni.resteasy.common;

import java.util.List;

import com.google.common.collect.Lists;

public class Item<T> {

	private List<Link> links = Lists.newArrayList();

	private T data;

	public Item() {
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(final List<Link> links) {
		this.links = links;
	}

	public T getData() {
		return data;
	}

	public void setData(final T data) {
		this.data = data;
	}

	public ItemBuilder<T> builder() {
		return new ItemBuilder<>();
	}

	public static final class ItemBuilder<T> {

		private final Item<T> item;

		private ItemBuilder() {
			this.item = new Item<>();
		}

		public ItemBuilder<T> links(final List<Link> links) {
			this.item.setLinks(links);
			return this;
		}

		public ItemBuilder<T> addLink(final Link link) {
			this.item.links.add(link);
			return this;
		}

		public ItemBuilder<T> data(final T data) {
			this.item.setData(data);
			return this;
		}

		public Item<T> build() {
			return this.item;
		}
	}

}
