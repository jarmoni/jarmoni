/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.impl;

import org.jarmoni.jobqueue.api.IJob;
import org.jarmoni.util.Asserts;

public class Job implements IJob {

	private final String id;
	private final String groupId;
	private final Object jobObject;
	private final JobQueueServiceAccess queueServiceAccess;

	public Job(final String id, final String groupId, final Object jobObject, final JobQueueServiceAccess queueServiceAccess) {
		this.id = Asserts.notNullSimple(id, "id");
		this.groupId = Asserts.notNullSimple(groupId, "groupId");
		this.jobObject = Asserts.notNullSimple(jobObject, "jobObject");
		this.queueServiceAccess = Asserts.notNullSimple(queueServiceAccess, "queueServiceAccess");
	}

	@Override
	public String getJobId() {
		return this.id;
	}

	@Override
	public String getGroupId() {
		return this.groupId;
	}

	@Override
	public Object getJobObject() {
		return this.jobObject;
	}

	@Override
	public void update() throws JobQueueException {
		this.queueServiceAccess.update(this);

	}

	@Override
	public boolean isPaused() throws JobQueueException {
		return this.queueServiceAccess.isPaused(this);
	}

	@Override
	public boolean isCanceled() throws JobQueueException {
		return this.queueServiceAccess.isCanceled(this);
	}

	@Override
	public boolean setFinished(final IJob job) throws JobQueueException {
		// TODO Auto-generated method stub
		return false;
	}

}
