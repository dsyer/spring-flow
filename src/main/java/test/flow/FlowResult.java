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
package test.flow;


/**
 * Represents the current state and a trigger for change in a flow.
 * 
 * @author Dave Syer
 * 
 */
public class FlowResult<T, S> {

	private final Object memento;

	private final S event;

	private final T context;

	private final boolean complete;

	/**
	 * @param memento an identifier for the current state of the flow
	 * @param event an event that may trigger a change in the flow
	 */
	public FlowResult(Object memento, T context, S event, boolean complete) {
		this.memento = memento;
		this.context = context;
		this.event = event;
		this.complete = complete;
	}

	public FlowResult(Object memento, T context, S event) {
		this(memento, context, event, true);
	}

	public FlowResult(T context, S event) {
		this(null, context, event, true);
	}

	/**
	 * A memento of the current state of the flow in some abstract sense.
	 * Implementations of {@link Flow} give particular meaning to this value.
	 * 
	 * @return the name of the current state
	 */
	public Object getMemento() {
		return memento;
	}

	/**
	 * An event that triggered a change in the state of the flow.
	 * 
	 * @return the event
	 */
	public S getEvent() {
		return event;
	}

	/**
	 * @return the context
	 */
	public T getContext() {
		return context;
	}

	/**
	 * @return true if the flow has completed
	 */
	public boolean isComplete() {
		return complete;
	}

	@Override
	public String toString() {
		return String.format("FlowExecution: name=%s, status=%s", memento, event);
	}

}
