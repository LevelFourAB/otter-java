package se.l4.otter.model;

import se.l4.otter.lock.CloseableLock;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedDelta;
import se.l4.otter.operations.combined.CombinedTarget;

public interface Model
	extends SharedMap
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
	 * Get the intitial operation that a new model represents.
	 * 
	 * @return
	 */
	static Operation<CombinedTarget> getInitialModel()
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
}
