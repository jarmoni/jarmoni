/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.api;

import java.util.Collection;

import org.jarmoni.jobqueue.impl.JobQueueException;

public interface IQueueService {

	String push(Object jobObject, String group) throws JobQueueException;

	String push(Object jobObject, String group, long timeout) throws JobQueueException;

	void cancel(String jobId) throws JobQueueException;

	void pause(String jobId) throws JobQueueException;

	void resume(final String jobId) throws JobQueueException;

	void update(IJob job) throws JobQueueException;

	boolean isPaused(IJob job) throws JobQueueException;

	boolean isCanceled(IJob job) throws JobQueueException;

	void setFinished(IJob job) throws JobQueueException;

	void setJobGroups(Collection<IJobGroup> jobGroups);
}
