/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.api;

import java.util.Collection;

public interface IJobGroup {

	String getName();

	Collection<IJobFinishedStrategy> getJobFinishedStrategies();

	void setJobFinishedStrategies(Collection<IJobFinishedStrategy> jobFinishedStrategies);

	IJobReceiver getJobReceiver();

	void setJobReceiver(IJobReceiver jobReceiver);

	IJobReceiver getFinishedReceiver();

	void setFinishedReceiver(IJobReceiver finishedReceiver);

	IJobReceiver getTimeoutReceiver();

	void setTimeoutReceiver(IJobReceiver timeoutReceiver);

	// int getNumJobReceiverThreads();
	//
	// void setNumJobReceiverThreads(final int numJobReceiverThreads);
	//
	// int getNumFinishedReceiverThreads();
	//
	// void setNumFinishedReceiverThreads(final int numFinishedReceiverThreads);
	//
	// int getNumTimeoutReceiverThreads();
	//
	// void setNumTimeoutReceiverThreads(final int numTimeoutReceiverThreads);

}
