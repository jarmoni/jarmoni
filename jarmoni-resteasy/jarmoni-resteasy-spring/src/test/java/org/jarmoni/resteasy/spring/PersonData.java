/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.spring;

public class PersonData {

	private String name;
	private Integer age;

	public PersonData() {
	}

	public String getName() {
		return name;
	}

	public Integer getAge() {
		return age;
	}

	public static PersonDataBuilder builder() {
		return new PersonDataBuilder();
	}

	public static final class PersonDataBuilder {

		private final PersonData data;

		private PersonDataBuilder() {
			this.data = new PersonData();
		}

		public PersonDataBuilder name(final String name) {
			this.data.name = name;
			return this;
		}

		public PersonDataBuilder age(final Integer age) {
			this.data.age = age;
			return this;
		}

		public PersonData build() {
			return this.data;
		}
	}

}
