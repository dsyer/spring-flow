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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

import test.flow.Flow;
import test.flow.FlowExecutionException;
import test.flow.FlowResult;
import test.flow.support.State;

/**
 * A {@link State} implementation that splits a {@link Flow} into multiple
 * parallel subflows.
 * 
 * @author Dave Syer
 * 
 */
public class SplitState<T, S> extends AbstractState<T, S> {

	private final Collection<Flow<T, S>> flows;

	private TaskExecutor taskExecutor = new SyncTaskExecutor();

	@SuppressWarnings("unchecked")
	private SplitAggregator<S> aggregator = new MaxValueSplitAggregator();

	private final SplitAdapter<T, S> adapter;

	/**
	 * Create a new {@link SplitState} that can share work amongst a collection
	 * of flows. The same context is passed to each flow.
	 * 
	 * @param name the name of the state
	 * @param flows the flows to divide work amongst
	 */
	public SplitState(String name, Collection<Flow<T, S>> flows) {
		this(name, flows, null);
	}

	/**
	 * Create a new {@link SplitState} that can share work amongst a collection
	 * of flows. A new context is passed to each flow according to the
	 * FlowExecution of the adapter {@link SplitAdapter#create(Object) adapter
	 * create} method. The adapter is optional, but should always be provided in
	 * a multi-threaded split if the business context (T) is not thread safe.
	 * 
	 * @param name the name of the state
	 * @param flows the flows to divide work amongst
	 * @param adapter a business adapter
	 */
	public SplitState(String name, Collection<Flow<T, S>> flows, SplitAdapter<T, S> adapter) {
		super(name);
		this.flows = flows;
		this.adapter = adapter;
	}

	/**
	 * Public setter for the taskExecutor.
	 * @param taskExecutor the taskExecutor to set
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Execute the flows by passing them to the {@link TaskExecutor} and waiting
	 * for all of them to finish before proceeding. Take care that the business
	 * context is thread safe if using a multi-threaded executor, or else
	 * provide a {@link SplitAdapter} to create new instances and aggregate them
	 * back into the parent.<br/>
	 * <br/>
	 * 
	 * Use of a thread pool is strongly recommended for multi-threaded
	 * execution. The default is single threaded.
	 * 
	 * @see State#handle(FlowResult, Object)
	 */
	@Override
	public S handle(final T context) throws Exception {

		Collection<FutureTask<FlowResult<T, S>>> tasks = new ArrayList<FutureTask<FlowResult<T, S>>>();

		for (final Flow<T, S> flow : flows) {

			final FutureTask<FlowResult<T, S>> task = new FutureTask<FlowResult<T, S>>(
					new Callable<FlowResult<T, S>>() {
						public FlowResult<T, S> call() throws Exception {
							T child = adapter == null ? context : adapter.create(context);
							FlowResult<T, S> start = flow.start(child);
							return new FlowResult<T, S>(getName(), start.getContext(), start.getEvent());
						}
					});

			tasks.add(task);

			try {
				taskExecutor.execute(new Runnable() {
					public void run() {
						task.run();
					}
				});
			}
			catch (TaskRejectedException e) {
				throw new FlowExecutionException("TaskExecutor rejected task for flow=" + flow.getName());
			}

		}

		Collection<S> executions = new ArrayList<S>();
		Collection<T> contexts = new ArrayList<T>();

		// TODO: could use a CompletionService in Spring 3?
		for (FutureTask<FlowResult<T, S>> task : tasks) {
			FlowResult<T, S> FlowExecution = task.get();
			executions.add(FlowExecution.getEvent());
			contexts.add(FlowExecution.getContext());
		}

		if (adapter != null) {
			adapter.aggregate(context, contexts);
		}

		return aggregator.aggregate(executions);

	}

}
