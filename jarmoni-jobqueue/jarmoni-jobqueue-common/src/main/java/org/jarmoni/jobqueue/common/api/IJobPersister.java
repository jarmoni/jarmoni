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
	 * Inserts job into store. Follwing properties must be set:
	 * <ul>
	 * <li>jobObject = <code>jobObject</code></li>
	 * <li>group = <code>group</code></li>
	 * <li>lastUpdate = current time</li>
	 * <li>jobState = <code>NEW</code></li>
	 * <li>timeout = <code>timeout</code></li>
	 * <li>currentTimeout = <code>timeout</code></li>
	 * </ul>
	 * 
	 * @param jobObject
	 * @param group
	 * @param timeout
	 * @return jobId
	 * @throws JobQueueException If insertion failed
	 */
	String insert(Object jobObject, String group, Long timeout) throws JobQueueException;

	/**
	 * Deletes job from store.
	 * 
	 * @param jobId
	 * @throws JobQueueException If deletion failed
	 */
	void delete(String jobId) throws JobQueueException;

	/**
	 * Pauses a job. Following properties must be set:
	 * <ul>
	 * <li>jobState = <code>PAUSED</code></li>
	 * <li>currentTimeout = null</li>
	 * </ul>
	 * 
	 * @param jobId
	 * @throws JobQueueException if update failed
	 */
	void pause(String jobId) throws JobQueueException;

	/**
	 * Resumes a (paused) job. Following properties must be set:
	 * <ul>
	 * <li>jobState = <code>NEW</code></li>
	 * <li>currentTimeout = timeout of job</li>
	 * <li>lastUpdate = current time</code>
	 * </ul>
	 * 
	 * @param jobId
	 * @throws JobQueueException
	 */
	void resume(String jobId) throws JobQueueException;

	/**
	 * Updates the job-object. Following properties must be set:
	 * <ul>
	 * <li>jobObject = <code>jobObject</code></li>
	 * <li>jobState = <code>NEW</code>
	 * <li>lastUpdate = current time</code>
	 * </ul>
	 * 
	 * @param jobId
	 * @param jobObject
	 * @throws JobQueueException
	 */
	void update(String jobId, Object jobObject) throws JobQueueException;

	/**
	 * Sets state to <code>FINISHED</code>
	 * 
	 * @param jobId
	 * @throws JobQueueException
	 */
	void setFinished(String jobId) throws JobQueueException;

	/**
	 * Returns job with given <code>jobId</code>
	 * 
	 * @param jobId
	 * @return {@link IJobEntity}
	 * @throws JobQueueException
	 */
	IJobEntity getJobEntity(String jobId) throws JobQueueException;

	/**
	 * Returns all Jobs with jobState=<code>NEW</code>.<br>
	 * The <code>jobState</code> of these jobs must be set to <code>NEW_IN_PROGRESS</code> before method returns.
	 * 
	 * @return {@link Collection}
	 * @throws JobQueueException
	 */
	Collection<IJobEntity> getNewJobs() throws JobQueueException;

	/**
	 * Returns all Jobs with jobState=<code>FINISHED</code>.<br>
	 * The <code>jobState</code> of these jobs must be set to <code>FINISHED_IN_PROGRESS</code> before method returns.
	 * 
	 * @return {@link Collection}
	 * @throws JobQueueException
	 */
	Collection<IJobEntity> getFinishedJobs() throws JobQueueException;

	/**
	 * Returns all exceeded jobs. Exceeded jobs are all jobs with:
	 * <ul>
	 * <li><code>jobState != PAUSED</code> <b>and</b></li>
	 * <li><code>currentTimeout != null</code> <b>and</b></li>
	 * <li><code>current time < lastUpdate + currentTimeout</li>
	 * </ul>
	 * 
	 * The <code>jobState</code> of these jobs must be set to <code>EXCEEDED_IN_PROGRESS</code> before method returns.
	 * 
	 */
	Collection<IJobEntity> getExceededJobs() throws JobQueueException;

	/**
	 * Should be invoked before the scanner-threads start. Sets the following properties:
	 * <ul>
	 * <li>lastUpdate = current time</li>
	 * <li>if <code>jobState = NEW_IN_PROGRESS</code> -> <code>jobState = NEW</code></li>
	 * <li>if <code>jobState = FINISHED_IN_PROGRESS</code> -> <code>jobState = FINISHED</code></li>
	 * <li>if <code>jobState = EXCEEDED_IN_PROGRESS</code> -> <code>jobState = EXCEEDED</code></li>
	 * <li>if <code>jobState = ERROR_IN_PROGRESS</code> -> <code>jobState = ERROR</code></li>
	 * </ul>
	 * 
	 * @throws JobQueueException
	 */
	void refresh() throws JobQueueException;
}
