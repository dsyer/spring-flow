package test.flow.support.state;

import java.util.Collection;

public interface SplitAdapter<T, S> {

	/**
	 * Factory method for context objects in split flows.
	 * 
	 * @param parent the parent context
	 * @return a new context (or the parent if it is thread safe)
	 */
	T create(T parent);
	
	/**
	 * Aggregate a collection of contexts into their parent.
	 * 
	 * @param parent the parent (output)
	 * @param children the contexts to aggregate
	 */
	void aggregate(T parent, Collection<T> children);

}
