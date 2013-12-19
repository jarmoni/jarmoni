/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 19, 2013
 */
package org.jarmoni.jobqueue.common.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.jarmoni.jobqueue.common.api.IJobEntity;
import org.jarmoni.jobqueue.common.api.IJobPersister;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Simple implementation of {@link IJobPersister} which holds all {@link IJobEntity} in a {@link Map}.<br>
 * This impl is not suited for productional use but may offer some hints when creating a more sophisticated
 * implementation.
 */
public class SimpleJobPersister implements IJobPersister {

	private final Map<String, IJobEntity> jobs = Maps.newConcurrentMap();

	@Override
	public String insert(final Object jobObject, final String group, final Long timeout) throws JobQueueException {

		final String id = UUID.randomUUID().toString();
		this.jobs.put(id,
				JobEntity.builder().id(id).jobObject(jobObject).jobGroup(group).lastUpdate(Calendar.getInstance().getTime()).timeout(timeout)
						.currentTimeout(timeout).jobState(JobState.NEW).build());
		return id;
	}

	@Override
	public void delete(final String jobId) throws JobQueueException {

		this.jobs.remove(jobId);
	}

	@Override
	public void pause(final String jobId) throws JobQueueException {

		final IJobEntity jobEntity = this.getJobEntityInternal(jobId);
		jobEntity.setCurrentTimeout(null);
		jobEntity.setJobState(JobState.PAUSED);
	}

	@Override
	public void resume(final String jobId) throws JobQueueException {

		final IJobEntity jobEntity = this.getJobEntityInternal(jobId);
		jobEntity.setLastUpdate(Calendar.getInstance().getTime());
		jobEntity.setCurrentTimeout(jobEntity.getTimeout());
		jobEntity.setJobState(JobState.NEW);
	}

	@Override
	public void update(final String jobId, final Object jobObject) throws JobQueueException {

		final IJobEntity jobEntity = this.getJobEntityInternal(jobId);
		jobEntity.setLastUpdate(Calendar.getInstance().getTime());
		jobEntity.setJobState(JobState.NEW);
		jobEntity.setJobObject(jobObject);
	}

	@Override
	public void setFinished(final String jobId) throws JobQueueException {

		final IJobEntity jobEntity = this.getJobEntityInternal(jobId);
		jobEntity.setJobState(JobState.FINISHED);
	}

	@Override
	public void setError(final String jobId) throws JobQueueException {

		final IJobEntity jobEntity = this.getJobEntityInternal(jobId);
		if (jobEntity != null) {
			jobEntity.setJobState(JobState.ERROR);
		}
	}

	@Override
	public IJobEntity getJobEntity(final String jobId) throws JobQueueException {

		return this.getJobEntityInternal(jobId);
	}

	@Override
	public Collection<IJobEntity> getNewJobs() throws JobQueueException {

		final Collection<IJobEntity> currentJobs = Lists.newArrayList();
		for (final IJobEntity jobEntity : this.jobs.values()) {
			if (JobState.NEW.equals(jobEntity.getJobState())) {
				jobEntity.setJobState(JobState.NEW_IN_PROGRESS);
				currentJobs.add(jobEntity);
			}
		}
		return currentJobs;
	}

	@Override
	public Collection<IJobEntity> getFinishedJobs() throws JobQueueException {

		final Collection<IJobEntity> currentJobs = Lists.newArrayList();
		for (final IJobEntity jobEntity : this.jobs.values()) {
			if (JobState.FINISHED.equals(jobEntity.getJobState())) {
				jobEntity.setJobState(JobState.FINISHED_IN_PROGRESS);
				currentJobs.add(jobEntity);
			}
		}
		return currentJobs;
	}

	@Override
	public Collection<IJobEntity> getTimeoutJobs() throws JobQueueException {

		final Collection<IJobEntity> currentJobs = Lists.newArrayList();
		for (final IJobEntity jobEntity : this.jobs.values()) {
			if (JobState.PAUSED.equals(jobEntity.getJobState()) || JobState.ERROR.equals(jobEntity.getJobState())
					|| JobState.EXCEEDED_IN_PROGRESS.equals(jobEntity.getJobState())) {
				continue;
			}
			if (jobEntity.getCurrentTimeout() != null) {
				if (jobEntity.getLastUpdate().getTime() + jobEntity.getCurrentTimeout() < System.currentTimeMillis()) {
					jobEntity.setJobState(JobState.EXCEEDED_IN_PROGRESS);
					currentJobs.add(jobEntity);
				}
			}
		}
		return currentJobs;
	}

	@Override
	public void refresh() throws JobQueueException {

		for (final IJobEntity jobEntity : this.jobs.values()) {

			jobEntity.setLastUpdate(Calendar.getInstance().getTime());

			if (JobState.NEW_IN_PROGRESS.equals(jobEntity.getJobState())) {
				jobEntity.setJobState(JobState.NEW);
			}
			if (JobState.FINISHED_IN_PROGRESS.equals(jobEntity.getJobState())) {
				jobEntity.setJobState(JobState.FINISHED);
			}
			if (JobState.EXCEEDED_IN_PROGRESS.equals(jobEntity.getJobState())) {
				jobEntity.setJobState(JobState.EXCEEDED);
			}
		}

	}

	private IJobEntity getJobEntityInternal(final String jobId) throws JobQueueException {

		return this.jobs.get(jobId);
	}

}
