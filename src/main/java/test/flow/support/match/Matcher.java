package test.flow.support.match;

public interface Matcher<S> extends Comparable<Matcher<S>> {
	
	boolean match(S value);

}
