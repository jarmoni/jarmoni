/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 18, 2013
 */
package org.jarmoni.jobqueue.common.api;

public interface IPersistenceProvider {

	String insert(IJobEntity jobEntity);

	void update(IJobEntity jobEntity);

	IJobEntity getJobEntity(String jobEntityId);

	void delete(String jobEntityId);
}
