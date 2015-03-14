package test.job;

import org.springframework.batch.core.ExitStatus;

import test.flow.support.state.AbstractState;

/**
 * @author Dave Syer
 * 
 */
public class DecisionState extends AbstractState<JobFlowExecutor, ExitStatus> {

	private final JobExecutionDecider decider;

	/**
	 * @param name
	 */
	DecisionState(String name, JobExecutionDecider decider) {
		super(name);
		this.decider = decider;
	}

	public ExitStatus handle(JobFlowExecutor context) {
		return decider.decide(context.getJobExecution());
	}

}