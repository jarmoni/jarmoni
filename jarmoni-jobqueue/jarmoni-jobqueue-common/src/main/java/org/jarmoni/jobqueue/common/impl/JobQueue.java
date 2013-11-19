/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.impl;

import org.jarmoni.jobqueue.common.api.IJobQueue;
import org.jarmoni.jobqueue.common.api.IQueueService;
import org.jarmoni.util.Asserts;

public class JobQueue implements IJobQueue {

	private IQueueService queueService;

	public JobQueue(final IQueueService queueService) {
		this.queueService = Asserts.notNullSimple(queueService, "queueService");
	}

	@Override
	public String push(final Object jobObject, final String group) throws JobQueueException {
		return this.queueService.push(jobObject, group);
	}

	@Override
	public String push(final Object jobObject, final String group, final long timeout) throws JobQueueException {
		return this.queueService.push(jobObject, group, timeout);
	}

	@Override
	public void pause(final String jobId) throws JobQueueException {
		this.queueService.pause(jobId);

	}

	@Override
	public void resume(final String jobId) throws JobQueueException {
		this.queueService.resume(jobId);

	}

	@Override
	public void cancel(final String jobId) throws JobQueueException {
		this.queueService.cancel(jobId);
	}

	@Override
	public void setQueueService(final IQueueService queueService) {
		this.queueService = queueService;
	}

}
