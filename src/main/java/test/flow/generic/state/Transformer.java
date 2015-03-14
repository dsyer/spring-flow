package test.flow.generic.state;


public interface Transformer<T> {
	
	T transform(T input);

}
