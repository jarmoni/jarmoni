/*
 * Copyright (c) 2013. All rights reserved.
 * Original Author: ms
 * Creation Date: Nov 16, 2013
 */
package org.jarmoni.jobqueue.common.impl;

import java.util.Collection;

import org.jarmoni.jobqueue.common.api.IJobFinishedStrategy;
import org.jarmoni.jobqueue.common.api.IJobGroup;
import org.jarmoni.jobqueue.common.api.IJobReceiver;
import org.jarmoni.util.Asserts;

import com.google.common.collect.Lists;

public class JobGroup implements IJobGroup {

	private final String name;

	private IJobReceiver jobReceiver;

	private IJobReceiver finishedReceiver;

	private IJobReceiver timeoutReceiver;

	private int numJobReceiverThreads = 1;

	private int numFinishedReceiverThreads = 1;

	private int numTimeoutReceiverThreads = 1;

	private Collection<IJobFinishedStrategy> jobFinishedStrategies = Lists.newArrayList();

	public JobGroup(final String name) {
		this.name = Asserts.notNullSimple(name, "name");
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Collection<IJobFinishedStrategy> getJobFinishedStrategies() {
		return jobFinishedStrategies;
	}

	@Override
	public void setJobFinishedStrategies(final Collection<IJobFinishedStrategy> jobFinishedStrategies) {
		this.jobFinishedStrategies = jobFinishedStrategies;
	}

	@Override
	public IJobReceiver getJobReceiver() {
		return this.jobReceiver;
	}

	@Override
	public void setJobReceiver(final IJobReceiver jobReceiver) {
		this.jobReceiver = jobReceiver;
	}

	@Override
	public IJobReceiver getFinishedReceiver() {
		return this.finishedReceiver;
	}

	@Override
	public void setFinishedReceiver(final IJobReceiver finishedReceiver) {
		this.finishedReceiver = finishedReceiver;
	}

	@Override
	public IJobReceiver getTimeoutReceiver() {
		return this.timeoutReceiver;
	}

	@Override
	public void setTimeoutReceiver(final IJobReceiver timeoutReceiver) {
		this.timeoutReceiver = timeoutReceiver;
	}

	@Override
	public int getNumJobReceiverThreads() {
		return numJobReceiverThreads;
	}

	@Override
	public void setNumJobReceiverThreads(final int numJobReceiverThreads) {
		this.numJobReceiverThreads = numJobReceiverThreads;
	}

	@Override
	public int getNumFinishedReceiverThreads() {
		return numFinishedReceiverThreads;
	}

	@Override
	public void setNumFinishedReceiverThreads(final int numFinishedReceiverThreads) {
		this.numFinishedReceiverThreads = numFinishedReceiverThreads;
	}

	@Override
	public int getNumTimeoutReceiverThreads() {
		return numTimeoutReceiverThreads;
	}

	@Override
	public void setNumTimeoutReceiverThreads(final int numTimeoutReceiverThreads) {
		this.numTimeoutReceiverThreads = numTimeoutReceiverThreads;
	}
}
