package se.l4.otter.engine;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import se.l4.otter.lock.CloseableLock;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * Interface used by editors, allows for listening for external operations
 * and sending local operations to other editors.
 *
 * <p>
 * When using the editor please note that changes can occur at any time and
 * to guarantee thread safety you need to use either {@link #lock()},
 * {@link #perform(Runnable)} or {@link #perform(Callable)} when performing
 * changes that require either several mutations or a read followed by a
 * mutation.
 *
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public interface Editor<Op extends Operation<?>>
	extends AutoCloseable
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
	void addListener(EditorListener<Op> listener);

	/**
	 * Remove a listener that should no longer be notified about changes.
	 *
	 * @param listener
	 */
	void removeListener(EditorListener<Op> listener);

	/**
	 * Apply the given operation and send it to other clients. The operation
	 * is assumed to have been applied locally and no listeners will be
	 * triggered.
	 *
	 * @param op
	 */
	CompletableFuture<Void> apply(Op op);

	/**
	 * Acquire a lock for this editor. When the lock is held the editor will
	 * not apply any remote edits and will buffer all local edits.
	 *
	 * <p>
	 * The lock is tied to the current thread and must be used with a
	 * try-statement.
	 *
	 * <pre>
	 * try(CloseableLock lock = editor.lock()) {
	 *   // Your code here
	 * }
	 * </pre>
	 * @return
	 */
	CloseableLock lock();

	/**
	 * Perform an action while holding a lock. This is another way of
	 * acquiring the {@link #lock()} for this editor.
	 *
	 * @param method
	 */
	default void perform(Runnable action)
	{
		try(CloseableLock lock = lock())
		{
			action.run();
		}
	}

	/**
	 * Perform an action while holding a lock. See {@link #perform(Runnable)}
	 * for details.
	 *
	 * @param action
	 * @return
	 * @throws Exception
	 */
	default <T> T perform(Callable<T> action)
		throws Exception
	{
		try(CloseableLock lock = lock())
		{
			return action.call();
		}
	}

	/**
	 * Close this editor.
	 *
	 */
	@Override
	void close();
}
