/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 17, 2013
 */
package org.jarmoni.jobqueue.common.api;

import java.util.Date;

import org.jarmoni.jobqueue.common.impl.JobState;

public interface IJobEntity {

	String getId();

	Date getLastUpdate();

	void setLastUpdate(Date lastUpdate);

	Long getTimeout();

	void setTimeout(Long timeout);

	Long getCurrentTimeout();

	void setCurrentTimeout(Long timeout);

	JobState getJobState();

	void setJobState(JobState jobState);

	Object getJobObject();

	void setJobObject(Object jobObject);

	String getJobGroup();

	void setJobGroup(String jobGroup);
}
