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

import test.flow.support.match.AlwaysMatcher;
import test.flow.support.match.EqualsMatcher;
import test.flow.support.match.Matcher;
import test.flow.support.match.PatternMatcher;

/**
 * Value object representing a potential transition from one {@link State} to
 * another. The originating {@link State} and the name of the next {@link State}
 * are linked by a pattern for the exit status of an execution of the
 * originating {@link State}.
 * 
 * @author Dave Syer
 * 
 */
public class Transition<T, S> implements Comparable<Transition<T, S>> {

	private final State<T, S> state;

	private final Matcher<S> matcher;

	private final String next;

	/**
	 * Create a new end state {@link Transition} specification. This transition
	 * explicitly goes unconditionally to an end state from the {@link State}
	 * provided (i.e. no more executions).
	 * 
	 * @param state the {@link State} used to generate the outcome for this
	 * transition
	 */
	public static <T, S> Transition<T, S> createEnd(State<T, S> state) {
		return create(state, new AlwaysMatcher<S>(), null);
	}

	/**
	 * Create a new end state {@link Transition} specification. This transition
	 * explicitly goes to an end state (i.e. no more processing) if the outcome
	 * matches the pattern.
	 * 
	 * @param state the {@link State} used to generate the outcome for this
	 * transition
	 * @param matcher the matcher to match the exit status of the {@link State}
	 */
	public static <T, S> Transition<T, S> createEnd(State<T, S> state, Matcher<S> matcher) {
		return create(state, matcher, null);
	}

	/**
	 * Create a new end state {@link Transition} specification. This transition
	 * explicitly goes to an end state (i.e. no more processing) if the outcome
	 * matches the pattern.
	 * 
	 * @param state the {@link State} used to generate the outcome for this
	 * transition
	 * @param value the value to match the exit status of the {@link State}
	 */
	public static <T, S> Transition<T, S> createEnd(State<T, S> state, S value) {
		return create(state, new EqualsMatcher<S>(value), null);
	}

	/**
	 * Create a new state {@link Transition} specification with a wildcard
	 * pattern that matches all outcomes.
	 * 
	 * @param state the {@link State} used to generate the outcome for this
	 * transition
	 * @param next the name of the next {@link State} to execute
	 */
	public static <T, S> Transition<T, S> create(State<T, S> state, String next) {
		return create(state, new AlwaysMatcher<S>(), next);
	}

	/**
	 * Create a new {@link Transition} specification from one {@link State} to
	 * another (by name).
	 * 
	 * @param state the {@link State} used to generate the outcome for this
	 * transition
	 * @param matcher the matcher to match in the exit status of the
	 * {@link State}
	 * @param next the name of the next {@link State} to execute
	 */
	public static <T, S> Transition<T, S> create(State<T, S> state, Matcher<S> matcher, String next) {
		if (matcher == null) {
			matcher = new AlwaysMatcher<S>();
		}
		return new Transition<T, S>(state, matcher, next);
	}

	/**
	 * Create a new {@link Transition} specification from one {@link State} to
	 * another (by name).
	 * 
	 * @param state the {@link State} used to generate the outcome for this
	 * transition
	 * @param value the value to match in the exit status of the {@link State}
	 * @param next the name of the next {@link State} to execute
	 */
	public static <T, S> Transition<T, S> create(State<T, S> state, S value, String next) {
		Matcher<S> matcher = new EqualsMatcher<S>(value);
		if (value instanceof String) {
			@SuppressWarnings("unchecked")
			Matcher<S> pattern = (Matcher<S>)new PatternMatcher((String)value);
			matcher = pattern;
		}
		return new Transition<T, S>(state, matcher, next);
	}

	private Transition(State<T, S> state, Matcher<S> matcher, String next) {
		super();
		this.matcher = matcher;
		this.next = next;
		this.state = state;
	}

	/**
	 * Public getter for the State.
	 * @return the State
	 */
	public State<T, S> getState() {
		return state;
	}

	/**
	 * The next State name.
	 * @return the next
	 */
	public String getNext() {
		return next;
	}

	/**
	 * The pattern of event names that trigger this transition
	 * @return the pattern for a trigger of this transition
	 */
	public Matcher<S> getMatcher() {
		return matcher;
	}

	/**
	 * Check if the provided status matches the pattern, signalling that the
	 * next State should be executed.
	 * 
	 * @param status the status to compare
	 * @return true if the pattern matches this status
	 */
	public boolean matches(S status) {
		return matcher.match(status);
	}

	/**
	 * Check for a special next State signalling the end of a job.
	 * 
	 * @return true if this transition goes nowhere (there is no next)
	 */
	public boolean isEnd() {
		return next == null;
	}

	/**
	 * Sorts by decreasing specificity of pattern, based on comparing the
	 * matchers.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Transition<T, S> other) {
		return matcher.compareTo(other.matcher);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Transition))
			return false;
		@SuppressWarnings("rawtypes")
		Transition other = (Transition) obj;
		return (other.state == null ? "" : other.state.getName()).equals(state == null ? "" : state.getName())
				&& other.matcher.equals(matcher) && (next == null ? other.next == null : next.equals(other.next));
	}

	@Override
	public int hashCode() {
		return 17 + 57 * state.getName().hashCode() + 71 * matcher.hashCode() + 83
				* (next == null ? 0 : next.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("StateTransition: state=%s, matcher=%s, next=%s", state == null ? null : state.getName(),
				matcher, next);
	}

}
