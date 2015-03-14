package test.flow.support.state;

public interface PauseAdapter<T, S> {
	
	S pause(T context);

}
