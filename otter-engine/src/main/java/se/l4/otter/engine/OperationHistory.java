package se.l4.otter.engine;

import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * Storage abstraction used by {@link EditorControl}. This abstraction
 * uses {@code long} identifiers that need to be increasing for every
 * call to {@link #store(Operation)}. The identifiers do not need to be
 * continuous.
 *
 * <p>
 * A good practice when implementing this interface would be to store composed
 * snapshots at certain points to reduce the number of operations that need
 * to be composed by users of the history.
 *
 * @author Andreas Holstenson
 *
 */
public interface OperationHistory<Op extends Operation<?>>
{
	/**
	 * Get the type this storage supports.
	 *
	 * @return
	 */
	OTType<Op> getType();

	/**
	 * Get the latest history identifier available.
	 *
	 * @return
	 */
	long getLatest();

	/**
	 * Get all operations up until specified history identifier.
	 *
	 * @param historyId
	 *   history id to return history up until (exclusive)
	 * @return
	 */
	CloseableIterator<Op> until(long historyId);

	/**
	 * Get all operations between the two history identifier (including both
	 * start and end).
	 *
	 * @param start
	 *   history id to return history from (inclusive)
	 * @param end
	 *   history id to return history up until (exclusive)
	 * @return
	 */
	CloseableIterator<Op> between(long start, long end);

	/**
	 * Get all operations starting with the specified history identifier.
	 *
	 * @param historyId
	 *   history id to return history from (inclusive)
	 * @return
	 */
	CloseableIterator<Op> from(long historyId);

	/**
	 * Store an operation and return the identifier it was stored with.
	 *
	 * @param op
	 * @return
	 */
	long store(Op op);
}
