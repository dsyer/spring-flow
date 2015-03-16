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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.core.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import test.flow.Flow;
import test.flow.FlowExecutionException;
import test.flow.FlowResult;

/**
 * A {@link Flow} implementation that branches conditionally depending on the exit status
 * of the last {@link State}. The input parameters to build a flow are the state
 * transitions (in no particular order). The start state can be specified explicitly (and
 * must exist in the set of transitions), or computed from the existing transitions, if
 * unambiguous. The {@link FlowResult} contains a memento for resuming a flow which is the
 * name of the last state handled.
 * 
 * @author Dave Syer
 * 
 */
public class SimpleFlow<T, S> implements Flow<T, S>, StateLocator<T, S> {

	private State<T, S> startState;

	private Map<String, SortedSet<Transition<T, S>>> transitionMap = new ConcurrentHashMap<String, SortedSet<Transition<T, S>>>();

	private Map<String, State<T, S>> stateMap = new ConcurrentHashMap<String, State<T, S>>();

	private Collection<Transition<T, S>> transitions = new HashSet<Transition<T, S>>();

	private final String name;

	/**
	 * @param string
	 */
	public SimpleFlow(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.flow.Flow#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * An unmodifiable copy of the names of states in this flow.
	 * 
	 * @see Flow#getStateNames()
	 */
	public Collection<String> getStateNames() {
		Collection<String> values = new HashSet<String>();
		for (State<T, S> state : stateMap.values()) {
			values.add(state.getName());
		}
		return Collections.unmodifiableCollection(values);
	}

	/**
	 * Public setter for the stateTransitions. If there is any ambiguity about the start
	 * state, the first state in the first transition is used.
	 * @param transitions the stateTransitions to set
	 */
	public void setTransitions(List<Transition<T, S>> transitions) {
		this.transitions = new LinkedHashSet<Transition<T, S>>(transitions);
	}

	/**
	 * Locate start step and pre-populate data structures needed for execution.
	 * 
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		initializeTransitions();
	}

	/**
	 * @see Flow#start(T)
	 */
	public FlowResult<T, S> start(T context) throws FlowExecutionException {
		if (startState == null) {
			initializeTransitions();
		}
		return handle(startState, context);
	}

	/**
	 * @see Flow#resume(T, S)
	 */
	public FlowResult<T, S> resume(Object memento, T context, S event)
			throws FlowExecutionException {

		if (startState == null) {
			initializeTransitions();
		}

		State<T, S> state = getState((String) memento);
		if (state == null) {
			throw new FlowExecutionException("No state with that name: [" + memento + "]");
		}
		state = nextState(state, event);

		if (state == null) {
			// There is no next state so return unmodified
			return new FlowResult<T, S>(memento, context, event);
		}

		return handle(state, context);

	}

	/**
	 * Get the state with the given name if it exists, otherwise null.
	 * 
	 * @see Flow#getState(String)
	 */
	public State<T, S> getState(String stateName) {
		return stateMap.get(stateName);
	}

	/**
	 * A collection of flow event names that trigger transitions from this state (not
	 * necessarily exhaustive, but sufficient for clients to explore the structure of the
	 * flow). A single wildcard ("*") is replaced with a suitable concrete alternative.
	 * 
	 * @see #getConcretePatternForWildcard(Collection)
	 * @see Flow#getTriggers(String)
	 */
	public Collection<String> getTriggers(String stateName)
			throws FlowDefinitionException {

		Set<Transition<T, S>> set = transitionMap.get(stateName);

		if (set == null) {
			throw new FlowDefinitionException(String.format(
					"No transitions found in flow=%s for state=%s", getName(), stateName));
		}

		Set<String> triggers = new HashSet<String>();
		for (Transition<T, S> stateTransition : set) {
			String pattern = stateTransition.getMatcher().toString();
			if (pattern != null) {
				triggers.add(pattern);
			}
		}
		return getConcretePatterns(triggers);
	}

	private Collection<String> getConcretePatterns(Set<String> patterns) {

		Set<String> triggers = new HashSet<String>();

		for (String pattern : patterns) {

			if (pattern.equals("*")) {
				triggers.add(getConcretePatternForWildcard(triggers));
			}
			else {
				triggers.add(pattern);
			}

		}

		return triggers;

	}

	/**
	 * Convenience method to generate a concrete event name for a single wildcard, but not
	 * repeating anything already on the list. Begins by using well-known constants from
	 * {@link FlowEvent}, and when those run out just returns a trigger of the form
	 * <code>ANYTHING[n]</code> where <code>[n]</code> starts blank and then increments as
	 * an integer until a unique event is found.
	 * 
	 * @param triggers the current candidate trigger list
	 * @return a replacement for a single wildcard not already in the candidate list
	 */
	protected String getConcretePatternForWildcard(Collection<String> triggers) {
		Collection<String> standards = Arrays.asList("COMPLETED", "FAILED", "UNKNOWN");
		for (String standard : standards) {
			if (!triggers.contains(standard)) {
				return standard;
			}
		}
		StringBuilder builder = new StringBuilder("ANYTHING");
		if (!triggers.contains(builder.toString())) {
			return builder.toString();
		}
		int count = 0;
		builder.append(count);
		while (triggers.contains(builder.toString())) {
			builder.replace("ANYTHING".length(), builder.length(), "" + (count++));
		}
		return builder.toString();
	}

	/**
	 * Convenience method for handling a flow event.
	 * 
	 * @param state start in this state
	 * @param event pass in this status
	 * @param context and this context
	 * @return a token for the execution when it pauses or ends
	 * @throws FlowExecutionException
	 */
	private FlowResult<T, S> handle(State<T, S> state, T context)
			throws FlowExecutionException {

		String stateName = state.getName();

		S event = null;
		boolean pause = false;

		// Terminate if there are no more states
		while (state != null) {

			stateName = state.getName();

			try {
				event = state.handle(context);
			}
			catch (Exception e) {
				throw new FlowExecutionException(String.format(
						"Ended flow=%s at state=%s with exception", name, stateName), e);
			}

			pause = state.isPause();
			state = pause ? null : nextState(state, event);

		}

		// If we are not paused then we must be complete.
		return new FlowResult<T, S>(stateName, context, event, !pause);

	}

	/**
	 * Determine the next state from here given the event.
	 * 
	 * @return the next {@link State} (or null if this is the end)
	 * @throws JobExecutionException
	 */
	private State<T, S> nextState(State<T, S> state, S event)
			throws FlowExecutionException {

		String stateName = state.getName();
		Set<Transition<T, S>> set = transitionMap.get(stateName);

		if (set == null) {
			throw new FlowExecutionException(String.format(
					"No transitions found in flow=%s for state=%s", getName(), stateName));
		}

		String next = null;
		for (Transition<T, S> stateTransition : set) {
			if (stateTransition.matches(event)) {
				if (stateTransition.isEnd()) {
					// End of job
					return null;
				}
				next = stateTransition.getNext();
				break;
			}
		}

		if (next == null) {
			throw new FlowExecutionException(String.format(
					"Next state not found in flow=%s for step=%s with exit status=%s",
					getName(), stateName, event));
		}

		// This should not happen if initializeTransitions is called
		Assert.state(stateMap.containsKey(next), String.format(
				"Next state not specified in flow=%s for next=%s", getName(), next));

		return stateMap.get(next);

	}

	/**
	 * Analyse the transitions provided and generate all the information needed to execute
	 * the flow.
	 */
	private void initializeTransitions() {

		if (transitions.isEmpty()) {
			throw new FlowDefinitionException("There are no transitions in this flow.");
		}

		startState = null;
		transitionMap.clear();
		stateMap.clear();
		boolean hasEndState = false;

		for (Transition<T, S> stepTransition : transitions) {
			State<T, S> step = stepTransition.getState();
			stateMap.put(step.getName(), step);
		}

		for (Transition<T, S> stateTransition : transitions) {

			State<T, S> state = stateTransition.getState();

			if (!stateTransition.isEnd()) {

				String next = stateTransition.getNext();

				if (!stateMap.containsKey(next)) {
					throw new IllegalArgumentException("Missing State for ["
							+ stateTransition + "]");
				}

			}
			else {
				hasEndState = true;
			}

			String name = state.getName();

			SortedSet<Transition<T, S>> set = transitionMap.get(name);
			if (set == null) {
				set = new TreeSet<Transition<T, S>>();
				transitionMap.put(name, set);
			}
			set.add(stateTransition);

		}

		if (!hasEndState) {
			throw new IllegalArgumentException(
					"No end step was found.  You must specify at least one transition with no next state.");
		}

		// Try and locate a transition with no incoming links

		Set<String> nextStateNames = new HashSet<String>();

		for (Transition<T, S> stepTransition : transitions) {
			nextStateNames.add(stepTransition.getNext());
		}

		for (Transition<T, S> stepTransition : transitions) {
			State<T, S> state = stepTransition.getState();
			if (!nextStateNames.contains(state.getName())) {
				if (startState != null && !startState.getName().equals(state.getName())) {
					throw new IllegalArgumentException(
							String.format(
									"Multiple possible start steps found: [%s, %s].  "
											+ "Please specify one explicitly with the startStateName property.",
									startState.getName(), state.getName()));
				}
				startState = state;
			}
		}

		if (startState == null) {
			startState = transitions.iterator().next().getState();
		}

	}

}
