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
package test.flow.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import test.flow.FlowExecutionException;
import test.flow.FlowResult;
import test.flow.support.state.AbstractState;
import test.flow.support.state.PauseAdapter;
import test.flow.support.state.PauseState;

/**
 * @author Dave Syer
 * 
 */
public class SimpleFlowTests {

	private SimpleFlow<String, String> flow = new SimpleFlow<String, String>("job");

	private String executor = "data";

	@Test(expected = FlowDefinitionException.class)
	public void testEmptySteps() throws Exception {
		flow.setTransitions(Collections.<Transition<String, String>> emptyList());
		flow.afterPropertiesSet();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoNextStepSpecified() throws Exception {
		flow.setTransitions(Collections.singletonList(Transition.create(new StubState("step"), "foo")));
		flow.afterPropertiesSet();
	}

	@Test
	public void testNoStartStep() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step"), "FAILED", "step"),
				Transition.createEnd(new StubState("step"))));
		flow.afterPropertiesSet();
		assertTrue(flow.getTriggers("step").contains("FAILED"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoEndStep() throws Exception {
		flow.setTransitions(Collections.singletonList(Transition.create(new StubState("step"), "FAILED", "step")));
		flow.afterPropertiesSet();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMultipleStartSteps() throws Exception {
		flow.setTransitions(collect(Transition.createEnd(new StubState("step1")), Transition.createEnd(new StubState(
				"step2"))));
		flow.afterPropertiesSet();
	}

	@Test
	public void testNoMatchForNextStep() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "FOO", "step2"), Transition
				.createEnd(new StubState("step2"))));
		flow.afterPropertiesSet();
		try {
			flow.start(executor);
			fail("Expected FlowExecutionException");
		}
		catch (FlowExecutionException e) {
			// expected
			String message = e.getMessage();
			assertTrue("Wrong message: " + message, message.toLowerCase().contains("next state not found"));
		}
	}

	@Test
	public void testEmptyTriggersOnEnd() throws Exception {
		flow.setTransitions(Collections.singletonList(Transition.createEnd(new StubState("step1"))));
		flow.afterPropertiesSet();
		assertEquals("[COMPLETED]", flow.getTriggers("step1").toString());
	}

	@Test
	public void testGetState() throws Exception {
		flow.setTransitions(Collections.singletonList(Transition.createEnd(new StubState("step1"))));
		flow.afterPropertiesSet();
		assertEquals("step1", flow.getState("step1").getName());
	}

	@Test
	public void testOneStep() throws Exception {
		flow.setTransitions(Collections.singletonList(Transition.createEnd(new StubState("step1"))));
		// flow.afterPropertiesSet(); // test lazy init by commenting this out
		FlowResult<String, String> execution = flow.start(executor);
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("step1", execution.getMemento());
	}

	@Test(expected = FlowExecutionException.class)
	public void testOneStepWrongInstruction() throws Exception {
		flow.setTransitions(Collections.singletonList(Transition.createEnd(new StubState("step1"))));
		FlowResult<String, String> execution = flow.resume("step2", executor, "COMPLETED");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("step1", execution.getMemento());
	}

	@Test
	public void testExplicitStartStep() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step"), "FAILED", "step"),
				Transition.createEnd(new StubState("step"))));
		flow.afterPropertiesSet();
		FlowResult<String, String> execution = flow.start(executor);
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("step", execution.getMemento());
	}

	@Test
	public void testTwoSteps() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "step2"), Transition
				.createEnd(new StubState("step2"))));
		flow.afterPropertiesSet();
		FlowResult<String, String> execution = flow.start(executor);
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("step2", execution.getMemento());
	}

	@Test
	public void testTriggers() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "step2"), Transition
				.createEnd(new StubState("step2"))));
		flow.afterPropertiesSet();
		assertEquals("[COMPLETED]", flow.getTriggers("step1").toString());
	}

	@Test
	public void testAnythingTriggers() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "step2"), Transition
				.createEnd(new StubState("step2"))));
		String wildcard = flow.getConcretePatternForWildcard(new HashSet<String>(Arrays.asList("COMPLETED", "FAILED",
				"UNKNOWN", "*")));
		assertEquals("ANYTHING", wildcard);
	}

	@Test
	public void testMultipleAnythingTriggers() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "step2"), Transition
				.createEnd(new StubState("step2"))));
		String wildcard = flow.getConcretePatternForWildcard(new HashSet<String>(Arrays.asList("COMPLETED", "FAILED",
				"UNKNOWN", "ANYTHING")));
		assertEquals("ANYTHING0", wildcard);
	}

	@Test
	public void testMoreAnythingTriggers() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "step2"), Transition
				.createEnd(new StubState("step2"))));
		String wildcard = flow.getConcretePatternForWildcard(new HashSet<String>(Arrays.asList("COMPLETED", "FAILED",
				"UNKNOWN", "ANYTHING", "ANYTHING0")));
		assertEquals("ANYTHING1", wildcard);
	}

	@Test(expected = FlowDefinitionException.class)
	public void testNoTriggers() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "step2"), Transition
				.createEnd(new StubState("step2"))));
		flow.afterPropertiesSet();
		assertEquals("[*]", flow.getTriggers("foo").toString());
	}

	@Test
	public void testResume() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "step2"), Transition
				.createEnd(new StubState("step2"))));
		flow.afterPropertiesSet();
		FlowResult<String, String> execution = flow.resume("step1", executor, "COMPLETED");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("step2", execution.getMemento());
	}

	@Test
	public void testFailedStep() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1") {
			public String handle(String context) throws Exception {
				return "FAILED";
			}
		}, "step2"), Transition.createEnd(new StubState("step2"))));
		flow.afterPropertiesSet();
		FlowResult<String, String> execution = flow.start(executor);
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("step2", execution.getMemento());
	}

	@Test
	public void testBranching() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "step2"), Transition.create(
				new StubState("step1"), "COMPLETED", "step3"), Transition.createEnd(new StubState(
				"step2")), Transition.createEnd(new StubState("step3"))));
		flow.afterPropertiesSet();
		FlowResult<String, String> execution = flow.start(executor);
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("step3", execution.getMemento());
	}

	@Test
	public void testPause() throws Exception {
		flow.setTransitions(collect(Transition.create(new StubState("step1"), "step2"), Transition.create(
				new PauseState<String, String>("step2", new PauseAdapter<String,String>() {
					public String pause(String context) {
						return "COMPLETED";
					}
				}), "step3"), Transition.createEnd(new StubState("step3"))));
		flow.afterPropertiesSet();
		FlowResult<String, String> execution = flow.start(executor);
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("step2", execution.getMemento());
		execution = flow.resume(execution.getMemento(), executor, "COMPLETED");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("step3", execution.getMemento());
	}

	private List<Transition<String, String>> collect(Transition<String, String> s1, Transition<String, String> s2) {
		List<Transition<String, String>> list = new ArrayList<Transition<String, String>>();
		list.add(s1);
		list.add(s2);
		return list;
	}

	private List<Transition<String, String>> collect(Transition<String, String> s1, Transition<String, String> s2,
			Transition<String, String> s3) {
		List<Transition<String, String>> list = collect(s1, s2);
		list.add(s3);
		return list;
	}

	private List<Transition<String, String>> collect(Transition<String, String> s1, Transition<String, String> s2,
			Transition<String, String> s3, Transition<String, String> s4) {
		List<Transition<String, String>> list = collect(s1, s2, s3);
		list.add(s4);
		return list;
	}

	/**
	 * @author Dave Syer
	 * 
	 */
	private static class StubState extends AbstractState<String, String> {

		private final String result;

		public StubState(String name, String result) {
			super(name);
			this.result = result;
		}

		public StubState(String name) {
			this(name, "COMPLETED");
		}
		
		public String handle(String context) throws Exception {
			return result;
		}

	}

}
