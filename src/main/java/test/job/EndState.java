package test.job;

import org.springframework.batch.core.ExitStatus;

import test.flow.support.state.AbstractState;

/**
 * @author Dave Syer
 * 
 */
public class EndState extends AbstractState<JobFlowExecutor, ExitStatus>  {
	
	private final ExitStatus status;
	
	public EndState(String name, ExitStatus status) {
		super(name);
		this.status = status;
	}

	public ExitStatus handle(JobFlowExecutor context) throws Exception {
		context.updateJobExecutionStatus(status);
		return status;
	}

}