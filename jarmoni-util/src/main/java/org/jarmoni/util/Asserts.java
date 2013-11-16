/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Sep 14, 2013
 */
package org.jarmoni.util;

/**
 * Helper-class which performs basic checks and throws {@link Exception}s if checks are not passed
 */
public final class Asserts {

	private Asserts() {
	}

	/**
	 * checks if argument <code>arg</code> with name <code>argName</code> is null and throws
	 * {@link NullPointerException} with standard-message if so.
	 */
	public static <T> T notNullSimple(final T arg, final String argName) {
		if (arg == null) {
			throw new NullPointerException(new StringBuilder().append("'").append(argName).append("' must not be null").toString());
		}
		return arg;
	}

	/**
	 * checks if argument <code>arg</code> is null and throws {@link NullPointerException} with <code>message</code>.
	 */
	public static <T> T notNull(final T arg, final String message) {
		if (arg == null) {
			throw new NullPointerException(message);
		}
		return arg;
	}

	/**
	 * checks if argument <code>arg</code> with name <code>argName</code> is null and throws instance of
	 * <code>exceptionClass</code> with standard-message if so.
	 */
	public static <T, E extends Exception> T notNullSimple(final T arg, final String argName, final Class<E> exceptionClass) throws E {
		if (arg == null) {
			throw ReflectionUtil.createException(exceptionClass, new StringBuilder().append("'").append(argName).append("' must not be null'")
					.toString(), null);
		}
		return arg;
	}

	/**
	 * checks if argument <code>arg</code> is null and throws instance of <code>exceptionClass</code> with
	 * <code>message</code>.
	 */
	public static <T, E extends Exception> T notNull(final T arg, final String message, final Class<E> exceptionClass) throws E {
		if (arg == null) {
			throw ReflectionUtil.createException(exceptionClass, message, null);
		}
		return arg;
	}

	/**
	 * checks if {@link CharSequence} <code>arg</code> with name <code>argName</code> is null or empty and throws
	 * instance of <code>exceptionClass</code> with standard-message if so.
	 */
	public static <E extends Exception> CharSequence notNullOrEmptySimple(final CharSequence arg, final String argName, final Class<E> exceptionClass)
			throws E {
		if (arg == null || arg.length() == 0) {
			throw ReflectionUtil.createException(exceptionClass,
					new StringBuilder().append("'").append(argName).append("' must not be not null||empty").toString(), null);
		}
		return arg;
	}

	/**
	 * checks if {@link CharSequence} <code>arg</code> with name <code>argName</code> is null or empty and throws
	 * instance of <code>exceptionClass</code> with <code>message</code> if so.
	 */
	public static <E extends Exception> CharSequence notNullOrEmpty(final CharSequence arg, final String message, final Class<E> exceptionClass)
			throws E {
		if (arg == null || arg.length() == 0) {
			throw ReflectionUtil.createException(exceptionClass, message, null);
		}
		return arg;
	}

	/**
	 * checks if condition <code>expression</code> fails and throws {@link IllegalStateException} with
	 * <code>message</code> if so.
	 */
	public static void state(final boolean expression, final String message) {
		if (!expression) {
			throw new IllegalStateException(message);
		}
	}

	/**
	 * checks if condition <code>expression</code> fails and throws instance of <code>exceptionClass</code> if so.
	 */
	public static <E extends Exception> void state(final boolean expression, final String message, final Class<E> exceptionClass) throws E {
		if (!expression) {
			throw ReflectionUtil.createException(exceptionClass, message);
		}
	}
}
