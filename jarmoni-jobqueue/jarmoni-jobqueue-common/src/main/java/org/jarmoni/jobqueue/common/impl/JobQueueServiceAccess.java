/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.impl;

import org.jarmoni.jobqueue.common.api.IJob;
import org.jarmoni.jobqueue.common.api.IJobQueueService;
import org.jarmoni.util.Asserts;

public class JobQueueServiceAccess {

	private final IJobQueueService queueService;

	public JobQueueServiceAccess(final IJobQueueService queueService) {
		this.queueService = Asserts.notNullSimple(queueService, "queueService");
	}

	void update(final IJob job) throws JobQueueException {
		this.queueService.update(job);
	}

	boolean isPaused(final IJob job) throws JobQueueException {
		return this.queueService.isPaused(job);
	}

	void setFinished(final IJob job) throws JobQueueException {
		this.queueService.setFinished(job);
	}
}
