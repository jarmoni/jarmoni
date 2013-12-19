/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Dec 19, 2013
 */
package org.jarmoni.jobqueue.common.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMock;
import org.jarmoni.jobqueue.common.api.IJob;
import org.jarmoni.jobqueue.common.api.IJobEntity;
import org.jarmoni.jobqueue.common.api.IJobFinishedStrategy;
import org.jarmoni.jobqueue.common.api.IJobPersister;
import org.jarmoni.jobqueue.common.api.IJobQueue;
import org.jarmoni.jobqueue.common.api.IJobReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JobQueueTest {

	private ClassPathXmlApplicationContext ctx;

	private IJobQueue jobQueue;
	private IJobPersister persister;
	private IJobReceiver jobReceiver;
	private IJobReceiver finishedReceiver;
	private IJobReceiver timeoutReceiver;
	private IJobFinishedStrategy finishedStrategy;

	@Before
	public void setUp() throws Exception {
		this.ctx = new ClassPathXmlApplicationContext("/org/jarmoni/jobqueue/impl/queue.xml");
		this.jobQueue = this.ctx.getBean(IJobQueue.class);
		this.persister = this.ctx.getBean(IJobPersister.class);
		this.jobReceiver = (IJobReceiver) ctx.getBean("jobReceiver");
		this.finishedReceiver = (IJobReceiver) ctx.getBean("finishedReceiver");
		this.timeoutReceiver = (IJobReceiver) ctx.getBean("timeoutReceiver");
		this.finishedStrategy = (IJobFinishedStrategy) ctx.getBean("finishedStrategy");

	}

	@After
	public void tearDown() throws Exception {
		if (this.ctx != null) {
			this.ctx.close();
		}
	}

	@Test
	public void testRegular() throws Exception {

		final TestObject testObject = new TestObject("first", "first object");

		this.jobReceiver.receive(EasyMock.anyObject(IJob.class));
		EasyMock.expectLastCall().times(1);
		EasyMock.expect(this.finishedStrategy.finished(EasyMock.anyObject(IJob.class))).andReturn(false);

		this.jobReceiver.receive(EasyMock.anyObject(IJob.class));
		EasyMock.expectLastCall().times(1);
		EasyMock.expect(this.finishedStrategy.finished(EasyMock.anyObject(IJob.class))).andReturn(false);

		this.jobReceiver.receive(EasyMock.anyObject(IJob.class));
		EasyMock.expectLastCall().times(1);
		EasyMock.expect(this.finishedStrategy.finished(EasyMock.anyObject(IJob.class))).andReturn(true);

		this.finishedReceiver.receive(EasyMock.anyObject(IJob.class));
		EasyMock.expectLastCall().times(1);

		EasyMock.replay(this.jobReceiver, this.finishedReceiver, this.timeoutReceiver, this.finishedStrategy);

		final String id = this.jobQueue.push(testObject, "myGroup");

		Thread.sleep(10000);
		EasyMock.verify(this.jobReceiver, this.finishedReceiver, this.timeoutReceiver, this.finishedStrategy);
		assertNull(this.persister.getJobEntity(id));
	}

	@Test
	public void testError() throws Exception {

		final TestObject testObject = new TestObject("first", "first object");

		this.jobReceiver.receive(EasyMock.anyObject(IJob.class));
		EasyMock.expectLastCall().andThrow(new RuntimeException("abc"));

		final String id = this.jobQueue.push(testObject, "myGroup");

		EasyMock.replay(this.jobReceiver, this.finishedReceiver, this.timeoutReceiver, this.finishedStrategy);

		Thread.sleep(2000L);
		EasyMock.verify(this.jobReceiver, this.finishedReceiver, this.timeoutReceiver, this.finishedStrategy);
		final IJobEntity jobEntity = this.persister.getJobEntity(id);
		assertEquals(JobState.ERROR, jobEntity.getJobState());
	}

	@Test
	public void testTimeout() throws Exception {

		final TestObject testObject = new TestObject("first", "first object");

		this.jobReceiver.receive(EasyMock.anyObject(IJob.class));
		EasyMock.expectLastCall().anyTimes();

		EasyMock.expect(this.finishedStrategy.finished(EasyMock.anyObject(IJob.class))).andReturn(false).anyTimes();

		this.timeoutReceiver.receive(EasyMock.anyObject(IJob.class));
		EasyMock.expectLastCall().times(1);

		EasyMock.replay(this.jobReceiver, this.finishedReceiver, this.timeoutReceiver, this.finishedStrategy);

		final String id = this.jobQueue.push(testObject, "myGroup", 200L);

		Thread.sleep(3000L);
		EasyMock.verify(this.jobReceiver, this.finishedReceiver, this.timeoutReceiver, this.finishedStrategy);
		assertNull(this.persister.getJobEntity(id));
	}

	@Test
	public void testPausedNoTimeout() throws Exception {

		final TestObject testObject = new TestObject("first", "first object");

		this.jobReceiver.receive(EasyMock.anyObject(IJob.class));
		EasyMock.expectLastCall().anyTimes();

		EasyMock.expect(this.finishedStrategy.finished(EasyMock.anyObject(IJob.class))).andReturn(false).anyTimes();

		EasyMock.replay(this.jobReceiver, this.finishedReceiver, this.timeoutReceiver, this.finishedStrategy);

		final String id = this.jobQueue.push(testObject, "myGroup", 1000L);
		this.persister.pause(id);

		Thread.sleep(3000L);

		EasyMock.verify(this.jobReceiver, this.finishedReceiver, this.timeoutReceiver, this.finishedStrategy);
		assertNotNull(this.persister.getJobEntity(id));
	}
}
