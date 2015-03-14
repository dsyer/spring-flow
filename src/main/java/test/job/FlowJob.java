/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.job;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StartLimitExceededException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.repository.JobRestartException;

import test.flow.Flow;
import test.flow.FlowExecutionException;
import test.flow.FlowResult;
import test.flow.support.State;
import test.flow.support.StateLocator;

/**
 * @author Dave Syer
 * 
 */
public class FlowJob extends AbstractJob {

	private static final String LAST_STATE_NAME = FlowJob.class.getName() + ".LAST_STATE_NAME";

	private Flow<JobFlowExecutor, ExitStatus> flow;

	/**
	 * Public setter for the flow.
	 * @param flow the flow to set
	 */
	public void setFlow(Flow<JobFlowExecutor, ExitStatus> flow) {
		this.flow = flow;
	}

	/**
	 * @see AbstractJob#doExecute(JobExecution)
	 */
	@Override
	protected void doExecute(final JobExecution execution) throws JobExecutionException {
		try {
			JobFlowExecutor executor = new SimpleJobFlowExecutor(execution);
			if (execution.getExecutionContext().containsKey(LAST_STATE_NAME)) {
				Object memento = execution.getExecutionContext().get(LAST_STATE_NAME);
				flow.resume(memento, executor, execution.getExitStatus());
			}
			else {
				FlowResult<JobFlowExecutor, ExitStatus> flowExecution = flow.start(executor);
				execution.getExecutionContext().put(LAST_STATE_NAME, flowExecution.getMemento());
			}
		}
		catch (FlowExecutionException e) {
			throw new JobExecutionException("Flow execution ended unexpectedly", e);
		}
	}

	public Step getStep(String stepName) {
		if (flow instanceof StateLocator) {
			@SuppressWarnings("unchecked")
			StateLocator<JobFlowExecutor, ExitStatus> stateLocator = (StateLocator<JobFlowExecutor, ExitStatus>) flow;
			for (String name : stateLocator.getStateNames()) {
				State<JobFlowExecutor, ExitStatus> state = stateLocator.getState(name);
				if (state instanceof StepState && stepName.equals(state.getName())) {
					return ((StepState) state).getStep();
				}
			}
		}
		return null;
	}

	public Collection<String> getStepNames() {
		Collection<String> names = new HashSet<String>();
		if (flow instanceof StateLocator) {
			@SuppressWarnings("unchecked")
			StateLocator<JobFlowExecutor, ExitStatus> stateLocator = (StateLocator<JobFlowExecutor, ExitStatus>) flow;
			for (String name : stateLocator.getStateNames()) {
				State<JobFlowExecutor, ExitStatus> state = stateLocator.getState(name);
				if (state instanceof StepState) {
					names.add(state.getName());
				}
			}
		}
		return names;
	}

	private final class SimpleJobFlowExecutor implements JobFlowExecutor {

		private final JobExecution execution;

		private SimpleJobFlowExecutor(JobExecution execution) {
			this.execution = execution;
		}

		public ExitStatus executeStep(Step step) throws JobInterruptedException, JobRestartException,
				StartLimitExceededException {
			StepExecution stepExecution = handleStep(step, execution);
			return stepExecution == null ? ExitStatus.COMPLETED : stepExecution.getExitStatus();
		}

		public JobExecution getJobExecution() {
			return execution;
		}

		public void updateJobExecutionStatus(ExitStatus status) {
			execution.setStatus(findBatchStatus(status.getExitCode()));
			execution.setExitStatus(status);
		}

		private BatchStatus findBatchStatus(String status) {
			for (BatchStatus batchStatus : BatchStatus.values()) {
				if (status.startsWith(batchStatus.name())) {
					return batchStatus;
				}
			}
			return BatchStatus.UNKNOWN;
		}

	}

}
