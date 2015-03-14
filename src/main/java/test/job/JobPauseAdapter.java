package test.job;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;

import test.flow.support.state.PauseAdapter;

/**
 * @author Dave Syer
 * 
 */
public class JobPauseAdapter implements PauseAdapter<JobFlowExecutor, ExitStatus> {

	private final ExitStatus status = new ExitStatus("PAUSED");

	public ExitStatus pause(JobFlowExecutor context) {
		// This state is just a toggle for the status of the job execution. If
		// not already paused we pause it.
		JobExecution jobExecution = context.getJobExecution();
		jobExecution.upgradeStatus(BatchStatus.STOPPED);
		jobExecution.setExitStatus(jobExecution.getExitStatus().and(status));
		return status;
	}

}