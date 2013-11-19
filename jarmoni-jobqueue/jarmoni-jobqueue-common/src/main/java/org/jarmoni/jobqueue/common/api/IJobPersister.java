/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 17, 2013
 */
package org.jarmoni.jobqueue.common.api;

import java.util.Collection;

import org.jarmoni.jobqueue.common.impl.JobQueueException;

public interface IJobPersister {

	/**
	 * Inserts job into store
	 * 
	 * @return jobId
	 */
	String insert(Object jobObject, String group, Long timeout) throws JobQueueException;

	void delete(String jobId) throws JobQueueException;

	void pause(String jobId) throws JobQueueException;

	IJobEntity resume(String jobId) throws JobQueueException;

	void update(String jobId, Object jobObject) throws JobQueueException;

	IJobEntity getJobEntity(String jobId) throws JobQueueException;

	Collection<IJobEntity> getExceededJobs() throws JobQueueException;
}
