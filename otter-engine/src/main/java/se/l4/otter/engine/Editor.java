package se.l4.otter.engine;

import java.util.function.Consumer;

import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * Interface used by editors, allows for listening for external operations
 * and sending local operations to other editors.
 * 
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public interface Editor<Op extends Operation<?>>
{
	/**
	 * Get the unique identifier of this editor instance.
	 * 
	 * @return
	 */
	String getId();
	
	/**
	 * Get the type that this editor works with.
	 * 
	 * @return
	 */
	OTType<Op> getType();
	
	/**
	 * Get the operation that describes the current thing being worked on.
	 * 
	 * @return
	 */
	Op getCurrent();
	
	/**
	 * Add a listener that will be notified about changes performed by other
	 * clients.
	 * 
	 * @param listener
	 */
	void addChangeListener(Consumer<Op> listener);
	
	/**
	 * Apply the given operation and send it to other clients. The operation
	 * is assumed to have been applied locally and no listeners will be
	 * triggered.
	 * 
	 * @param op
	 */
	void apply(Op op);
}
