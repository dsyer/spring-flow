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
package test.flow.support.state;

import test.flow.support.State;


/**
 * Convenient base class for states handling common concerns (naming).
 * 
 * @author Dave Syer
 * 
 */
public abstract class AbstractState<T, S> implements State<T, S> {

	private final String name;

	private final boolean pause;

	/**
	 * Create a new state with the given name and pause flag.
	 * 
	 * @param name
	 * @param pause if true then a flow execution will pause after this state
	 */
	public AbstractState(String name, boolean pause) {
		this.name = name;
		this.pause = pause;
	}

	/**
	 * Create a non-pausing state with the given name.
	 * 
	 * @param name the state name
	 */
	public AbstractState(String name) {
		this(name, false);
	}

	/**
	 * The name of the state.
	 * 
	 * @see State#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * Extension point for subclasses requiring specific semantics for the
	 * mapping from input to output. Abstracts the execution of a business
	 * service, in the context provided.
	 * 
	 * @param context the current context
	 * 
	 * @return an indication of the outcome
	 * 
	 * @see State#
	 */
	public abstract S handle(T context) throws Exception;

	/**
	 * @return true if the state was constructed to pause
	 * @see State#isPause()
	 */
	public boolean isPause() {
		return pause;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + name + (pause ? "(pause)" : "");
	}

}
