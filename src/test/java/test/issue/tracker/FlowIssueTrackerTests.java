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
package test.issue.tracker;

import static org.junit.Assert.assertEquals;
import static test.issue.Action.APPROVED;
import static test.issue.Action.REJECTED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import test.flow.Flow;
import test.flow.support.FlowBuilder;
import test.flow.support.State;
import test.flow.support.StateLocator;
import test.issue.Action;
import test.issue.Issue;
import test.issue.Status;

/**
 * @author Dave Syer
 * 
 */
public class FlowIssueTrackerTests {

	private FlowIssueTracker tracker;
	private Flow<Issue, Action> flow;
	
	private State<Issue, Action> ACCEPTED = new IssueState(Status.ACCEPTED);
	private State<Issue, Action> CLOSED = new IssueState(Status.CLOSED);
	private State<Issue, Action> CREATED = new IssueState(Status.CREATED);
	private State<Issue, Action> RESOLVED = new IssueState(Status.RESOLVED);
	private State<Issue, Action> REVISED = new IssueState(Status.REVISED);
	private State<Issue, Action> UNCLEAR = new IssueState(Status.UNCLEAR);
	private State<Issue, Action> UNRESOLVED = new IssueState(Status.ACCEPTED);

	@Before
	public void setUp() throws Exception {

		FlowBuilder<Issue, Action> builder = new FlowBuilder<Issue, Action>("issues");

		builder.from(CREATED).on(APPROVED).to(ACCEPTED).on(APPROVED).to(RESOLVED).on(APPROVED).to(CLOSED);
		builder.from(CREATED).on(REJECTED).to(UNCLEAR).on(APPROVED).to(REVISED).on(APPROVED).to(ACCEPTED);
		builder.from(ACCEPTED).on(REJECTED).to(UNCLEAR);
		builder.from(RESOLVED).on(REJECTED).to(UNRESOLVED).on(APPROVED).to(RESOLVED);
		builder.from(REVISED).on(REJECTED).to(UNCLEAR);

		flow = builder.build();
		tracker = new FlowIssueTracker(flow);

	}
	
	@Test
	public void testFlowStructure() throws Exception {
		@SuppressWarnings("unchecked")
		StateLocator<Issue, Action> locator = (StateLocator<Issue, Action>) flow;
		assertEquals("[APPROVED, REJECTED]", sort(locator.getTriggers(ACCEPTED.getName())).toString());
		assertEquals("[APPROVED]", sort(locator.getTriggers(UNCLEAR.getName())).toString());		
	}

	@Test
	public void testOpen() throws Throwable {
		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		assertEquals(Status.CREATED, issue.getStatus());
	}

	@Test
	public void testAccepted() throws Throwable {
		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.approve(issue);
		assertEquals(Status.ACCEPTED, issue.getStatus());
	}

	@Test
	public void testUnAccepted() throws Throwable {
		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.approve(issue);
		tracker.reject(issue);
		assertEquals(Status.UNCLEAR, issue.getStatus());
	}

	@Test(expected = IllegalStateException.class)
	public void testNotResolved() throws Throwable {
		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.reject(issue);
		tracker.reject(issue); // Barf! You can't reject an unclear issue
		assertEquals(Status.UNCLEAR, issue.getStatus());
	}

	@Test
	public void testResolved() throws Throwable {
		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.approve(issue);
		tracker.approve(issue);
		assertEquals(Status.RESOLVED, issue.getStatus());
	}

	@Test
	public void testClosed() throws Throwable {
		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.approve(issue);
		tracker.approve(issue);
		tracker.approve(issue);
		assertEquals(Status.CLOSED, issue.getStatus());
	}

	@Test
	public void testUnclear() throws Throwable {
		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.reject(issue);
		assertEquals(Status.UNCLEAR, issue.getStatus());
	}

	@Test
	public void testUnclearRevised() throws Throwable {
		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.reject(issue);
		tracker.approve(issue);
		assertEquals(Status.REVISED, issue.getStatus());
	}

	@Test
	public void testUnclearRevisedAccepted() throws Throwable {
		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.reject(issue);
		tracker.approve(issue);
		tracker.approve(issue);
		assertEquals(Status.ACCEPTED, issue.getStatus());
	}

	@Test(expected=IllegalStateException.class)
	public void testBadTriggerNoPathOnReject() throws Throwable {

		FlowBuilder<Issue, Action> builder = new FlowBuilder<Issue, Action>("issues");

		builder.from(CREATED).on(APPROVED).to(CLOSED);

		flow = builder.build();
		tracker = new FlowIssueTracker(flow);

		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.reject(issue);

	}

	@Test(expected=IllegalStateException.class)
	public void testBadTriggerNoPathOnApprove() throws Throwable {

		FlowBuilder<Issue, Action> builder = new FlowBuilder<Issue, Action>("issues");

		builder.from(CREATED).on(REJECTED).to(CLOSED);

		flow = builder.build();
		tracker = new FlowIssueTracker(flow);

		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);
		tracker.approve(issue);

	}

	@Test(expected=IllegalStateException.class)
	public void testBadTriggerNoPathOnOpen() throws Throwable {

		FlowBuilder<Issue, Action> builder = new FlowBuilder<Issue, Action>("issues");

		builder.from(new IssueState(Status.CREATED) {
			public Action handle(Issue issue) throws Exception {
				throw new RuntimeException("Expected!");
			}
		}).on(APPROVED).to(CLOSED);

		flow = builder.build();
		tracker = new FlowIssueTracker(flow);

		Issue issue = new Issue(123L, "Not working");
		tracker.open(issue);

	}

	private List<String> sort(Collection<String> values) {
		List<String> names = new ArrayList<String>(values);
		Collections.sort(names);
		return names;
	}

}
