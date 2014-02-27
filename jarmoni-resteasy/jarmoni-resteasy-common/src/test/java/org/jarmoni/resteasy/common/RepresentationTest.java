/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RepresentationTest {

	private final LinkFactory linkBuilder = new LinkFactory(new TestUrlResolver());

	@Test
	public void testCreateRepresentation() throws Exception {

		final Link selfLink = this.linkBuilder.createLink("self", "/self/path");
		final Link nextLink = this.linkBuilder.createLink("next", "/next/path");

		final Link item1SelfLink = this.linkBuilder.createLink("self", "/self/path/1");
		final Link item1NextLink = this.linkBuilder.createLink("next", "/next/path/1");

		final Link item2SelfLink = this.linkBuilder.createLink("self", "/self/path/2");
		final Link item2NextLink = this.linkBuilder.createLink("next", "/next/path/2");

		final Item<PersonData> item1 = new Item<PersonData>().builder().data(PersonData.builder().name("john").age(25).build())
				.addLink(item1SelfLink).addLink(item1NextLink).build();
		final Item<PersonData> item2 = new Item<PersonData>().builder().data(PersonData.builder().name("jane").age(30).build())
				.addLink(item2SelfLink).addLink(item2NextLink).build();

		final Representation<PersonData> representation = new Representation<PersonData>().builder().version("1.0.0").addLink(selfLink)
				.addLink(nextLink).addItem(item1).addItem(item2).build();

		assertEquals("1.0.0", representation.getVersion());

		assertEquals(2, representation.getLinks().size());
		assertEquals(2, representation.getItems().size());

		assertEquals("self", representation.getLinks().get(0).getRel());
		assertEquals("http://myhost:8080/self/path", representation.getLinks().get(0).getHref());
		assertEquals("next", representation.getLinks().get(1).getRel());
		assertEquals("http://myhost:8080/next/path", representation.getLinks().get(1).getHref());

		assertEquals("john", representation.getItems().get(0).getData().getName());
		assertEquals(Integer.valueOf(25), representation.getItems().get(0).getData().getAge());
		assertEquals(2, representation.getItems().get(0).getLinks().size());
		assertEquals("self", representation.getItems().get(0).getLinks().get(0).getRel());
		assertEquals("http://myhost:8080/self/path/1", representation.getItems().get(0).getLinks().get(0).getHref());
		assertEquals("next", representation.getItems().get(0).getLinks().get(1).getRel());
		assertEquals("http://myhost:8080/next/path/1", representation.getItems().get(0).getLinks().get(1).getHref());

		assertEquals("jane", representation.getItems().get(1).getData().getName());
		assertEquals(Integer.valueOf(30), representation.getItems().get(1).getData().getAge());
		assertEquals(2, representation.getItems().get(1).getLinks().size());
		assertEquals("self", representation.getItems().get(1).getLinks().get(0).getRel());
		assertEquals("http://myhost:8080/self/path/2", representation.getItems().get(1).getLinks().get(0).getHref());
		assertEquals("next", representation.getItems().get(1).getLinks().get(1).getRel());
		assertEquals("http://myhost:8080/next/path/2", representation.getItems().get(1).getLinks().get(1).getHref());
	}
}
