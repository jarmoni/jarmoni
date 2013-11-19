/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jarmoni.jobqueue.common.api.IJob;
import org.jarmoni.jobqueue.common.api.IJobEntity;
import org.jarmoni.jobqueue.common.api.IJobGroup;
import org.jarmoni.jobqueue.common.api.IJobPersister;
import org.jarmoni.jobqueue.common.api.IQueueService;
import org.jarmoni.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class JobQueueService implements IQueueService {

	private static final long SLEEP_INTERVAL = 1000L;

	private final Map<String, IJobGroup> jobGroups = Maps.newHashMap();

	private final JobQueueServiceAccess queueServiceAccess;

	private IJobPersister persister;

	private boolean running = false;

	private final Logger logger = LoggerFactory.getLogger(JobQueueService.class);

	public JobQueueService() {

		this.queueServiceAccess = new JobQueueServiceAccess(this);
	}

	/**
	 * lifecycle-method
	 */
	public void startExceededJobChecker() {

		if (this.running) {
			this.logger.info("JobQueueService already running");
			return;
		}
		Executors.newSingleThreadScheduledExecutor().execute(new ExceededJobChecker());
		this.running = true;
	}

	/**
	 * lifecycle-method
	 */
	public void stopExceededJobChecker() {

		if (!this.running) {
			this.logger.info("JobQueueService is not running");
			return;
		}
		this.running = false;
	}

	@Override
	public String push(final Object jobObject, final String group) throws JobQueueException {

		return this.push(jobObject, group, null);
	}

	@Override
	public String push(final Object jobObject, final String group, final Long timeout) throws JobQueueException {

		Asserts.notNullSimple(jobObject, "jobObject", JobQueueException.class);
		Asserts.notNullOrEmptySimple(group, "group", JobQueueException.class);
		Asserts.state(this.jobGroups.containsKey(group), "Group does not exist. Group='" + group + "'", JobQueueException.class);

		final String jobId = this.persister.insert(jobObject, group, timeout);

		final IJob job = new Job(jobId, group, jobObject, this.queueServiceAccess);

		final IJobGroup jobGroup = this.getJobGroupInternal(job.getJobId());
		Asserts.notNullSimple(jobGroup.getJobReceiver(), "jobReceiver", JobQueueException.class);
		jobGroup.getJobReceiver().receive(job);

		return jobId;
	}

	@Override
	public void cancel(final String jobId) throws JobQueueException {

		Asserts.notNullOrEmptySimple(jobId, "jobId", JobQueueException.class);
		this.persister.delete(jobId);
	}

	@Override
	public void pause(final String jobId) throws JobQueueException {

		Asserts.notNullOrEmptySimple(jobId, "jobId", JobQueueException.class);
		this.persister.pause(jobId);
	}

	@Override
	public void resume(final String jobId) throws JobQueueException {

		Asserts.notNullOrEmptySimple(jobId, "jobId", JobQueueException.class);
		final IJobEntity jobEntity = this.persister.resume(jobId);

		final IJobGroup jobGroup = this.getJobGroupInternal(jobId);
		Asserts.notNullSimple(jobGroup.getJobReceiver(), "jobReceiver", JobQueueException.class);
		jobGroup.getJobReceiver().receive(new Job(jobEntity.getId(), jobEntity.getJobGroup(), jobEntity.getJobObject(), this.queueServiceAccess));
	}

	@Override
	public void update(final IJob job) throws JobQueueException {

		Asserts.notNullSimple(job, "job", JobQueueException.class);
		this.persister.update(job.getJobId(), job.getJobObject());
	}

	@Override
	public void setFinished(final IJob job) throws JobQueueException {

		Asserts.notNullSimple(job, "job", JobQueueException.class);
		final IJobGroup jobGroup = this.getJobGroupInternal(job.getJobId());
		Asserts.notNullSimple(jobGroup.getFinishedReceiver(), "finishedReceiver", JobQueueException.class);
		jobGroup.getFinishedReceiver().receive(job);

		persister.delete(job.getJobId());
	}

	@Override
	public boolean isPaused(final IJob job) throws JobQueueException {

		Asserts.notNullSimple(job, "job", JobQueueException.class);
		return this.persister.getJobEntity(job.getJobId()).getJobState().equals(JobState.PAUSED) ? true : false;
	}

	@Override
	public void setJobGroups(final Collection<IJobGroup> jobGroups) {

		Asserts.notNullSimple(jobGroups, "jobGroups");
		for (final IJobGroup jobGroup : jobGroups) {
			this.jobGroups.put(jobGroup.getName(), jobGroup);
		}
	}

	private void pushExceededJob(final IJobEntity jobEntity) throws JobQueueException {

		final IJob job = new Job(jobEntity.getId(), jobEntity.getJobGroup(), jobEntity.getJobObject(), this.queueServiceAccess);

		final IJobGroup jobGroup = this.getJobGroupInternal(job.getJobId());
		Asserts.notNullSimple(jobGroup.getTimeoutReceiver(), "timeoutReceiver", JobQueueException.class);
		jobGroup.getTimeoutReceiver().receive(job);

		this.persister.delete(job.getJobId());
	}

	private IJobGroup getJobGroupInternal(final String jobId) throws JobQueueException {

		Asserts.notNullOrEmptySimple(jobId, "jobId", JobQueueException.class);

		final IJobEntity jobEntity = this.persister.getJobEntity(jobId);
		Asserts.notNullSimple(jobEntity, "jobEntity", JobQueueException.class);
		return Asserts.notNull(this.jobGroups.get(jobEntity.getJobGroup()), "No JobGroup found for id='" + jobEntity.getJobGroup() + "'",
				JobQueueException.class);
	}

	public class ExceededJobChecker extends Thread {
		@Override
		public void run() {
			while (running) {
				try {
					final Collection<IJobEntity> jobEntities = persister.getExceededJobs();
					if (!jobEntities.isEmpty()) {
						for (final IJobEntity jobEntity : jobEntities) {
							pushExceededJob(jobEntity);
						}
					} else {
						Thread.sleep(SLEEP_INTERVAL);
					}
				} catch (final InterruptedException e) {
					logger.error("Thread interrupted");
				} catch (final Exception e) {
					logger.error("Exception while pushing exceeded jobs");
				}
			}
		}

	}
}
