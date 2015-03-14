package test.flow.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import test.flow.Flow;
import test.flow.support.match.AlwaysMatcher;
import test.flow.support.match.EqualsMatcher;
import test.flow.support.match.Matcher;
import test.flow.support.match.PatternMatcher;

/**
 * Builder for {@link SimpleFlow} instances providing a convenient fluent API
 * for programmatically creating a flow. Not thread safe.
 * 
 * @author Dave Syer
 * 
 */
public class FlowBuilder<T, S> {

	/**
	 * Enumeration to keep track of internal state.
	 * 
	 */
	private static enum Current {
		FROM, ON, TO, END, BUILD;
	}

	private String name;

	private State<T, S> state;

	private State<T, S> start;

	private Collection<Transition<T, S>> transitions = new LinkedHashSet<Transition<T, S>>();

	private Set<String> froms = new HashSet<String>();

	private Set<String> ends = new HashSet<String>();

	private Set<State<T, S>> tos = new HashSet<State<T, S>>();

	private Matcher<S> matcher;

	private Current in;

	public FlowBuilder(String name) {
		this.name = name;
	}

	public FlowBuilder() {
	}

	/**
	 * The name of the flow. Can also be set implicitly to the first state
	 * registered with a call to {@link #from(State)} or {@link #end(State)}.
	 * 
	 * @param name the name of the flow
	 */
	public void name(String name) {
		this.name = name;
	}

	/**
	 * Set up an end state, from which there can be no further transitions. A
	 * single state flow can be set up by calling this method only, followed by
	 * {@link #build()}.
	 * 
	 * @param next an explicit end state
	 */
	public void end(State<T, S> next) {

		if (name == null) {
			name = next.getName();
		}

		if (state != null) {
			transitions.add(Transition.create(state, matcher, next.getName()));
		}

		if (matcher == null) {
			transitions.add(Transition.createEnd(next));
		}
		else {
			transitions.add(Transition.createEnd(next, matcher));
		}

		ends.add(next.getName());
		in = Current.END;
		this.state = null;
		this.matcher = null;

	}

	/**
	 * Start a new sequence of transitions at the given state. Cannot be called
	 * if waiting for a transition to be finished (i.e. if {@link #from(State)}
	 * or {@link #on(String)} was just called).
	 * 
	 * @param state the state to start a transition from. Must not be an end
	 * state.
	 * @return this (the builder instance)
	 * 
	 * @throws FlowDefinitionException if the builder is in an illegal state for
	 * starting a new sequence.
	 */
	public FlowBuilder<T, S> from(State<T, S> state) {

		if (in == Current.FROM) {
			throw new FlowDefinitionException("Illegal ordering. Waiting for on() or to() or end().");
		}

		if (in == Current.ON) {
			throw new FlowDefinitionException("Illegal ordering.  Waiting for to() or end() before from().");
		}

		if (ends.contains(state.getName())) {
			throw new FlowDefinitionException("This state is an end state: it cannot be used in from().");
		}

		if (name == null) {
			name = state.getName();
		}

		if (start == null) {
			start = state;
		}

		this.state = state;
		froms.add(state.getName());

		in = Current.FROM;
		return this;

	}

	/**
	 * Specify a trigger with a template value of the event, i.e. if the event
	 * in a flow equals this template then the transition will be triggered. As
	 * a special case, if the template is a String then you can specify patterns
	 * here as well as exact matches (e.g. "*" to match all, or "FINISH*" to
	 * match values starting with "FINISH").
	 * 
	 * @param template the template value
	 * @return this (the builder instance)
	 * 
	 * @see PatternMatcher
	 */
	public FlowBuilder<T, S> on(S template) {
		if (template instanceof String) {
			@SuppressWarnings("unchecked")
			Matcher<S> matcher = (Matcher<S>) new PatternMatcher((String) template);
			return on(matcher);
		}
		return on(new EqualsMatcher<S>(template));
	}

	/**
	 * Specify a matcher to trigger a transition to the next state (to be
	 * specified with {@link #to(State)} or {@link #end(State)}). This method is
	 * optional: if it is not called before {@link #to(State)} then no matcher
	 * is explicitly registered and a wildcard matching all events will be used.
	 * 
	 * @param matcher an event matcher to trigger a transition
	 * @return this (the builder instance)
	 * 
	 * @throws FlowDefinitionException if there is no state to transition from.
	 */
	public FlowBuilder<T, S> on(Matcher<S> matcher) {

		if (state == null) {
			throw new FlowDefinitionException("No current state.  Use from() or to() before on().");
		}

		this.matcher = matcher;

		in = Current.ON;
		return this;

	}

	/**
	 * The next state in the sequence.
	 * 
	 * @param next the {@link State} to handle next
	 * @return this (the builder instance)
	 * 
	 * @throws FlowDefinitionException if there is no current state registered
	 * already with {@link #from(State)} or {@link #to(State)}
	 */
	public FlowBuilder<T, S> to(State<T, S> next) {

		if (state == null) {
			throw new FlowDefinitionException("No current state.  Use from() and on() before to().");
		}

		if (matcher == null) {
			matcher = new AlwaysMatcher<S>();
		}

		transitions.add(Transition.create(state, matcher, next.getName()));
		froms.add(state.getName());
		state = next;
		tos.add(next);
		matcher = null;

		in = Current.TO;
		return this;

	}

	/**
	 * Build a flow from the accumulated state. May be called repeatedly and the
	 * result will be a different instance of {@link Flow}.
	 * 
	 * @return a finished {@link Flow} built from the accumulated state
	 */
	public Flow<T, S> build() {

		if (in == Current.FROM || in == Current.ON) {
			throw new FlowDefinitionException("Illegal ordering. Waiting for end() or to() before build().");
		}

		SimpleFlow<T, S> flow = new SimpleFlow<T, S>(name);

		in = Current.BUILD;
		transitions.addAll(getMissingEnds());
		flow.setTransitions(new ArrayList<Transition<T, S>>(transitions));

		try {
			flow.afterPropertiesSet();
		}
		catch (Exception e) {
			throw new FlowDefinitionException("Could not build flow", e);
		}

		return flow;

	}

	/**
	 * To allow the use of to() as a synonym for end() we need to look for the
	 * missing end states and supply the transition.
	 * 
	 * @return the missing end states
	 */
	private Collection<Transition<T, S>> getMissingEnds() {
		Collection<Transition<T, S>> transitions = new ArrayList<Transition<T, S>>();
		for (State<T, S> state : tos) {
			String name = state.getName();
			if (!froms.contains(name) && !ends.contains(name)) {
				transitions.add(Transition.createEnd(state));
			}
		}
		return transitions;
	}

}
