/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.util;

public class Tuple<F, S> {

	private F first;
	private S second;

	public Tuple(final F first, final S second) {
		this.first = first;
		this.second = second;
	}

	public F getFirst() {
		return this.first;
	}

	public void setFirst(final F first) {
		this.first = first;
	}

	public S getSecond() {
		return this.second;
	}

	public void setSecond(final S second) {
		this.second = second;
	}
}
