package test.issue.tracker;

import test.flow.support.state.AbstractState;
import test.issue.Action;
import test.issue.Issue;
import test.issue.Status;

public class IssueState extends AbstractState<Issue, Action> {

	private final Status status;

	public IssueState(Status status) {
		super(status.name(), true);
		this.status = status;
	}

	@Override
	public Action handle(Issue issue) throws Exception {
		issue.setStatus(status);
		return null;
	}

}
