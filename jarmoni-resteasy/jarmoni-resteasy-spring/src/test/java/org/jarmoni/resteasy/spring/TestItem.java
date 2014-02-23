/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.spring;

import java.util.List;

import org.jarmoni.resteasy.common.AbstractItem;
import org.jarmoni.resteasy.common.Link;

public class TestItem extends AbstractItem {

	private String name;
	private Integer age;

	public TestItem() {

	}

	public TestItem(final String name, final Integer age) {
		this.name = name;
		this.age = age;
	}

	public TestItem(final List<Link> links, final String name, final Integer age) {
		super(links);
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(final Integer age) {
		this.age = age;
	}

}
