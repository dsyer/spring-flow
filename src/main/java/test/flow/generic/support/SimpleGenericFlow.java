package test.flow.generic.support;

import test.flow.Flow;
import test.flow.FlowExecutionException;
import test.flow.generic.GenericFlow;

public class SimpleGenericFlow<T, S> implements GenericFlow<T, S> {

	private final Flow<FlowToken<T>, S> flow;

	private final FlowMapper<T> extractor;

	public SimpleGenericFlow(Flow<FlowToken<T>, S> flow, FlowMapper<T> extractor) {
		super();
		this.flow = flow;
		this.extractor = extractor;
	}

	public SimpleGenericFlow(Flow<FlowToken<T>, S> flow) {
		this(flow, new DefaultFlowMapper<T>());
	}

	public String getName() {
		return flow.getName();
	}

	public T start(T context) throws FlowExecutionException {
		FlowToken<T> token = new FlowToken<T>(context);
		flow.start(token);
		return token.getContext();
	}

	public T resume(T context, S trigger) throws FlowExecutionException {
		FlowToken<T> token = new FlowToken<T>(context);
		flow.resume(extractor.getState(context), token, trigger);
		return token.getContext();
	}

}
