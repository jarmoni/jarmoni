/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.api;

import org.jarmoni.jobqueue.common.impl.JobQueueException;

public interface IJob {

	String getJobId();

	String getGroupId();

	Object getJobObject();

	void update() throws JobQueueException;

	boolean isPaused() throws JobQueueException;

	void setFinished(IJob job) throws JobQueueException;
}
