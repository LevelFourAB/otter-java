package se.l4.otter.model.spi;

import se.l4.otter.lock.CloseableLock;
import se.l4.otter.model.SharedObject;
import se.l4.otter.operations.Operation;

/**
 * Helper for implementations of {@link SharedObject}, contains id, type,
 * latest value and a method that can should be called when an operation is
 * performed locally on the object.
 *
 * @author Andreas Holstenson
 *
 */
public interface SharedObjectEditor<Op extends Operation<?>>
{
	/**
	 * Get the identifier being used.
	 *
	 * @return
	 */
	String getId();

	/**
	 * Get the type being used.
	 *
	 * @return
	 */
	String getType();

	/**
	 * Get the current value.
	 *
	 * @return
	 */
	Op getCurrent();

	/**
	 * Acquire a lock on this editor, which guarantees that no other
	 * changes will occur while the lock is held.
	 *
	 * @return
	 */
	CloseableLock lock();

	/**
	 * Locally apply the given operation.
	 *
	 * @param op
	 */
	void apply(Op op);

	/**
	 * Queue a runnable that should trigger an event when the current lock
	 * is released or when the current remote change is fully applied.
	 *
	 * @param runnable
	 */
	void queueEvent(Runnable runnable);

	/**
	 * Get an object from the model.
	 *
	 * @param id
	 * @param type
	 * @return
	 */
	SharedObject getObject(String id, String type);

	/**
	 * Set a handler that will receive any operations that occur.
	 *
	 * @param handler
	 */
	void setOperationHandler(OperationHandler<Op> handler);

	interface OperationHandler<Op>
	{
		void newOperation(Op op, boolean local);
	}
}
