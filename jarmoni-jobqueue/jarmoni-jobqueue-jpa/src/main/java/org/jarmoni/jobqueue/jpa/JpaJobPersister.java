/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Dec 7, 2013
 */
package org.jarmoni.jobqueue.jpa;

import java.util.Calendar;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.jarmoni.jobqueue.common.api.IJobEntity;
import org.jarmoni.jobqueue.common.api.IJobPersister;
import org.jarmoni.jobqueue.common.impl.JobQueueException;
import org.jarmoni.jobqueue.common.impl.JobState;
import org.jarmoni.util.Asserts;

public class JpaJobPersister implements IJobPersister {

	private final EntityManagerFactory emf;

	public JpaJobPersister(final EntityManagerFactory emf) {
		this.emf = Asserts.notNullSimple(emf, "emf");
	}

	@Override
	public String insert(final Object jobObject, final String group, final Long timeout) throws JobQueueException {
		final IJobEntity jobEntity = JpaJobEntity.builder().id("123").jobObject(jobObject).currentTimeout(timeout).timeout(timeout).jobGroup(group)
				.jobState(JobState.NEW).lastUpdate(Calendar.getInstance().getTime()).build();
		final EntityManager em = this.emf.createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			em.persist(jobEntity);
			tx.commit();
			return jobEntity.getId();
		} catch (final Exception ex) {
			throw new JobQueueException("Persisting of JobEntity failed", ex);
		}
	}

	@Override
	public void delete(final String jobId) throws JobQueueException {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause(final String jobId) throws JobQueueException {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume(final String jobId) throws JobQueueException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(final String jobId, final Object jobObject) throws JobQueueException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFinished(final String jobId) throws JobQueueException {
		// TODO Auto-generated method stub

	}

	@Override
	public IJobEntity getJobEntity(final String jobId) throws JobQueueException {
		return this.emf.createEntityManager().find(JpaJobEntity.class, jobId);
	}

	@Override
	public Collection<IJobEntity> getNewJobs() throws JobQueueException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IJobEntity> getFinishedJobs() throws JobQueueException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IJobEntity> getExceededJobs() throws JobQueueException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh() throws JobQueueException {
		// TODO Auto-generated method stub

	}
}
