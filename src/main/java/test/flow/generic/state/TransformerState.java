package test.flow.generic.state;

import test.flow.generic.support.FlowToken;
import test.flow.support.state.AbstractState;

public class TransformerState<T, S> extends AbstractState<FlowToken<T>, S> {

	private final Transformer<T> transformer;
	private final S event;

	public TransformerState(String name, Transformer<T> transformer, S event) {
		super(name);
		this.transformer = transformer;
		this.event = event;
	}

	public TransformerState(String name, Transformer<T> transformer) {
		this(name, transformer, null);
	}

	public TransformerState(String name) {
		this(name, new DefaultTransformer<T>());
	}

	public S handle(FlowToken<T> token) throws Exception {
		T transformed = transformer.transform(token.getContext());
		token.update(transformed);
		return event;
	}

}
