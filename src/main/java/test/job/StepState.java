package test.job;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Step;

import test.flow.FlowResult;
import test.flow.support.state.AbstractState;

/**
 * @author Dave Syer
 * 
 */
public class StepState extends AbstractState<JobFlowExecutor, ExitStatus> {

	private final Step step;

	/**
	 * @param name
	 */
	StepState(Step step) {
		super(step.getName());
		this.step = step;
	}

	/**
	 * @return the {@link Step}
	 */
	public Step getStep() {
		return step;
	}

	/**
	 * @param execution the current {@link FlowResult} (ignored)
	 * @param context the job executor
	 * @return the exit code of the step converted to a {@link FlowEvent}
	 * @throws Exception
	 */
	@Override
	public ExitStatus handle(JobFlowExecutor context) throws Exception {
		return context.executeStep(step);
	}

}