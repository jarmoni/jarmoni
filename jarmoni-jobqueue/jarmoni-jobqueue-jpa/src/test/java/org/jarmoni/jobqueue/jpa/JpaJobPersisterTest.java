/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Dec 9, 2013
 */
package org.jarmoni.jobqueue.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.jarmoni.jobqueue.common.api.IJobEntity;
import org.jarmoni.jobqueue.common.api.IJobPersister;
import org.jarmoni.jobqueue.common.impl.JobState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JpaJobPersisterTest {

	private ClassPathXmlApplicationContext ctx;

	@Before
	public void setUp() throws Exception {
		this.ctx = new ClassPathXmlApplicationContext("/org/jarmoni/jobqueue/jpa/database.xml");
	}

	@After
	public void tearDown() throws Exception {
		if (this.ctx != null) {
			this.ctx.close();
		}
	}

	@Test
	public void testInsert() throws Exception {

		final String id = this.insertObject();
		final IJobPersister persister = this.getPersister();

		final IJobEntity entity = persister.getJobEntity(id);
		assertNotNull(entity);
		assertEquals(id, entity.getId());
		assertEquals(entity.getTimeout(), entity.getCurrentTimeout());
		assertNotNull(entity.getLastUpdate());
		assertEquals(JobState.NEW, entity.getJobState());
		final TestObject testObject2 = (TestObject) entity.getJobObject();
		assertEquals("foo", testObject2.getName());
		assertEquals("bar", testObject2.getDescription());
		assertEquals("foobar", entity.getJobGroup());
	}

	@Test
	public void testDelete() throws Exception {

		final String id = this.insertObject();
		final IJobPersister persister = this.getPersister();

		persister.delete(id);

		final IJobEntity entity = persister.getJobEntity(id);
		assertNull(entity);
	}

	@Test
	public void testDeleteJobNotExists() throws Exception {

		final IJobPersister persister = this.getPersister();

		persister.delete("123");
	}

	@Test
	public void testPause() throws Exception {

		final String id = this.insertObject();
		final IJobPersister persister = this.getPersister();

		persister.pause(id);

		final IJobEntity entity = persister.getJobEntity(id);
		assertNotNull(entity);
		assertEquals(id, entity.getId());
		assertNotNull(entity.getTimeout());
		assertNull(entity.getCurrentTimeout());
		assertNotNull(entity.getLastUpdate());
		assertEquals(JobState.PAUSED, entity.getJobState());
		final TestObject testObject = (TestObject) entity.getJobObject();
		assertEquals("foo", testObject.getName());
		assertEquals("bar", testObject.getDescription());
		assertEquals("foobar", entity.getJobGroup());
	}

	@Test
	public void testPauseJobNotExists() throws Exception {

		final IJobPersister persister = this.getPersister();

		persister.pause("123");
	}

	@Test
	public void testResume() throws Exception {

		final String id = this.insertObject();
		final IJobPersister persister = this.getPersister();

		persister.pause(id);

		final IJobEntity jobEntity = persister.getJobEntity(id);
		final Date lastUpdate = jobEntity.getLastUpdate();

		Thread.sleep(1L);
		persister.resume(id);

		final IJobEntity entity2 = persister.getJobEntity(id);
		assertNotNull(entity2);
		assertEquals(id, entity2.getId());
		assertEquals(entity2.getTimeout(), entity2.getCurrentTimeout());
		assertNotNull(entity2.getLastUpdate());
		assertEquals(JobState.NEW, entity2.getJobState());
		assertTrue(lastUpdate.getTime() < entity2.getLastUpdate().getTime());
	}

	@Test
	public void testUpdate() throws Exception {

		final String id = this.insertObject();
		final IJobPersister persister = this.getPersister();

		final IJobEntity jobEntity = persister.getJobEntity(id);
		final Date lastUpdate = jobEntity.getLastUpdate();

		final Object newJobObject = new TestObject("foofoo", "barbar");

		Thread.sleep(1L);
		persister.update(id, newJobObject);

		final IJobEntity entity2 = persister.getJobEntity(id);
		assertNotNull(entity2);
		assertEquals(id, entity2.getId());
		assertEquals(JobState.NEW, entity2.getJobState());
		assertTrue(lastUpdate.getTime() < entity2.getLastUpdate().getTime());
	}

	@Test
	public void testSetFinished() throws Exception {

		final String id = this.insertObject();
		final IJobPersister persister = this.getPersister();

		persister.setFinished(id);

		final IJobEntity jobEntity = persister.getJobEntity(id);
		assertEquals(JobState.FINISHED, jobEntity.getJobState());
	}

	@Test
	public void testSetError() throws Exception {

		final String id = this.insertObject();
		final IJobPersister persister = this.getPersister();

		persister.setError(id);

		final IJobEntity jobEntity = persister.getJobEntity(id);
		assertEquals(JobState.ERROR, jobEntity.getJobState());
	}

	@Test
	public void testGetNewJobs() throws Exception {

		this.insertObject();

		final IJobPersister persister = this.getPersister();

		final Collection<IJobEntity> jobs = persister.getNewJobs();
		assertEquals(1, jobs.size());
		assertEquals(JobState.NEW_IN_PROGRESS, jobs.iterator().next().getJobState());
	}

	@Test
	public void testGetFinishedJobs() throws Exception {

		final String id = this.insertObject();

		final IJobPersister persister = this.getPersister();
		persister.setFinished(id);

		final Collection<IJobEntity> jobs = persister.getFinishedJobs();
		assertEquals(1, jobs.size());
		assertEquals(JobState.FINISHED_IN_PROGRESS, jobs.iterator().next().getJobState());
	}

	@Test
	public void testGetExceededJobs() throws Exception {

		this.insertObject();

		final IJobPersister persister = this.getPersister();
		Thread.sleep(1001L);

		final Collection<IJobEntity> jobs = persister.getTimeoutJobs();
		assertEquals(1, jobs.size());
		assertEquals(JobState.EXCEEDED_IN_PROGRESS, jobs.iterator().next().getJobState());
	}

	private IJobPersister getPersister() {
		return this.ctx.getBean(IJobPersister.class);
	}

	private String insertObject() throws Exception {
		return this.insertObject("foo");
	}

	private String insertObject(final String name) throws Exception {
		final IJobPersister persister = this.getPersister();
		final TestObject testObject = new TestObject(name, "bar");
		return persister.insert(testObject, "foobar", 1000L);
	}
}
