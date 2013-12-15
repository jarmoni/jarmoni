/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 23, 2013
 */
package org.jarmoni.jobqueue.common.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jarmoni.jobqueue.common.api.IJob;
import org.jarmoni.jobqueue.common.api.IJobEntity;
import org.jarmoni.jobqueue.common.api.IJobGroup;
import org.jarmoni.jobqueue.common.api.IJobPersister;
import org.jarmoni.jobqueue.common.api.IJobReceiver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

public class JobQueueServiceTest {

	// CHECKSTYLE:OFF
	@Rule
	public ExpectedException ee = ExpectedException.none();
	// CHECKSTYLE:ON

	private JobQueueService jobQueueService;
	private IJobPersister persister;
	private IMocksControl mocksControl;

	private IJobGroup group;
	private IJobReceiver jobReceiver;
	private IJobReceiver finishedReceiver;
	private IJobReceiver timeoutReceiver;
	private IJob job;
	private final long timeout = System.currentTimeMillis();

	@Before
	public void setUp() throws Exception {

		this.mocksControl = EasyMock.createControl();
		this.persister = this.mocksControl.createMock(IJobPersister.class);
		this.jobReceiver = this.mocksControl.createMock(IJobReceiver.class);
		this.finishedReceiver = this.mocksControl.createMock(IJobReceiver.class);
		this.timeoutReceiver = this.mocksControl.createMock(IJobReceiver.class);

		this.group = new JobGroup("myGroup");
		this.group.setJobReceiver(this.jobReceiver);
		this.group.setFinishedReceiver(this.finishedReceiver);
		this.group.setTimeoutReceiver(this.timeoutReceiver);

		this.jobQueueService = new JobQueueService(Lists.newArrayList(this.group), this.persister);

		this.job = new Job("abc", this.group.getName(), "123", this.jobQueueService.getQueueServiceAccess());
	}

	@Test
	public void testPushGroup() throws Exception {

		EasyMock.expect(this.persister.insert(this.job.getJobObject(), this.group.getName(), this.timeout)).andReturn("abc");
		this.mocksControl.replay();
		assertEquals("abc", this.jobQueueService.push(this.job.getJobObject(), this.group.getName(), this.timeout));
	}

	@Test
	public void testPushGroupNoGroup() throws Exception {

		this.ee.expect(JobQueueException.class);
		this.ee.expectMessage("Group does not exist. Group='myGroup'");
		this.jobQueueService.getJobGroups().clear();
		EasyMock.expect(this.persister.insert(this.job.getJobObject(), this.group.getName(), this.timeout)).andReturn("abc");
		this.mocksControl.replay();
		this.jobQueueService.push(this.job.getJobObject(), this.group.getName(), this.timeout);
	}

	@Test
	public void testCancel() throws Exception {

		this.persister.delete("abc");
		EasyMock.expectLastCall();
		this.mocksControl.replay();
		this.jobQueueService.cancel("abc");
	}

	@Test
	public void testPause() throws Exception {

		this.persister.pause("abc");
		EasyMock.expectLastCall();
		this.mocksControl.replay();
		this.jobQueueService.pause("abc");
	}

	@Test
	public void testResume() throws Exception {

		this.persister.resume("abc");
		EasyMock.expectLastCall();
		this.mocksControl.replay();
		this.jobQueueService.resume("abc");
	}

	@Test
	public void testUpdate() throws Exception {

		this.persister.update(this.job.getJobId(), this.job.getJobObject());
		EasyMock.expectLastCall();
		this.mocksControl.replay();
		this.jobQueueService.update(this.job);
	}

	@Test
	public void testSetFinished() throws Exception {

		this.persister.setFinished(this.job.getJobId());
		EasyMock.expectLastCall();
		this.mocksControl.replay();
		this.jobQueueService.setFinished(this.job);
	}

	@Test
	public void testIsPausedTrue() throws Exception {

		final IJobEntity jobEntity = JobEntity.builder().id("abc").jobState(JobState.PAUSED).build();
		EasyMock.expect(this.persister.getJobEntity("abc")).andReturn(jobEntity);
		this.mocksControl.replay();
		assertTrue(this.jobQueueService.isPaused(this.job));
	}

	@Test
	public void testIsPausedFalse() throws Exception {

		final IJobEntity jobEntity = JobEntity.builder().id("abc").jobState(JobState.NEW).build();
		EasyMock.expect(this.persister.getJobEntity("abc")).andReturn(jobEntity);
		this.mocksControl.replay();
		assertFalse(this.jobQueueService.isPaused(this.job));
	}
}
