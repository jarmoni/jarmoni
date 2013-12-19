/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jarmoni.jobqueue.common.api.IJob;
import org.jarmoni.jobqueue.common.api.IJobEntity;
import org.jarmoni.jobqueue.common.api.IJobFinishedStrategy;
import org.jarmoni.jobqueue.common.api.IJobGroup;
import org.jarmoni.jobqueue.common.api.IJobPersister;
import org.jarmoni.jobqueue.common.api.IJobQueueService;
import org.jarmoni.jobqueue.common.api.IJobReceiver;
import org.jarmoni.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class JobQueueService implements IJobQueueService {

	private static final long SLEEP_INTERVAL = 1000L;

	private final Map<String, IJobGroup> jobGroups = Maps.newHashMap();

	private final JobQueueServiceAccess queueServiceAccess;

	private final IJobPersister persister;

	private int numReceiverThreads = 1;
	private int numFinishedReceiverThreads = 1;
	private int numTimeoutReceiverThreads = 1;

	private ExecutorService newJobExecutorPool;
	private ExecutorService finishedJobExecutorPool;
	private ExecutorService exceededJobExecutorPool;
	private ExecutorService jobScannerPool;

	private final Logger logger = LoggerFactory.getLogger(JobQueueService.class);

	public JobQueueService(final Collection<IJobGroup> jobGroups, final IJobPersister jobPersister) {

		Asserts.notNullSimple(jobGroups, "jobGroups");
		for (final IJobGroup group : jobGroups) {
			this.jobGroups.put(group.getName(), this.validateJobGroup(group));
		}
		this.persister = Asserts.notNullSimple(jobPersister, "jobPersister");
		this.queueServiceAccess = new JobQueueServiceAccess(this);
		this.init();
	}

	private void init() {

		this.newJobExecutorPool = Executors.newFixedThreadPool(this.numReceiverThreads,
				new ThreadFactoryBuilder().setNameFormat("new-job-executor-%d").build());
		this.finishedJobExecutorPool = Executors.newFixedThreadPool(this.numFinishedReceiverThreads,
				new ThreadFactoryBuilder().setNameFormat("finished-job-executor-%d").build());
		this.exceededJobExecutorPool = Executors.newFixedThreadPool(this.numTimeoutReceiverThreads,
				new ThreadFactoryBuilder().setNameFormat("exceeded-job-executor-%d").build());
		this.jobScannerPool = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("job-scanner-%d").build());
	}

	/**
	 * lifecycle-method
	 */
	public void start() {

		this.logger.info("JobQueueService#start()");

		try {
			this.persister.refresh();
		} catch (final JobQueueException e) {
			throw new RuntimeException("Could not refresh jobs", e);
		}

		this.jobScannerPool.execute(new JobScanner(this.persister, new IJobSubmitter() {
			@Override
			public void submit(final IJobEntity jobEntity) throws JobQueueException {
				processNewJob(jobEntity);
			}
		}, new IJobSubmitter() {
			@Override
			public void submit(final IJobEntity jobEntity) throws JobQueueException {
				processFinishedJob(jobEntity);

			}
		}, new IJobSubmitter() {
			@Override
			public void submit(final IJobEntity jobEntity) throws JobQueueException {
				processExceededJob(jobEntity);

			}
		}));

	}

	/**
	 * lifecycle-method
	 */
	public void stop() {

		this.logger.info("JobQueueService#stop()");
		this.jobScannerPool.shutdownNow();
		this.newJobExecutorPool.shutdownNow();
		this.finishedJobExecutorPool.shutdownNow();
		this.exceededJobExecutorPool.shutdownNow();
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

		return this.persister.insert(jobObject, group, timeout);
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
		this.persister.resume(jobId);
	}

	@Override
	public void update(final IJob job) throws JobQueueException {

		Asserts.notNullSimple(job, "job", JobQueueException.class);
		this.persister.update(job.getJobId(), job.getJobObject());
	}

	@Override
	public void setFinished(final IJob job) throws JobQueueException {

		Asserts.notNullSimple(job, "job", JobQueueException.class);
		this.persister.setFinished(job.getJobId());
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

	private void processNewJob(final IJobEntity jobEntity) throws JobQueueException {

		final IJob job = new Job(jobEntity.getId(), jobEntity.getJobGroup(), jobEntity.getJobObject(), this.queueServiceAccess);

		final IJobGroup jobGroup = this.getJobGroupInternal(job.getJobId());
		this.newJobExecutorPool.execute(new ReceiverExecutor(jobGroup.getJobReceiver(), "jobReceiver", job, jobGroup.getJobFinishedStrategies(),
				false));
	}

	private void processFinishedJob(final IJobEntity jobEntity) throws JobQueueException {

		final IJob job = new Job(jobEntity.getId(), jobEntity.getJobGroup(), jobEntity.getJobObject(), this.queueServiceAccess);

		final IJobGroup jobGroup = this.getJobGroupInternal(job.getJobId());
		this.finishedJobExecutorPool.execute(new ReceiverExecutor(jobGroup.getFinishedReceiver(), "finishedReceiver", job, null, true));
	}

	private void processExceededJob(final IJobEntity jobEntity) throws JobQueueException {

		final IJob job = new Job(jobEntity.getId(), jobEntity.getJobGroup(), jobEntity.getJobObject(), this.queueServiceAccess);

		final IJobGroup jobGroup = this.getJobGroupInternal(job.getJobId());
		this.exceededJobExecutorPool.execute(new ReceiverExecutor(jobGroup.getTimeoutReceiver(), "timeoutReceiver", job, null, true));
	}

	private IJobGroup getJobGroupInternal(final String jobId) throws JobQueueException {

		Asserts.notNullOrEmptySimple(jobId, "jobId", JobQueueException.class);

		final IJobEntity jobEntity = this.persister.getJobEntity(jobId);
		Asserts.notNullSimple(jobEntity, "jobEntity", JobQueueException.class);
		return Asserts.notNull(this.jobGroups.get(jobEntity.getJobGroup()), "No JobGroup found for id='" + jobEntity.getJobGroup() + "'",
				JobQueueException.class);
	}

	private IJobGroup validateJobGroup(final IJobGroup jobGroup) {

		Asserts.notNullSimple(jobGroup.getName(), "name");
		Asserts.notNullSimple(jobGroup.getJobReceiver(), "jobReceiver");
		Asserts.notNullSimple(jobGroup.getFinishedReceiver(), "finishedReceiver");
		return jobGroup;
	}

	public void setNumReceiverThreads(final int numReceiverThreads) {
		this.numReceiverThreads = numReceiverThreads;
	}

	public void setNumFinishedReceiverThreads(final int numFinishedReceiverThreads) {
		this.numFinishedReceiverThreads = numFinishedReceiverThreads;
	}

	public void setNumTimeoutReceiverThreads(final int numTimeoutReceiverThreads) {
		this.numTimeoutReceiverThreads = numTimeoutReceiverThreads;
	}

	Map<String, IJobGroup> getJobGroups() {
		return this.jobGroups;
	}

	JobQueueServiceAccess getQueueServiceAccess() {
		return this.queueServiceAccess;
	}

	public static class JobScanner extends Thread {

		private final IJobPersister jobPersister;
		private final IJobSubmitter jobSubmitter;
		private final IJobSubmitter finishedSubmitter;
		private final IJobSubmitter timeoutSubmitter;

		private final Logger logger = LoggerFactory.getLogger(JobScanner.class);

		public JobScanner(final IJobPersister jobPersister, final IJobSubmitter jobSubmitter, final IJobSubmitter finishedSubmitter,
				final IJobSubmitter timeoutSubmitter) {

			this.jobPersister = Asserts.notNullSimple(jobPersister, "jobPersister");
			this.jobSubmitter = Asserts.notNullSimple(jobSubmitter, "jobSubmitter");
			this.finishedSubmitter = Asserts.notNullSimple(finishedSubmitter, "finishedSubmitter");
			this.timeoutSubmitter = Asserts.notNullSimple(timeoutSubmitter, "timeoutSubmitter");
		}

		@Override
		public void run() {

			while (true) {
				boolean noJobs = true;
				try {
					final Collection<IJobEntity> newJobs = jobPersister.getNewJobs();
					for (final IJobEntity jobEntity : newJobs) {
						this.jobSubmitter.submit(jobEntity);
						noJobs = false;
					}

					final Collection<IJobEntity> finishedJobs = jobPersister.getFinishedJobs();
					for (final IJobEntity jobEntity : finishedJobs) {
						this.finishedSubmitter.submit(jobEntity);
						noJobs = false;
					}

					final Collection<IJobEntity> timeoutJobs = jobPersister.getTimeoutJobs();
					for (final IJobEntity jobEntity : timeoutJobs) {
						this.timeoutSubmitter.submit(jobEntity);
						noJobs = false;
					}

				} catch (final Exception ex) {
					logger.warn("Getting jobs threw exception", ex);
				}
				if (noJobs) {
					try {
						Thread.sleep(SLEEP_INTERVAL);
					} catch (final InterruptedException iex) {
						logger.info("Received interrupt");
					}
				}
			}
		}
	}

	public final class ReceiverExecutor extends Thread {

		private final IJobReceiver jobReceiver;
		private final String receiverName;
		private final IJob job;
		private final Collection<IJobFinishedStrategy> finishedStrategies;
		private final boolean deleteAfterExecution;

		private final Logger logger = LoggerFactory.getLogger(ReceiverExecutor.class);

		public ReceiverExecutor(final IJobReceiver jobReceiver, final String receiverName, final IJob job,
				final Collection<IJobFinishedStrategy> finishedStrategies, final boolean deleteAfterExecution) {

			this.jobReceiver = Asserts.notNullSimple(jobReceiver, "jobReceiver");
			this.receiverName = Asserts.notNullOrEmptySimple(receiverName, "receiverName", IllegalStateException.class);
			this.job = Asserts.notNullSimple(job, "job");
			this.finishedStrategies = finishedStrategies;
			this.deleteAfterExecution = deleteAfterExecution;
		}

		@Override
		public void run() {
			logger.info("Executing receiver='{}'", this.receiverName);
			try {
				try {
					this.jobReceiver.receive(job);
				} catch (final Exception ex) {
					throw new JobQueueException(String.format("Receiver threw exception for job='%S'", job), ex);
				}
				if (this.deleteAfterExecution) {
					try {
						persister.delete(job.getJobId());
						return;
					} catch (final Exception ex) {
						throw new JobQueueException(String.format("Could not delete job='%s'", job), ex);
					}
				}
				try {
					if (this.finishedStrategies != null) {
						for (final IJobFinishedStrategy finishedStrategy : this.finishedStrategies) {
							if (finishedStrategy.finished(job)) {
								persister.setFinished(job.getJobId());
								return;
							}
						}
					}
					persister.resume(job.getJobId());
				} catch (final Exception ex) {
					throw new JobQueueException(String.format("Could not resume job='%s'", job), ex);
				}
			} catch (final JobQueueException ex) {
				try {
					persister.setError(job.getJobId());
				} catch (final Exception jex) {
					logger.error("Could not set state='ERROR' for job='{}'", job, jex);
				}
			}
		}
	}

	public interface IJobSubmitter {

		void submit(IJobEntity jobEntity) throws JobQueueException;

	}
}
