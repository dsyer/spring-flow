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

/**
 * Encapsulation of business processing leading to a flow outcome. The concerns
 * of business logic and flow execution logic are separated here so that flow
 * logic can be configured or implemented separately from the business
 * execution.
 * 
 * 
 * @author Dave Syer
 * 
 */
public interface State<T, S> {

	/**
	 * The name of the state. Must be unique within a flow (but uniqueness is
	 * not enforced by implementations of this interface).
	 * 
	 * @return the name of this state
	 */
	String getName();

	/**
	 * Handle some business or processing logic and return a event that can be
	 * used to drive a flow to the next {@link State}. The context can be used
	 * by implementations to do whatever they need to do, and the same context
	 * will be passed to all {@link State} instances in the same flow, so
	 * implementations should be careful that the context is thread safe, or
	 * used in a thread safe manner.
	 * 
	 * @param context the context passed in by the caller, encapsulating
	 * business state.
	 * 
	 * @return a status for the flow execution
	 * 
	 * @throws Exception if anything goes wrong
	 */
	S handle(T context) throws Exception;

	/**
	 * @return true if a flow should pause after handling this state
	 */
	boolean isPause();

}
