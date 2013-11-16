/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 8, 2013
 */
package org.jarmoni.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class ReflectionUtil {

	private ReflectionUtil() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] arrayFromList(final List<T> list, final Class<T> typeClass) {
		final T[] array = (T[]) Array.newInstance(typeClass, list.size());
		return list.toArray(array);
	}

	public static <T> T createObject(final String name, final Class<? extends T> typeClass) throws Exception {
		final Class<?> clazz = Class.forName(name);
		final Class<? extends T> typedClazz = clazz.asSubclass(typeClass);
		return typedClazz.newInstance();
	}

	public static <E extends Exception> E createException(final Class<E> exceptionClass, final Throwable cause) {
		return createException(exceptionClass, null, cause);
	}

	public static <E extends Exception> E createException(final Class<E> exceptionClass, final String message) {
		return createException(exceptionClass, message, null);
	}

	public static <E extends Exception> E createException(final Class<E> exceptionClass, final String message, final Throwable cause) {

		if (message == null && cause == null) {
			try {
				return exceptionClass.newInstance();
				// CHECKSTYLE:OFF
			} catch (InstantiationException | IllegalAccessException e) {
				// CHECKSTYLE:ON
				throw new IllegalStateException("No constructor with no arguments found for class='" + exceptionClass.getCanonicalName() + "'");
			}
		}

		Constructor<E> constructor = null;
		try {
			constructor = exceptionClass.getConstructor(String.class, Throwable.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Constructor with single 'String-argument' expected");
		}
		try {
			return constructor.newInstance(message, cause);
			// CHECKSTYLE:OFF
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// CHECKSTYLE:ON
			throw new IllegalStateException(new StringBuilder().append("Could not create exception-object. Message='").append(message)
					.append("', cause='").append(cause).append("'").toString());
		}
	}
}
