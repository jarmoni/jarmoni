package org.jarmoni.resteasy.spring;

import java.util.List;

import org.jarmoni.resteasy.common.Link;
import org.jarmoni.resteasy.common.LinkBuilder;
import org.jarmoni.resteasy.common.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

@RestController
@RequestMapping
public class TestItemController {

	public static final String ROOT_PATH = "/items";
	public static final String GET_PATH = ROOT_PATH + "/get";
	public static final String ADD_PATH = ROOT_PATH + "/add";

	@Autowired
	private LinkBuilder linkBuilder;

	private Representation<TestItem> representation;

	@RequestMapping(value = GET_PATH + "/{name}", method = RequestMethod.GET)
	public Representation<TestItem> get(@PathVariable final String name) {

		this.checkCreateTestData();

		TestItem currentItem = null;
		for (final TestItem i : this.representation.getItems()) {
			if (i.getName().equals(name)) {
				currentItem = i;
			}
		}

		final List<TestItem> currentItems = Lists.newArrayList();
		if (currentItem != null) {
			currentItems.add(currentItem);
		}

		final List<Link> currentLinks = Lists.newArrayList();
		return new Representation<TestItem>().builder().links(currentLinks).items(currentItems).build();
	}

	@RequestMapping(value = GET_PATH, method = RequestMethod.GET)
	public Representation<TestItem> get() {

		this.checkCreateTestData();

		return this.representation;
	}

	@RequestMapping(value = ADD_PATH, method = RequestMethod.POST)
	public Representation<TestItem> add(@RequestBody final Representation<TestItem> representation) {
		this.checkCreateTestData();

		final List<TestItem> newItems = Lists.newArrayList();
		final List<Link> newLinks = Lists.newArrayList();

		for (final TestItem current : representation.getItems()) {
			current.getLinks().clear();
			current.getLinks().add(this.linkBuilder.createLink(LinkBuilder.SELF_REF, GET_PATH + "/" + current.getName()));
			newItems.add(current);
		}

		this.representation.getItems().addAll(newItems);
		return new Representation<TestItem>().builder().links(newLinks).items(newItems).build();
	}

	private void checkCreateTestData() {

		if (this.representation != null) {
			return;
		}
		this.representation = new Representation<TestItem>()
				.builder()
				.addLink(this.linkBuilder.createLink(LinkBuilder.SELF_REF, TestItemController.GET_PATH))
				.addItem(
						new TestItem(Lists.newArrayList(this.linkBuilder.createLink(LinkBuilder.SELF_REF, TestItemController.GET_PATH + "/john")),
								"john", 25))
				.addItem(
						new TestItem(Lists.newArrayList(this.linkBuilder.createLink(LinkBuilder.SELF_REF, TestItemController.GET_PATH + "/jane")),
								"jane", 30)).build();
	}
}
