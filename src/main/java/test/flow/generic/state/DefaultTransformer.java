package test.flow.generic.state;

public class DefaultTransformer<T> implements Transformer<T> {

	public T transform(T input) {
		return input;
	}

}
