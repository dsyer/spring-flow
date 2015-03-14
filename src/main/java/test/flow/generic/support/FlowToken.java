package test.flow.generic.support;


public class FlowToken<T> {

	private T context;

	public FlowToken(T context) {
		this.context = context;
	}
	
	public T getContext() {
		return context;
	}
	
	public void update(T context) {
		this.context = context;
	};
	
}
