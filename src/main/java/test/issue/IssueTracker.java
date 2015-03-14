package test.issue;

public interface IssueTracker {
	
	void open(Issue issue);

	void approve(Issue issue);

	void reject(Issue issue);

}
