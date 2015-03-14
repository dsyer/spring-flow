package test.flow.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.flow.Flow;
import test.flow.FlowResult;
import test.flow.support.state.AbstractState;

public class FlowBuilderTests {
	
	private FlowBuilder<String, String> builder = new FlowBuilder<String, String>("test");

	@Before
	@After
	public void cleanUp() {
		StubState.clear();
	}

	@Test(expected=FlowDefinitionException.class)
	public void testBuildIllegalTo() throws Exception {
		builder.to(new StubState("foo"));
	}

	@Test(expected=FlowDefinitionException.class)
	public void testBuildIllegalOn() throws Exception {
		builder.on("foo");
	}

	@Test(expected=FlowDefinitionException.class)
	public void testBuildIllegalBackToBackFrom() throws Exception {
		builder.from(new StubState("foo")).from(new StubState("bar"));
	}

	@Test(expected=FlowDefinitionException.class)
	public void testBuildIllegalFromOnEnd() throws Exception {
		StubState state = new StubState("foo");
		builder.end(state);
		builder.from(state).to(new StubState("bar"));
	}

	@Test
	public void testBuildAndStart() throws Exception {

		FlowBuilder<String, String> builder = new FlowBuilder<String, String>();

		builder.end(new StubState("foo"));
		Flow<String, String> flow = builder.build();
		assertNotNull(flow);

		FlowResult<String,String> execution = flow.start("FOO");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("foo", execution.getMemento());

	}

	@Test
	public void testBuildWithFinalEnd() throws Exception {

		FlowBuilder<String, String> builder = new FlowBuilder<String, String>();

		StubState foo = new StubState("foo");
		StubState bar = new StubState("bar");

		builder.from(foo).end(bar);

		Flow<String, String> flow = builder.build();
		assertNotNull(flow);

		FlowResult<String,String> execution = flow.start("FOO");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("bar", execution.getMemento());

	}

	@Test
	public void testBuildWithAlternateEnd() throws Exception {

		FlowBuilder<String, String> builder = new FlowBuilder<String, String>();

		StubState foo = new StubState("foo");
		StubState bar = new StubState("bar");
		StubState spam = new StubState("spam");

		builder.from(foo).on("COMPLETED").end(bar);
		builder.from(foo).on("FAILED").end(spam);

		Flow<String, String> flow = builder.build();
		@SuppressWarnings("unchecked")
		StateLocator<String,String> locator = (StateLocator<String,String>) flow;
		assertEquals("[bar, foo, spam]", sort(locator.getStateNames()).toString());
		
		assertEquals("[COMPLETED, FAILED]", sort(locator.getTriggers("foo")).toString());

		FlowResult<String,String> execution = flow.start("FOO");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("bar", execution.getMemento());

	}

	@Test
	public void testBuildWithRepeatStart() throws Exception {

		FlowBuilder<String, String> builder = new FlowBuilder<String, String>();

		StubState foo = new StubState("foo");
		StubState bar = new StubState("bar");
		StubState spam = new StubState("spam");

		builder.from(foo).to(spam).on("COMPLETED").end(bar);
		builder.from(foo).to(spam).on("FAILED").end(bar);

		Flow<String, String> flow = builder.build();
		@SuppressWarnings("unchecked")
		StateLocator<String,String> locator = (StateLocator<String,String>) flow;
		assertEquals("[bar, foo, spam]", sort(locator.getStateNames()).toString());
		
		assertEquals("[COMPLETED]", sort(locator.getTriggers("foo")).toString());
		assertEquals("[COMPLETED, FAILED]", sort(locator.getTriggers("spam")).toString());

	}

	@Test
	public void testBuildWithCycle() throws Exception {

		FlowBuilder<String, String> builder = new FlowBuilder<String, String>();

		StubState foo = new StubState("foo");
		StubState bar = new StubState("bar");

		builder.from(foo).on("COMPLETED").to(foo);
		builder.from(foo).on("FAILED").end(bar);

		Flow<String, String> flow = builder.build();
		@SuppressWarnings("unchecked")
		StateLocator<String,String> locator = (StateLocator<String,String>) flow;
		assertEquals("[bar, foo]", sort(locator.getStateNames()).toString());
		
		assertEquals("[COMPLETED, FAILED]", sort(locator.getTriggers("foo")).toString());

	}

	@Test
	public void testBuildWithFromTo() throws Exception {

		FlowBuilder<String, String> builder = new FlowBuilder<String, String>();

		StubState foo = new StubState("foo");
		StubState bar = new StubState("bar");

		builder.from(foo).to(bar);
		
		Flow<String, String> flow = builder.build();
		assertNotNull(flow);

		FlowResult<String,String> execution = flow.start("FOO");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("[foo, bar]", StubState.getHandled().toString());

	}

	@Test
	public void testBuildWithImplicitEnd() throws Exception {

		FlowBuilder<String, String> builder = new FlowBuilder<String, String>();

		StubState foo = new StubState("foo");
		StubState bar = new StubState("bar");
		StubState spam = new StubState("spam");

		builder.from(bar).to(spam);
		builder.from(foo).on("SPAM").to(spam);
		// spam is the end but bar is the last call of to()
		builder.from(foo).to(bar);
		
		Flow<String, String> flow = builder.build();
		assertNotNull(flow);

		FlowResult<String,String> execution = flow.start("FOO");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("[foo, bar, spam]", StubState.getHandled().toString());

	}

	@Test
	public void testBuildWithImplicitEndAndSide() throws Exception {

		FlowBuilder<String, String> builder = new FlowBuilder<String, String>();

		StubState foo = new StubState("foo");
		StubState bar = new StubState("bar");
		StubState spam = new StubState("spam");
		StubState bucket = new StubState("bucket");

		builder.from(foo).on("COMPLETED").to(bar).to(spam).on("COMPLETED").to(bucket);
		builder.from(foo).on("FAILED").to(spam);
		
		Flow<String, String> flow = builder.build();

		@SuppressWarnings("unchecked")
		StateLocator<String,String> locator = (StateLocator<String,String>) flow;
		assertEquals("[COMPLETED, FAILED]", sort(locator.getTriggers("foo")).toString());
		assertEquals("[COMPLETED]", sort(locator.getTriggers("spam")).toString());

		FlowResult<String,String> execution = flow.start("FOO");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("[foo, bar, spam, bucket]", StubState.getHandled().toString());

	}

	@Test
	public void testBuildWithTransitionAndStart() throws Exception {

		FlowBuilder<String, String> builder = new FlowBuilder<String, String>();

		StubState foo = new StubState("foo");
		StubState spam = new StubState("spam");
		StubState bar = new StubState("bar");

		builder.from(foo).on("BAR").to(bar);
		builder.from(foo).on("*").to(spam).on("*").to(bar);

		Flow<String, String> flow = builder.build();
		assertNotNull(flow);

		FlowResult<String, String> execution = flow.start("FOO");
		assertEquals("COMPLETED", execution.getEvent());
		assertEquals("[foo, spam, bar]", StubState.getHandled().toString());

		StubState.clear();
		// Start in the middle at foo and fork off to bar
		execution = flow.resume("foo", "foo", "BAR");
		assertEquals("COMPLETED", execution.getEvent());
		// State foo wasn't handled, so it isn't expected here
		assertEquals("[bar]", StubState.getHandled().toString());

	}

	private List<String> sort(Collection<String> values) {
		List<String> names = new ArrayList<String>(values);
		Collections.sort(names);
		return names;
	}

	private static class StubState extends AbstractState<String, String> {
		
		public static List<String> handled = new ArrayList<String>();

		private final String result;

		public static Object getHandled() {
			return handled;
		}
		
		public static void clear() {
			handled.clear();
		}
		
		public StubState(String name, String result) {
			super(name);
			this.result = result;
		}

		public StubState(String name) {
			this(name, "COMPLETED");
		}
		
		public String handle(String context) throws Exception {
			handled.add(getName());
			return result;
		}

	}

}
