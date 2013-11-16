/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.api;

import org.jarmoni.jobqueue.impl.JobQueueException;

public interface IJobFinishedStrategy {

	boolean finished(IJob job) throws JobQueueException;

}
