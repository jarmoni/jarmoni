/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Dec 9, 2013
 */
package org.jarmoni.jobqueue.jpa;

import org.jarmoni.jobqueue.common.api.IJobEntity;
import org.jarmoni.jobqueue.common.api.IJobPersister;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JpaJobPersisterTest {

	private ClassPathXmlApplicationContext ctx;

	@Before
	public void setUp() throws Exception {
		this.ctx = new ClassPathXmlApplicationContext("/org/jarmoni/jobqueue/jpa/applicationContext.xml");
	}

	@After
	public void tearDown() throws Exception {
		if (this.ctx != null) {
			this.ctx.close();
		}
	}

	@Test
	public void testIt() throws Exception {
		final IJobPersister persister = this.ctx.getBean(IJobPersister.class);
		final String id = persister.insert(new TestObject("testObj", "desc"), "myGroup", 1000L);
		System.out.println("#####################id = " + id);
		final IJobEntity entity = persister.getJobEntity(id);
		System.out.println("********************name = " + ((TestObject) entity.getJobObject()).getName());
	}
	// TODO Auto-generated constructor stub
}
