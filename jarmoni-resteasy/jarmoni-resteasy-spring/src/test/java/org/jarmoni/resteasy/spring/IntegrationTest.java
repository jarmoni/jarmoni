/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.spring;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.jarmoni.resteasy.common.Item;
import org.jarmoni.resteasy.common.Representation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;

public class IntegrationTest {

	private final RestTemplate restTemplate = RestTemplateFactory.createTemplate(PersonData.class);
	private ConfigurableApplicationContext context;

	@Before
	public void setUp() throws Exception {
		this.context = SpringApplication.run(TestApplication.class, new String[0]);
	}

	@After
	public void tearDown() throws Exception {
		SpringApplication.exit(this.context, new ExitCodeGenerator() {

			@Override
			public int getExitCode() {
				return 0;
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetAll() throws Exception {

		final Representation<PersonData> rep = this.restTemplate.getForEntity(new URI("http://localhost:8080/items/get"), Representation.class)
				.getBody();

		assertEquals(1, rep.getLinks().size());
		assertEquals("self", rep.getLinks().get(0).getRel());
		assertEquals("http://localhost:8080/items/get", rep.getLinks().get(0).getHref());

		assertEquals(2, rep.getItems().size());

		assertEquals(1, rep.getItems().get(0).getLinks().size());
		assertEquals("self", rep.getItems().get(0).getLinks().get(0).getRel());
		assertEquals("http://localhost:8080/items/get/john", rep.getItems().get(0).getLinks().get(0).getHref());
		assertEquals("john", rep.getItems().get(0).getData().getName());
		assertEquals(Integer.valueOf(25), rep.getItems().get(0).getData().getAge());

		assertEquals(1, rep.getItems().get(1).getLinks().size());
		assertEquals("self", rep.getItems().get(1).getLinks().get(0).getRel());
		assertEquals("http://localhost:8080/items/get/jane", rep.getItems().get(1).getLinks().get(0).getHref());
		assertEquals("jane", rep.getItems().get(1).getData().getName());
		assertEquals(Integer.valueOf(30), rep.getItems().get(1).getData().getAge());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGet() throws Exception {

		final Representation<PersonData> rep = this.restTemplate.getForEntity(new URI("http://localhost:8080/items/get/john"), Representation.class)
				.getBody();

		assertEquals("1.0.0", rep.getVersion());

		assertEquals(0, rep.getLinks().size());

		assertEquals(1, rep.getItems().size());

		assertEquals(1, rep.getItems().get(0).getLinks().size());
		assertEquals("self", rep.getItems().get(0).getLinks().get(0).getRel());
		assertEquals("http://localhost:8080/items/get/john", rep.getItems().get(0).getLinks().get(0).getHref());
		assertEquals("john", rep.getItems().get(0).getData().getName());
		assertEquals(Integer.valueOf(25), rep.getItems().get(0).getData().getAge());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAdd() throws Exception {

		new Item<PersonData>().builder().data(PersonData.builder().name("doe").age(35).build());

		final Representation<PersonData> newRep = new Representation<PersonData>().builder()
				.addItem(new Item<PersonData>().builder().data(PersonData.builder().name("doe").age(35).build()).build()).build();

		final Representation<PersonData> rep = this.restTemplate.postForEntity(new URI("http://localhost:8080/items/add"), newRep,
				Representation.class).getBody();

		assertEquals(0, rep.getLinks().size());

		assertEquals(1, rep.getItems().size());

		assertEquals(1, rep.getItems().get(0).getLinks().size());
		assertEquals("self", rep.getItems().get(0).getLinks().get(0).getRel());
		assertEquals("http://localhost:8080/items/get/doe", rep.getItems().get(0).getLinks().get(0).getHref());
		assertEquals("doe", rep.getItems().get(0).getData().getName());
		assertEquals(Integer.valueOf(35), rep.getItems().get(0).getData().getAge());

		final Representation<PersonData> rep2 = this.restTemplate.getForEntity(new URI("http://localhost:8080/items/get"), Representation.class)
				.getBody();
		assertEquals(3, rep2.getItems().size());
	}
}
