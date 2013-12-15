/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.impl;

public enum JobState {
	NEW, NEW_IN_PROGRESS, PAUSED, FINISHED, FINISHED_IN_PROGRESS, EXCEEDED, EXCEEDED_IN_PROGRESS, ERROR, ERROR_IN_PROGRESS
}
