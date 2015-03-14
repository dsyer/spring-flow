package test.flow.generic.support;


public interface FlowMapper<T> {

	String getState(T context);

}
