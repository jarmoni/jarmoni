/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RepresentationTest {

	private final LinkBuilder linkBuilder = new LinkBuilder(new TestUrlResolver());

	@Test
	public void testCreateRepresentation() throws Exception {

		final Link selfLink = this.linkBuilder.createLink("self", "/self/path");
		final Link nextLink = this.linkBuilder.createLink("next", "/next/path");

		final Link item1SelfLink = this.linkBuilder.createLink("self", "/self/path/1");
		final Link item1NextLink = this.linkBuilder.createLink("next", "/next/path/1");

		final Link item2SelfLink = this.linkBuilder.createLink("self", "/self/path/2");
		final Link item2NextLink = this.linkBuilder.createLink("next", "/next/path/2");

		final TestItem item1 = new TestItem(Lists.newArrayList(item1SelfLink, item1NextLink), "john", 25);
		final TestItem item2 = new TestItem(Lists.newArrayList(item2SelfLink, item2NextLink), "jane", 30);

		final Representation<TestItem> representation = new Representation<TestItem>().builder().addLink(selfLink).addLink(nextLink).addItem(item1)
				.addItem(item2).build();

		assertEquals(2, representation.getLinks().size());
		assertEquals(2, representation.getItems().size());

		assertEquals("self", representation.getLinks().get(0).getRel());
		assertEquals("http://myhost:8080/self/path", representation.getLinks().get(0).getHref());
		assertEquals("next", representation.getLinks().get(1).getRel());
		assertEquals("http://myhost:8080/next/path", representation.getLinks().get(1).getHref());

		assertEquals("john", representation.getItems().get(0).getName());
		assertEquals(Integer.valueOf(25), representation.getItems().get(0).getAge());
		assertEquals(2, representation.getItems().get(0).getLinks().size());
		assertEquals("self", representation.getItems().get(0).getLinks().get(0).getRel());
		assertEquals("http://myhost:8080/self/path/1", representation.getItems().get(0).getLinks().get(0).getHref());
		assertEquals("next", representation.getItems().get(0).getLinks().get(1).getRel());
		assertEquals("http://myhost:8080/next/path/1", representation.getItems().get(0).getLinks().get(1).getHref());

		assertEquals("jane", representation.getItems().get(1).getName());
		assertEquals(Integer.valueOf(30), representation.getItems().get(1).getAge());
		assertEquals(2, representation.getItems().get(1).getLinks().size());
		assertEquals("self", representation.getItems().get(1).getLinks().get(0).getRel());
		assertEquals("http://myhost:8080/self/path/2", representation.getItems().get(1).getLinks().get(0).getHref());
		assertEquals("next", representation.getItems().get(1).getLinks().get(1).getRel());
		assertEquals("http://myhost:8080/next/path/2", representation.getItems().get(1).getLinks().get(1).getHref());
	}
}
