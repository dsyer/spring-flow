package test.flow.support;

import java.util.Collection;


public interface StateLocator<T, S> {

	/**
	 * @return the names of states in this flow
	 */
	Collection<String> getStateNames();

	/**
	 * @param stateName the name of the state
	 * @return the {@link State} requested
	 * @throws FlowDefinitionException if there is no such state
	 */
	State<T, S> getState(String stateName) throws FlowDefinitionException;

	/**
	 * @param stateName the name of the {@link State} to inquire about
	 * @return a collection of {@link FlowEvent} name patterns that can trigger
	 * transitions from this state
	 * @throws FlowDefinitionException if there is no state with this name
	 */
	Collection<String> getTriggers(String stateName) throws FlowDefinitionException;

}
