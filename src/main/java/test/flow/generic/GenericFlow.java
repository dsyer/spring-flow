package test.flow.generic;

import test.flow.FlowExecutionException;

public interface GenericFlow<T, S> {

	String getName();

	T start(T context) throws FlowExecutionException;

	T resume(T context, S trigger) throws FlowExecutionException;

}