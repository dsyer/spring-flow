package test.issue.tracker;

import test.flow.Flow;
import test.flow.FlowExecutionException;
import test.issue.Action;
import test.issue.Issue;
import test.issue.IssueTracker;

public class FlowIssueTracker implements IssueTracker {

	private Flow<Issue, Action> flow;

	public FlowIssueTracker(Flow<Issue, Action> flow) {
		super();
		this.flow = flow;
	}

	public void open(Issue issue) {
		try {
			// Could persist flow execution here.
			flow.start(issue);
		}
		catch (FlowExecutionException e) {
			throw new IllegalStateException("Could not open issue.", e);
		}
	}

	public void approve(Issue issue) {
		// In lieu of persisted flow execution, we use a naming convention: the
		// last state name is the same as the flow status:
		try {
			flow.resume(issue.getStatus().name(), issue, Action.APPROVED);
		}
		catch (FlowExecutionException e) {
			throw new IllegalStateException("Could not approve issue.", e);
		}
	}

	public void reject(Issue issue) {
		try {
			flow.resume(issue.getStatus().name(), issue, Action.REJECTED);
		}
		catch (FlowExecutionException e) {
			throw new IllegalStateException("Could not reject issue: "+issue, e);
		}
	}

}