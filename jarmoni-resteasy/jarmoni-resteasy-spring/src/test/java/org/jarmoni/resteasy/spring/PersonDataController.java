package org.jarmoni.resteasy.spring;

import java.util.List;

import org.jarmoni.resteasy.common.Item;
import org.jarmoni.resteasy.common.Link;
import org.jarmoni.resteasy.common.LinkFactory;
import org.jarmoni.resteasy.common.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

@RestController
public class PersonDataController {

	public static final String ROOT_PATH = "/items";
	public static final String GET_PATH = ROOT_PATH + "/get";
	public static final String ADD_PATH = ROOT_PATH + "/add";

	@Autowired
	private LinkFactory linkBuilder;

	private Representation<PersonData> representation;

	@RequestMapping(value = GET_PATH + "/{name}", method = RequestMethod.GET)
	public Representation<PersonData> get(@PathVariable final String name) {

		this.checkCreateTestData();

		Item<PersonData> currentItem = null;
		for (final Item<PersonData> i : this.representation.getItems()) {
			if (i.getData().getName().equals(name)) {
				currentItem = i;
			}
		}

		final List<Item<PersonData>> currentItems = Lists.newArrayList();
		if (currentItem != null) {
			currentItems.add(currentItem);
		}

		final List<Link> currentLinks = Lists.newArrayList();
		return new Representation<PersonData>().builder().version(this.representation.getVersion()).links(currentLinks).items(currentItems).build();
	}

	@RequestMapping(value = GET_PATH, method = RequestMethod.GET)
	public Representation<PersonData> get() {

		this.checkCreateTestData();

		return this.representation;
	}

	@RequestMapping(value = ADD_PATH, method = RequestMethod.POST)
	public Representation<PersonData> add(@RequestBody final Representation<PersonData> representation) {
		this.checkCreateTestData();

		final List<Item<PersonData>> newItems = Lists.newArrayList();
		final List<Link> newLinks = Lists.newArrayList();

		for (final Item<PersonData> current : representation.getItems()) {
			current.getLinks().clear();
			current.getLinks().add(this.linkBuilder.createLink(LinkFactory.SELF_REF, GET_PATH + "/" + current.getData().getName()));
			newItems.add(current);
		}

		this.representation.getItems().addAll(newItems);
		return new Representation<PersonData>().builder().version("1.0.0").links(newLinks).items(newItems).build();
	}

	private void checkCreateTestData() {

		if (this.representation != null) {
			return;
		}

		PersonData.builder().name("john").age(25).build();
		new Item<PersonData>().builder().addLink(this.linkBuilder.createLink(LinkFactory.SELF_REF, PersonDataController.GET_PATH + "/john"))
				.data(PersonData.builder().name("john").age(25).build()).build();

		this.representation = new Representation<PersonData>()
				.builder()
				.version("1.0.0")
				.addLink(this.linkBuilder.createLink(LinkFactory.SELF_REF, PersonDataController.GET_PATH))
				.addItem(
						new Item<PersonData>().builder()
								.addLink(this.linkBuilder.createLink(LinkFactory.SELF_REF, PersonDataController.GET_PATH + "/john"))
								.data(PersonData.builder().name("john").age(25).build()).build())
				.addItem(
						new Item<PersonData>().builder()
								.addLink(this.linkBuilder.createLink(LinkFactory.SELF_REF, PersonDataController.GET_PATH + "/jane"))
								.data(PersonData.builder().name("jane").age(30).build()).build()).build();
	}
}
