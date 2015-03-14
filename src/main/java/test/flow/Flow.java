package test.flow;

/**
 * Interface for a flow, which abstracts the structure of business logic from
 * the caller.
 * 
 * @author Dave Syer
 * 
 * @param <T> type for business context of flow execution
 * @param <S> type for status of a finished execution and the event that
 * triggers a resume operations
 */
public interface Flow<T, S> {

	/**
	 * @return the name of the flow
	 */
	String getName();

	/**
	 * Start the flow and wait for the result. The result tells the caller about
	 * completeness (did the flow finish executing?) and also carries the
	 * business context, in case that changed, as well as information about the
	 * trigger that caused the flow to end (the instance of S).
	 * 
	 * @param context the business context for the flow
	 * 
	 * @return a {@link FlowResult} encapsulating the output
	 * 
	 * @throws FlowExecutionException if there is problem, like a checked
	 * business exception
	 */
	FlowResult<T, S> start(T context) throws FlowExecutionException;

	/**
	 * Resume a flow that was paused. The value of the memento comes from the
	 * output of the previous resume or start operation. Each implementation of
	 * a {@link Flow} has its own memento type, so as long as the memento came
	 * from a {@link FlowResult} in the same flow instance, this operation will
	 * succeed.
	 * 
	 * @param memento a memento of the state of the flow when it paused
	 * @param context the business context for the flow
	 * @param event the event that triggered this operation
	 * 
	 * @return a {@link FlowResult} encapsulating the output
	 * 
	 * @throws FlowExecutionException
	 */
	FlowResult<T, S> resume(Object memento, T context, S event) throws FlowExecutionException;

}