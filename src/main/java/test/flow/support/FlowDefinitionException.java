package test.flow.support;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception signalling a problem with the definition of a flow, its logical
 * states and the paths between them.
 * 
 * @author Dave Syer
 * 
 */
@SuppressWarnings("serial")
public class FlowDefinitionException extends NestedRuntimeException {

	/**
	 * Construct a {@link FlowDefinitionException} with the provided message for
	 * the user and indicated cause.
	 * 
	 * @param message the user message
	 * @param cause the cause of the problem
	 */
	public FlowDefinitionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Construct a {@link FlowDefinitionException} with the provided message for
	 * the user.
	 * 
	 * @param message the user message
	 */
	public FlowDefinitionException(String message) {
		super(message);
	}

}
