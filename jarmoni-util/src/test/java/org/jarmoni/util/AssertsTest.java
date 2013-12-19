/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 1, 2013
 */
package org.jarmoni.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AssertsTest {

	// CHECKSTYLE:OFF
	@Rule
	public ExpectedException ee = ExpectedException.none();

	// CHECKSTYLE:ON

	@Test
	public void testNotNullSimple() throws Exception {
		this.ee.expect(NullPointerException.class);
		this.ee.expectMessage("'string' must not be null");
		final String string = null;
		Asserts.notNullSimple(string, "string");
	}

	@Test
	public void testNotNullSimpleWithEx() throws Exception {
		this.ee.expect(IllegalArgumentException.class);
		this.ee.expectMessage("'string' must not be null");
		final String string = null;
		Asserts.notNullSimple(string, "string", IllegalArgumentException.class);
	}

	@Test
	public void testNotNullSimpleTestPassed() throws Exception {
		final String string = "hallo";
		Asserts.notNullSimple(string, "string");
	}

	@Test
	public void testNotNullSimpleWithExTestPassed() throws Exception {
		final String string = "hallo";
		Asserts.notNullSimple(string, "string", IllegalArgumentException.class);
	}

	@Test
	public void testNotNull() throws Exception {
		this.ee.expect(NullPointerException.class);
		this.ee.expectMessage("no good idea that 'string' is null");
		final String string = null;
		Asserts.notNull(string, "no good idea that 'string' is null");
	}

	@Test
	public void testNotNullWithEx() throws Exception {
		this.ee.expect(IllegalArgumentException.class);
		this.ee.expectMessage("no good idea that 'string' is null");
		final String string = null;
		Asserts.notNull(string, "no good idea that 'string' is null", IllegalArgumentException.class);
	}

	@Test
	public void testNotNullPassed() throws Exception {
		final String string = "hallo";
		Asserts.notNull(string, "This test should throw no ex");
	}

	@Test
	public void testNotNullWithExPassed() throws Exception {
		final String string = "hallo";
		Asserts.notNull(string, "This test should throw no ex", IllegalArgumentException.class);
	}

	@Test
	public void testNotNullOrEmptySimpleIsEmpty() throws Exception {
		this.ee.expect(IllegalStateException.class);
		this.ee.expectMessage("'string' must not be null||empty");
		Asserts.notNullOrEmptySimple("", "string", IllegalStateException.class);
	}

	@Test
	public void testNotNullOrEmptySimpleIsNull() throws Exception {
		this.ee.expect(IllegalArgumentException.class);
		this.ee.expectMessage("'string' must not be null||empty");
		Asserts.notNullOrEmptySimple(null, "string", IllegalArgumentException.class);
	}

	@Test
	public void testNotNullOrEmptySimplePassed() throws Exception {
		Asserts.notNullOrEmptySimple("xyz", "string", IllegalArgumentException.class);
	}

	@Test
	public void testNotNullOrEmptyIsEmpty() throws Exception {
		this.ee.expect(IllegalStateException.class);
		this.ee.expectMessage("string must not be empty");
		Asserts.notNullOrEmpty("", "string must not be empty", IllegalStateException.class);
	}

	@Test
	public void testNotNullOrEmptyIsNull() throws Exception {
		this.ee.expect(IllegalArgumentException.class);
		this.ee.expectMessage("string must not be null");
		Asserts.notNullOrEmpty(null, "string must not be null", IllegalArgumentException.class);
	}

	@Test
	public void testNotNullOrEmptyPassed() throws Exception {
		Asserts.notNullOrEmpty("xyz", "string", IllegalArgumentException.class);
	}

	@Test
	public void testState() throws Exception {
		this.ee.expect(IllegalStateException.class);
		this.ee.expectMessage("'1' is not the same as '2'");
		Asserts.state(1 == 2, "'1' is not the same as '2'");
	}

	@Test
	public void testStateWithEx() throws Exception {
		this.ee.expect(IllegalArgumentException.class);
		this.ee.expectMessage("'1' is not the same as '2'");
		Asserts.state(1 == 2, "'1' is not the same as '2'", IllegalArgumentException.class);
	}

	@Test
	public void testStatePassed() throws Exception {
		final String string = "s";
		Asserts.state("s".equals(string), "This test should throw no ex");
	}

	@Test
	public void testStateWithExPassed() throws Exception {
		final String string = "s";
		Asserts.state("s".equals(string), "This test should throw no ex", IllegalArgumentException.class);
	}

}
