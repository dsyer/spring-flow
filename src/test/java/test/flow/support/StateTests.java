package test.flow.support;

import static org.junit.Assert.*;

import org.junit.Test;

import test.flow.support.state.AbstractState;

public class StateTests {
	
	private AbstractState<String, String> state = new AbstractState<String, String>("foo") {
		public String handle(String context) throws Exception { return context; }
	};
	
	@Test
	public void testToString() throws Exception {
		String string = state.toString();
		assertTrue("Wrong toString: "+string, string.contains(":foo"));
	}

	@Test
	public void testToStringWithPause() throws Exception {
		state = new AbstractState<String, String>("foo", true) {
			public String handle(String context) throws Exception { return context; }
		};
		String string = state.toString();
		assertTrue("Wrong toString: "+string, string.contains(":foo(pause)"));
	}

}
