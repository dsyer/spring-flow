package test.flow.generic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import test.flow.generic.state.DefaultTransformer;
import test.flow.generic.state.TransformerState;
import test.flow.generic.support.FlowToken;
import test.flow.generic.support.SimpleGenericFlow;
import test.flow.support.FlowBuilder;

public class GenericFlowTests {

	protected enum Status {
		OPEN, ACCEPTED, REJECTED, CLOSED;
	}

	protected enum Action {
		SUCCESS, FAILURE;
	}

	protected static class Item {

		private Status status = Status.OPEN;

		private Action action = Action.SUCCESS;

		public Item(Status status) {
			super();
			this.status = status;
		}

		public Status getStatus() {
			return status;
		}

		public Action getAction() {
			return action;
		}

		public void setStatus(Status status) {
			this.status = status;
		}

		public void setAction(Action action) {
			this.action = action;
		}

		public String toString() {
			return status.name();
		}

	}

	private static class SimpleState extends TransformerState<Item, Action> {

		private final boolean pause;

		public SimpleState(final Status status) {
			this(status, false);
		}

		public SimpleState(final Status status, boolean pause) {
			super(status.toString(), new DefaultTransformer<Item>() {
				public Item transform(Item input) {
					input.setStatus(status);
					return input;
				}
			});
			this.pause = pause;
		}
		
		public boolean isPause() {
			return pause;
		}
	}

	@Test
	public void testSimpleFlow() throws Exception {

		FlowBuilder<FlowToken<Item>, Action> builder = new FlowBuilder<FlowToken<Item>, Action>("items");

		builder.from(new SimpleState(Status.OPEN)).end(new SimpleState(Status.CLOSED));

		GenericFlow<Item, Action> flow = new SimpleGenericFlow<Item, Action>(builder.build());

		Item start = flow.start(new Item(Status.OPEN));
		assertEquals(Status.CLOSED, start.getStatus());

	}

	@Test
	public void testSimpleFlowWithPause() throws Exception {

		FlowBuilder<FlowToken<Item>, Action> builder = new FlowBuilder<FlowToken<Item>, Action>("items");

		builder.from(new SimpleState(Status.OPEN, true)).end(
				new SimpleState(Status.CLOSED, true));

		GenericFlow<Item, Action> flow = new SimpleGenericFlow<Item, Action>(builder.build());

		Item start = flow.start(new Item(Status.OPEN));
		assertEquals(Status.OPEN, start.getStatus());

		Item end = flow.resume(start, Action.SUCCESS);
		assertEquals(Status.CLOSED, end.getStatus());

	}

}
