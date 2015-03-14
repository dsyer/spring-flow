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

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;

import test.flow.support.FlowBuilder;
import test.flow.support.State;
import test.flow.support.state.PauseState;

/**
 * @author Dave Syer
 * 
 */
public class FlowJobTests {

	private JobExecution jobExecution;

	private JobRepository jobRepository;

	@Before
	public void setUp() throws Exception {
		MapJobRepositoryFactoryBean.clear();
		MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean();
		factory.setTransactionManager(new ResourcelessTransactionManager());
		factory.afterPropertiesSet();
		jobRepository = (JobRepository) factory.getObject();
		jobExecution = jobRepository.createJobExecution("job", new JobParameters());
	}

	@Test
	public void testStepLocator() throws Throwable {

		FlowJob job = new FlowJob();
		job.setJobRepository(jobRepository);

		State<JobFlowExecutor, ExitStatus> step = new StepState(new StubStep("step"));
		EndState end = new EndState("end", ExitStatus.COMPLETED);

		FlowBuilder<JobFlowExecutor, ExitStatus> builder = new FlowBuilder<JobFlowExecutor, ExitStatus>("job");
		builder.from(step).end(end);
		job.setFlow(builder.build());

		assertEquals("[step]", job.getStepNames().toString());
		assertEquals("step", job.getStep("step").getName());

	}

	@Test
	public void testBasicFlow() throws Throwable {

		FlowJob job = new FlowJob();
		job.setJobRepository(jobRepository);

		State<JobFlowExecutor, ExitStatus> step = new StepState(new StubStep("step"));
		EndState end = new EndState("end", ExitStatus.COMPLETED);

		FlowBuilder<JobFlowExecutor, ExitStatus> builder = new FlowBuilder<JobFlowExecutor, ExitStatus>("job");
		builder.from(step).end(end);
		job.setFlow(builder.build());

		job.execute(jobExecution);
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			throw jobExecution.getAllFailureExceptions().get(0);
		}

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

	}

	@Test
	public void testDecisionFlow() throws Throwable {
		FlowJob job = new FlowJob();
		job.setJobRepository(jobRepository);

		JobExecutionDecider decider = new JobExecutionDecider() {
			public ExitStatus decide(JobExecution jobExecution) {
				return new ExitStatus("SWITCH");
			}
		};

		State<JobFlowExecutor, ExitStatus> step1 = new StepState(new StubStep("step1"));
		State<JobFlowExecutor, ExitStatus> decision = new DecisionState("decision", decider);
		State<JobFlowExecutor, ExitStatus> step2 = new StepState(new StubStep("step2"));
		State<JobFlowExecutor, ExitStatus> step3 = new StepState(new StubStep("step3"));
		EndState end = new EndState("end", ExitStatus.COMPLETED);

		FlowBuilder<JobFlowExecutor, ExitStatus> builder = new FlowBuilder<JobFlowExecutor, ExitStatus>("job");

		// Sunny day straight through
		builder.from(step1).to(decision).to(step2).end(end);
		// Alternate path on decision outcome
		builder.from(decision).on(new ExitStatus("SWITCH")).to(step3).end(end);

		job.setFlow(builder.build());

		job.doExecute(jobExecution);
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			throw jobExecution.getAllFailureExceptions().get(0);
		}

		assertEquals(2, jobExecution.getStepExecutions().size());
		Iterator<StepExecution> iterator = jobExecution.getStepExecutions().iterator();
		iterator.next();
		StepExecution stepExecution = iterator.next();
		assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
		assertEquals("step3", stepExecution.getStepName());

	}

	@Test
	public void testPauseFlow() throws Throwable {

		FlowJob job = new FlowJob();
		job.setJobRepository(jobRepository);

		State<JobFlowExecutor, ExitStatus> step1 = new StepState(new StubStep("step1"));
		PauseState<JobFlowExecutor, ExitStatus> pause = new PauseState<JobFlowExecutor, ExitStatus>("pause",
				new JobPauseAdapter());
		State<JobFlowExecutor, ExitStatus> step2 = new StepState(new StubStep("step2"));
		EndState end = new EndState("end", ExitStatus.COMPLETED);

		FlowBuilder<JobFlowExecutor, ExitStatus> builder = new FlowBuilder<JobFlowExecutor, ExitStatus>("job");

		// Straight through with pause in the middle
		builder.from(step1).to(pause).to(step2).end(end);

		job.setFlow(builder.build());

		job.execute(jobExecution);
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			throw jobExecution.getAllFailureExceptions().get(0);
		}
		assertEquals(BatchStatus.STOPPED, jobExecution.getStatus());
		assertEquals(1, jobExecution.getStepExecutions().size());

		job.execute(jobExecution);
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			throw jobExecution.getAllFailureExceptions().get(0);
		}
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		assertEquals(2, jobExecution.getStepExecutions().size());

	}

	/**
	 * @author Dave Syer
	 * 
	 */
	private final class StubStep extends StepSupport {

		private StubStep(String name) {
			super(name);
		}

		public void execute(StepExecution stepExecution) throws JobInterruptedException {
			stepExecution.setStatus(BatchStatus.COMPLETED);
			stepExecution.setExitStatus(ExitStatus.COMPLETED);
			jobRepository.update(stepExecution);
		}

	}

}
