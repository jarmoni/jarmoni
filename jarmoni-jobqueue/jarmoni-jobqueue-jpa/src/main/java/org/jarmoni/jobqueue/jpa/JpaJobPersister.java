/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Dec 7, 2013
 */
package org.jarmoni.jobqueue.jpa;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.jarmoni.jobqueue.common.api.IJobEntity;
import org.jarmoni.jobqueue.common.api.IJobPersister;
import org.jarmoni.jobqueue.common.impl.JobQueueException;
import org.jarmoni.jobqueue.common.impl.JobState;
import org.jarmoni.util.Asserts;
import org.jarmoni.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class JpaJobPersister implements IJobPersister {

	private final EntityManagerFactory emf;
	private final Logger logger = LoggerFactory.getLogger(JpaJobPersister.class);

	public JpaJobPersister(final EntityManagerFactory emf) {

		this.emf = Asserts.notNullSimple(emf, "emf");
	}

	@Override
	public String insert(final Object jobObject, final String group, final Long timeout) throws JobQueueException {

		final IJobEntity jobEntity = JpaJobEntity.builder().jobObject(jobObject).currentTimeout(timeout).timeout(timeout).jobGroup(group)
				.jobState(JobState.NEW).lastUpdate(Calendar.getInstance().getTime()).build();
		EntityManager em = null;
		try {
			em = this.emf.createEntityManager();
			final EntityTransaction tx = em.getTransaction();
			tx.begin();
			em.persist(jobEntity);
			tx.commit();
			return jobEntity.getId();
		} catch (final Exception ex) {
			throw new JobQueueException(String.format("Persisting of JobEntity failed. JobObject='%s', JobGroup='%s'", jobObject, group), ex);
		} finally {
			this.closeEntityManager(em);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete(final String jobId) throws JobQueueException {

		try {
			this.executeQuery(JpaJobEntity.QUERY_DELETE_BY_ID, new Tuple<String, Object>("id", jobId));
		} catch (final Exception ex) {
			throw new JobQueueException(String.format("Deletion of JobEntity failed. ID='%s'", jobId), ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void pause(final String jobId) throws JobQueueException {

		try {
			this.executeQuery(JpaJobEntity.QUERY_PAUSE, new Tuple<String, Object>("id", jobId));
		} catch (final Exception ex) {
			throw new JobQueueException(String.format("Pausing of JobEntity failed. ID='%s'", jobId), ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void resume(final String jobId) throws JobQueueException {

		try {
			this.executeQuery(JpaJobEntity.QUERY_RESUME, new Tuple<String, Object>("id", jobId), new Tuple<String, Object>("lastUpdate", Calendar
					.getInstance().getTime()));
		} catch (final Exception ex) {
			throw new JobQueueException(String.format("Resuming of JobEntity failed. ID='%s'", jobId), ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(final String jobId, final Object jobObject) throws JobQueueException {

		try {
			this.executeQuery(JpaJobEntity.QUERY_UPDATE, new Tuple<String, Object>("id", jobId), new Tuple<String, Object>("lastUpdate", Calendar
					.getInstance().getTime()), new Tuple<String, Object>("jobBytes", JpaJobEntity.jobObjectToJobBytes(jobObject)));
		} catch (final Exception ex) {
			throw new JobQueueException(String.format("Pausing of JobEntity failed. ID='%s'", jobId), ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setFinished(final String jobId) throws JobQueueException {

		try {
			this.executeQuery(JpaJobEntity.QUERY_FINISH, new Tuple<String, Object>("id", jobId));
		} catch (final Exception ex) {
			throw new JobQueueException(String.format("Finishing of JobEntity failed. ID='%s'", jobId), ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setError(final String jobId) throws JobQueueException {

		try {
			this.executeQuery(JpaJobEntity.QUERY_ERROR, new Tuple<String, Object>("id", jobId));
		} catch (final Exception ex) {
			throw new JobQueueException(String.format("Finishing of JobEntity failed. ID='%s'", jobId), ex);
		}
	}

	@Override
	public IJobEntity getJobEntity(final String jobId) throws JobQueueException {

		EntityManager em = null;
		try {
			em = this.emf.createEntityManager();
			return em.find(JpaJobEntity.class, jobId);
		} catch (final Exception ex) {
			throw new JobQueueException(String.format("Could not get JobEntity. ID='%s'", jobId), ex);
		} finally {
			this.closeEntityManager(em);
		}
	}

	@Override
	public Collection<IJobEntity> getNewJobs() throws JobQueueException {

		try {
			return this.getEntitesForState(JobState.NEW, JobState.NEW_IN_PROGRESS);
		} catch (final Exception ex) {
			throw new JobQueueException("Could not obtain new jobs", ex);
		}
	}

	@Override
	public Collection<IJobEntity> getFinishedJobs() throws JobQueueException {

		try {
			return this.getEntitesForState(JobState.FINISHED, JobState.FINISHED_IN_PROGRESS);
		} catch (final Exception ex) {
			throw new JobQueueException("Could not obtain finished jobs", ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IJobEntity> getTimeoutJobs() throws JobQueueException {

		final List<IJobEntity> currentJobs = Lists.newArrayList();
		EntityManager em = null;
		try {
			em = this.emf.createEntityManager();
			final EntityTransaction tx = em.getTransaction();
			tx.begin();
			final Query query = em.createNamedQuery(JpaJobEntity.QUERY_SELECT_ALL);
			final List<IJobEntity> jobs = query.getResultList();
			for (final IJobEntity jobEntity : jobs) {
				if (JobState.PAUSED.equals(jobEntity.getJobState())) {
					continue;
				}
				if (jobEntity.getCurrentTimeout() == null) {
					continue;
				}
				if (JobState.EXCEEDED_IN_PROGRESS.equals(jobEntity.getJobState())) {
					continue;
				}
				if (jobEntity.getLastUpdate().getTime() + jobEntity.getCurrentTimeout() < System.currentTimeMillis()) {
					jobEntity.setJobState(JobState.EXCEEDED_IN_PROGRESS);
					em.merge(jobEntity);
					currentJobs.add(jobEntity);
				}
			}
			tx.commit();
			return currentJobs;
		} catch (final Exception ex) {
			throw new JobQueueException("Could not obtain exceeded jobs", ex);
		} finally {
			this.closeEntityManager(em);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void refresh() throws JobQueueException {

		EntityManager em = null;
		try {
			em = this.emf.createEntityManager();
			final EntityTransaction tx = em.getTransaction();
			tx.begin();
			final Query query = em.createNamedQuery(JpaJobEntity.QUERY_SELECT_ALL);
			final List<IJobEntity> jobs = query.getResultList();
			for (final IJobEntity jobEntity : jobs) {

				if (JobState.NEW_IN_PROGRESS.equals(jobEntity.getJobState()) || JobState.EXCEEDED_IN_PROGRESS.equals(jobEntity.getJobState())
						|| JobState.FINISHED_IN_PROGRESS.equals(jobEntity.getJobState())) {

					if (JobState.NEW_IN_PROGRESS.equals(jobEntity.getJobState())) {
						jobEntity.setJobState(JobState.NEW);
					} else if (JobState.EXCEEDED_IN_PROGRESS.equals(jobEntity.getJobState())) {
						jobEntity.setJobState(JobState.EXCEEDED);
					} else if (JobState.FINISHED_IN_PROGRESS.equals(jobEntity.getJobState())) {
						jobEntity.setJobState(JobState.FINISHED);
					}
					jobEntity.setLastUpdate(Calendar.getInstance().getTime());
					em.merge(jobEntity);

				}
			}
			tx.commit();
		} catch (final Exception ex) {
			throw new JobQueueException("Could not refresh jobs", ex);
		} finally {
			this.closeEntityManager(em);
		}

	}

	private void executeQuery(final String queryName, @SuppressWarnings("unchecked") final Tuple<String, Object>... parameters) throws Exception {
		EntityManager em = null;
		try {
			em = this.emf.createEntityManager();
			final EntityTransaction tx = em.getTransaction();
			tx.begin();
			final Query query = em.createNamedQuery(queryName);
			if (parameters != null) {
				for (final Tuple<String, Object> tuple : parameters) {
					query.setParameter(tuple.getFirst(), tuple.getSecond());
				}
			}
			query.executeUpdate();
			tx.commit();
		} finally {
			this.closeEntityManager(em);
		}
	}

	@SuppressWarnings("unchecked")
	private List<IJobEntity> getEntitesForState(final JobState state, final JobState newState) throws Exception {
		EntityManager em = null;
		try {
			em = this.emf.createEntityManager();
			final EntityTransaction tx = em.getTransaction();
			tx.begin();
			final Query query = em.createNamedQuery(JpaJobEntity.QUERY_SELECT_FOR_STATE);
			query.setParameter("jobState", state);
			final List<IJobEntity> results = query.getResultList();
			for (final IJobEntity jobEntity : results) {
				jobEntity.setJobState(newState);
				em.merge(jobEntity);
			}
			tx.commit();
			return results;
		} finally {
			this.closeEntityManager(em);
		}
	}

	private void closeEntityManager(final EntityManager em) {
		try {
			if (em != null && em.isOpen()) {
				em.close();
			}
		} catch (final Exception ex) {
			logger.error("Exception while closing EntityManager");
		}
	}
}
