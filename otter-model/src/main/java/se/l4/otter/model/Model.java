package se.l4.otter.model;

import se.l4.otter.engine.Editor;
import se.l4.otter.lock.CloseableLock;
import se.l4.otter.model.internal.DefaultModelBuilder;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedDelta;
import se.l4.otter.operations.combined.CombinedHandler;

public interface Model
	extends SharedMap, AutoCloseable
{
	/**
	 * Create a new map instance.
	 *
	 * @return
	 */
	SharedMap newMap();

	/**
	 * Create a new string instance.
	 *
	 * @return
	 */
	SharedString newString();

	/**
	 * Create a new shared list.
	 *
	 * @return
	 */
	<T> SharedList<T> newList();

	/**
	 * Create a new object of the given type.
	 *
	 * @param type
	 * @param type
	 * @return
	 */
	<T extends SharedObject> T newObject(String type, Class<T> objectType);

	/**
	 * Get the intitial operation that a new model represents.
	 *
	 * @return
	 */
	static Operation<CombinedHandler> getInitialModel()
	{
		return CombinedDelta.builder()
			.done();
	}

	/**
	 * Acquire a lock for this model. When the lock is held no remote edits
	 * will be applied to the model and any local changes will be buffered.
	 *
	 * <p>
	 * The lock is tied to the current thread and must be used with a
	 * try-statement.
	 *
	 * <pre>
	 * try(CloseableLock lock = model.lock()) {
	 *   // Your code here
	 * }
	 * </pre>
	 * @return
	 */
	CloseableLock lock();

	/**
	 * Close this model and its underlying editor.
	 */
	@Override
	void close();

	/**
	 * Start building a new model using the given editor.
	 *
	 * @param editor
	 *   the editor to use for receiving and performing changes
	 * @return
	 */
	static ModelBuilder builder(Editor<Operation<CombinedHandler>> editor)
	{
		return new DefaultModelBuilder(editor);
	}
}
