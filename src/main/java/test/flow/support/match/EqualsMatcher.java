package test.flow.support.match;


public class EqualsMatcher<S> implements Matcher<S> {

	private final S template;

	public EqualsMatcher(S template) {
		this.template = template;
	}

	public boolean match(S value) {
		return template.equals(value);
	}

	public int compareTo(Matcher<S> o) {
		if (!(o instanceof EqualsMatcher)) {
			return o==null ? -1 : -o.compareTo(this);
		}
		EqualsMatcher<S> matcher = (EqualsMatcher<S>) o;
		if (!(template instanceof Comparable)) {
			return 0;
		}
		@SuppressWarnings("unchecked")
		Comparable<S> comparable = (Comparable<S>)matcher.template;
		return comparable.compareTo(template);
	}
	
	@Override
	public String toString() {
		return template.toString();
	}

}
