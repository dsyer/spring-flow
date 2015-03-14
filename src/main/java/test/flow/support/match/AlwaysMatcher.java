package test.flow.support.match;


/**
 * Always matches any input and always sorts to the end of a list. Useful as a
 * "wildcard" entry to provide default behaviour to a chain of matchers.
 * 
 * @author Dave Syer
 * 
 * @param <S>
 */
public class AlwaysMatcher<S> implements Matcher<S> {

	/**
	 * Assures that this object has the highest possible sort order compared to
	 * other matchers.
	 * 
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Matcher<S> o) {
		return Integer.MAX_VALUE;
	}

	/**
	 * Always returns true.
	 * 
	 * @return true
	 * @see Matcher#match(Object)
	 */
	public boolean match(S status) {
		return true;
	}

	@Override
	public String toString() {
		return "*";
	}

}
