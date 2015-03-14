package test.flow.generic.support;

public class DefaultFlowMapper<T> implements FlowMapper<T> {

	public String getState(T context) {
		return context.toString();
	}

}
