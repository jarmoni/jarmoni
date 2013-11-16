/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.impl;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.jarmoni.jobqueue.api.IJob;
import org.jarmoni.jobqueue.api.IJobGroup;
import org.jarmoni.jobqueue.api.IQueueService;
import org.jarmoni.util.Asserts;
import org.jarmoni.util.Tuple;

import com.google.common.collect.Maps;

public class TestJobQueueService implements IQueueService {

	private final Map<String, IJobGroup> jobGroups = Maps.newHashMap();

	private final Map<String, Tuple<IJob, JobState>> jobs = Maps.newConcurrentMap();

	private final JobQueueServiceAccess queueServiceAccess;

	public TestJobQueueService() {

		this.queueServiceAccess = new JobQueueServiceAccess(this);
	}

	@Override
	public String push(final Object jobObject, final String group) throws JobQueueException {

		Asserts.notNullSimple(jobObject, "jobObject", JobQueueException.class);
		Asserts.notNullOrEmptySimple(group, "group", JobQueueException.class);
		Asserts.state(this.jobGroups.containsKey(group), "Group does not exist. Group='" + group + "'", JobQueueException.class);

		final String id = UUID.randomUUID().toString();
		this.jobs.put(id, new Tuple<IJob, JobState>(new Job(id, group, jobObject, this.queueServiceAccess), JobState.NEW));
		return id;
	}

	@Override
	public String push(final Object jobObject, final String group, final long timeout) throws JobQueueException {

		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void cancel(final String jobId) throws JobQueueException {

		if (this.jobs.remove(jobId) == null) {
			throw new JobQueueException("No job found for jobId='" + jobId + "'");
		}
	}

	@Override
	public void pause(final String jobId) throws JobQueueException {

		this.getJobTupleInternal(jobId).setSecond(JobState.PAUSED);
	}

	@Override
	public void resume(final String jobId) throws JobQueueException {

		this.getJobTupleInternal(jobId).setSecond(JobState.NEW);

	}

	@Override
	public void update(final IJob job) throws JobQueueException {

		this.getJobTupleInternal(job).setFirst(job);
	}

	@Override
	public void setFinished(final IJob job) throws JobQueueException {

		final IJobGroup jobGroup = this.getJobGroupInternal(job);
		if (jobGroup.getFinishedReceiver() != null) {
			jobGroup.getFinishedReceiver().receive(job);
		}
		this.cancel(job.getJobId());

	}

	@Override
	public boolean isPaused(final IJob job) throws JobQueueException {

		return this.getJobTupleInternal(job).getSecond().equals(JobState.PAUSED) ? true : false;
	}

	@Override
	public boolean isCanceled(final IJob job) throws JobQueueException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setJobGroups(final Collection<IJobGroup> jobGroups) {
		Asserts.notNullSimple(jobGroups, "jobGroups");
		for (final IJobGroup jobGroup : jobGroups) {
			this.jobGroups.put(jobGroup.getName(), jobGroup);
		}
	}

	private Tuple<IJob, JobState> getJobTupleInternal(final IJob job) throws JobQueueException {

		Asserts.notNullSimple(job, "job", JobQueueException.class);
		return this.getJobTupleInternal(job.getJobId());
	}

	private Tuple<IJob, JobState> getJobTupleInternal(final String jobId) throws JobQueueException {

		Asserts.notNullSimple(jobId, "jobId", JobQueueException.class);
		final Tuple<IJob, JobState> jobTuple = this.jobs.get(jobId);
		Asserts.notNull(jobTuple, "No job found for jobId='" + jobId + "'", JobQueueException.class);
		return jobTuple;
	}

	private IJobGroup getJobGroupInternal(final IJob job) throws JobQueueException {

		final Tuple<IJob, JobState> jobTuple = this.getJobTupleInternal(job);
		Asserts.notNullSimple(jobTuple.getFirst().getGroupId(), "groupId", JobQueueException.class);
		return Asserts.notNull(this.jobGroups.get(jobTuple.getFirst().getGroupId()), "No JobGroup found for id='" + jobTuple.getFirst().getGroupId()
				+ "'", JobQueueException.class);
	}
}
