/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.common;

import java.util.List;

public class TestItem extends AbstractItem {

	private final String name;
	private final Integer age;

	public TestItem(final List<Link> links, final String name, final Integer age) {
		super(links);
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public Integer getAge() {
		return age;
	}

}
