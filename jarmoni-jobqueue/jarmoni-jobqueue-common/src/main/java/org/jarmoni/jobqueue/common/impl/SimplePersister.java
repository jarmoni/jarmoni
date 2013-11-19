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
import org.jarmoni.util.Asserts;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SimplePersister implements IJobPersister {

	private final Map<String, IJobEntity> jobs = Maps.newConcurrentMap();

	@Override
	public String insert(final Object jobObject, final String group, final Long timeout) throws JobQueueException {

		final String id = UUID.randomUUID().toString();
		this.jobs.put(id,
				JobEntity.builder().id(id).jobObject(jobObject).jobGroup(group).lastUpdate(Calendar.getInstance().getTime()).timeout(timeout)
						.currentTimeout(timeout).build());
		return id;
	}

	@Override
	public void delete(final String jobId) throws JobQueueException {

		final IJobEntity jobEntity = this.jobs.remove(jobId);
		Asserts.notNull(jobEntity, "No JobEntity with id='" + jobId + "' exists", JobQueueException.class);
	}

	@Override
	public void pause(final String jobId) throws JobQueueException {

		final IJobEntity jobEntity = this.getJobEntityInternal(jobId);
		jobEntity.setCurrentTimeout(null);
		jobEntity.setJobState(JobState.PAUSED);
	}

	@Override
	public IJobEntity resume(final String jobId) throws JobQueueException {

		final IJobEntity jobEntity = this.getJobEntityInternal(jobId);
		jobEntity.setLastUpdate(Calendar.getInstance().getTime());
		jobEntity.setCurrentTimeout(jobEntity.getTimeout());
		jobEntity.setJobState(JobState.PROGRESS);
		return jobEntity;
	}

	@Override
	public void update(final String jobId, final Object jobObject) throws JobQueueException {

		final IJobEntity jobEntity = this.getJobEntityInternal(jobId);
		jobEntity.setLastUpdate(Calendar.getInstance().getTime());
		jobEntity.setJobObject(jobObject);
	}

	@Override
	public IJobEntity getJobEntity(final String jobId) throws JobQueueException {

		return this.getJobEntityInternal(jobId);
	}

	@Override
	public Collection<IJobEntity> getExceededJobs() throws JobQueueException {

		final Collection<IJobEntity> currentJobs = Lists.newArrayList();
		for (final IJobEntity jobEntity : this.jobs.values()) {
			if (JobState.PAUSED.equals(jobEntity.getJobState())) {
				continue;
			}
			Asserts.notNullSimple(jobEntity.getCurrentTimeout(), "currentTimeout");
			if (jobEntity.getLastUpdate().getTime() + jobEntity.getCurrentTimeout() < System.currentTimeMillis()) {
				currentJobs.add(jobEntity);
			}
		}
		return currentJobs;
	}

	private IJobEntity getJobEntityInternal(final String jobId) throws JobQueueException {

		final IJobEntity jobEntity = this.jobs.get(jobId);
		Asserts.notNull(jobEntity, "No JobEntity with id='" + jobId + "' exists", JobQueueException.class);
		return jobEntity;
	}

}
