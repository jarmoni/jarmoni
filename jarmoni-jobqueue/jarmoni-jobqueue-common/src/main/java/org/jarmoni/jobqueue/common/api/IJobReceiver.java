/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.api;

public interface IJobReceiver {

	void receive(IJob job) throws Exception;
}
