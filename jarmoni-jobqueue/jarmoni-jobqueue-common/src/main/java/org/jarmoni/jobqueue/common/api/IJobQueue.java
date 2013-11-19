/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.api;

import org.jarmoni.jobqueue.common.impl.JobQueueException;

public interface IJobQueue {

	String push(Object jobObject, String group) throws JobQueueException;

	String push(Object jobObject, String group, long timeout) throws JobQueueException;

	void pause(String jobId) throws JobQueueException;

	void resume(String jobId) throws JobQueueException;

	void cancel(String jobId) throws JobQueueException;

	void setQueueService(IQueueService queueService);
}
