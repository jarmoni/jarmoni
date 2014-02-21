/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Feb 21, 2014
 */
package org.jarmoni.resteasy.common;

public class TestUrlResolver implements IUrlResolver {

	@Override
	public String getRootUrl() {
		return "http://myhost:8080";
	}

}
