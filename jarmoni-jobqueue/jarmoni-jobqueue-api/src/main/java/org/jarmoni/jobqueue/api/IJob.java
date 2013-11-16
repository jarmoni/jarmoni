/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.api;

import org.jarmoni.jobqueue.impl.JobQueueException;

public interface IJob {

	String getJobId();

	String getGroupId();

	Object getJobObject();

	void update() throws JobQueueException;

	boolean isPaused() throws JobQueueException;

	boolean isCanceled() throws JobQueueException;

	boolean setFinished(IJob job) throws JobQueueException;

}
