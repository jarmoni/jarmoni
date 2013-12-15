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
import org.jarmoni.jobqueue.common.api.IJobGroup;
import org.jarmoni.jobqueue.common.api.IJobPersister;
import org.jarmoni.jobqueue.common.api.IJobReceiver;
import org.jarmoni.jobqueue.common.api.IJobQueueService;
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
	private ExecutorService newJobScannerPool;
	private ExecutorService finishedJobScannerPool;
	private ExecutorService exceededJobScannerPool;

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
		this.newJobScannerPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("new-job-scanner-%d").build());
		this.finishedJobScannerPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("finished-job-scanner-%d").build());
		this.exceededJobScannerPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("exceeded-job-scanner-%d").build());
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

		this.newJobScannerPool.execute(new JobScanner(new IJobSubmitter() {

			@Override
			public void submit(final IJobEntity jobEntity) throws JobQueueException {
				processNewJob(jobEntity);

			}

			@Override
			public Collection<IJobEntity> getJobs() throws JobQueueException {
				return persister.getNewJobs();
			}
		}));

		this.exceededJobScannerPool.execute(new JobScanner(new IJobSubmitter() {

			@Override
			public void submit(final IJobEntity jobEntity) throws JobQueueException {
				processExceededJob(jobEntity);
			}

			@Override
			public Collection<IJobEntity> getJobs() throws JobQueueException {
				return persister.getExceededJobs();
			}
		}));

		this.finishedJobExecutorPool.execute(new JobScanner(new IJobSubmitter() {

			@Override
			public void submit(final IJobEntity jobEntity) throws JobQueueException {
				processFinishedJob(jobEntity);

			}

			@Override
			public Collection<IJobEntity> getJobs() throws JobQueueException {
				return persister.getFinishedJobs();
			}
		}));

	}

	/**
	 * lifecycle-method
	 */
	public void stop() {

		this.logger.info("JobQueueService#stop()");

		this.newJobScannerPool.shutdownNow();
		this.finishedJobScannerPool.shutdownNow();
		this.exceededJobScannerPool.shutdownNow();
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
		this.newJobExecutorPool.execute(new ReceiverExecutor(jobGroup.getJobReceiver(), job, false));
	}

	private void processFinishedJob(final IJobEntity jobEntity) throws JobQueueException {

		final IJob job = new Job(jobEntity.getId(), jobEntity.getJobGroup(), jobEntity.getJobObject(), this.queueServiceAccess);

		final IJobGroup jobGroup = this.getJobGroupInternal(job.getJobId());
		this.finishedJobExecutorPool.execute(new ReceiverExecutor(jobGroup.getFinishedReceiver(), job, true));
	}

	private void processExceededJob(final IJobEntity jobEntity) throws JobQueueException {

		final IJob job = new Job(jobEntity.getId(), jobEntity.getJobGroup(), jobEntity.getJobObject(), this.queueServiceAccess);

		final IJobGroup jobGroup = this.getJobGroupInternal(job.getJobId());
		this.exceededJobExecutorPool.execute(new ReceiverExecutor(jobGroup.getTimeoutReceiver(), job, true));
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

		private final IJobSubmitter jobSubmitter;

		private final Logger logger = LoggerFactory.getLogger(JobScanner.class);

		public JobScanner(final IJobSubmitter jobSubmitter) {
			this.jobSubmitter = Asserts.notNullSimple(jobSubmitter, "jobSubmitter");
		}

		@Override
		public void run() {
			while (true) {
				try {
					final Collection<IJobEntity> jobEntities = this.jobSubmitter.getJobs();
					if (!jobEntities.isEmpty()) {
						for (final IJobEntity jobEntity : jobEntities) {
							this.jobSubmitter.submit(jobEntity);
						}
					} else {
						Thread.sleep(SLEEP_INTERVAL);
					}
				} catch (final InterruptedException e) {
					logger.info("Received interrupt");
				} catch (final Exception e) {
					logger.error("Exception while pushing exceeded jobs", e);
				}
			}
		}
	}

	public final class ReceiverExecutor extends Thread {

		private final IJobReceiver jobReceiver;
		private final IJob job;
		private final boolean deleteAfterExecution;

		private final Logger logger = LoggerFactory.getLogger(ReceiverExecutor.class);

		public ReceiverExecutor(final IJobReceiver jobReceiver, final IJob job, final boolean deleteAfterExecution) {
			this.jobReceiver = Asserts.notNullSimple(jobReceiver, "jobReceiver");
			this.job = Asserts.notNullSimple(job, "job");
			this.deleteAfterExecution = deleteAfterExecution;
		}

		@Override
		public void run() {
			try {
				this.jobReceiver.receive(job);
				if (this.deleteAfterExecution) {
					try {
						persister.delete(job.getJobId());
					} catch (final JobQueueException ex) {
						logger.error("Could not delete job", ex);
					}
				}
			} catch (final Exception ex) {
				logger.error("Receiver threw exception", ex);
			}
		}
	}

	public interface IJobSubmitter {

		Collection<IJobEntity> getJobs() throws JobQueueException;

		void submit(IJobEntity jobEntity) throws JobQueueException;

	}
}
