package test.flow.support.state;


/**
 * @author Dave Syer
 * 
 */
public class PauseState<T, S> extends AbstractState<T, S> {

	private PauseAdapter<T, S> adapter;

	/**
	 * @param name
	 */
	public PauseState(String name, PauseAdapter<T, S> adapter) {
		super(name, true);
		this.adapter = adapter;
	}

	@Override
	public S handle(T context) throws Exception {

		// The pause adapter is just a convenient way to provide an extension
		// point without requiring a subclass.
		return adapter.pause(context);

	}

}