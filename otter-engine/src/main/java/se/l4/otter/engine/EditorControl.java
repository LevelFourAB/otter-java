package se.l4.otter.engine;

import se.l4.otter.lock.CloseableLock;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * Helper for keeping several editors in sync.
 *
 * @author Andreas Holstenson
 *
 */
public interface EditorControl<Op extends Operation<?>>
{
	/**
	 * Get the type this control handles.
	 *
	 * @return
	 */
	OTType<Op> getType();

	/**
	 * Lock this instance, this ensures that no other operation occur
	 * on the editor control while the lock is being held. This is useful
	 * to guarantee sending of changes in order similar to this:
	 *
	 * <pre>
	 * try(CloseableLock lock = control.lock()) {
	 *   TaggedOperation change = control.store(taggedOp);
	 *   remoteClients.send(change);
	 * }
	 * </pre>
	 *
	 * @return
	 */
	CloseableLock lock();

	/**
	 * Get the operation that describes the document that a new client should
	 * start with.
	 *
	 * @return
	 */
	TaggedOperation<Op> getLatest();

	/**
	 * Get the identifier of the latest stored operation.
	 *
	 * @return
	 */
	long getLatestVersion();

	/**
	 * Store a new operation using {@link TaggedOperation#getHistoryId()} as
	 * the history base. This method returns a transformed operation that
	 * other editors should apply.
	 *
	 * @param operation
	 * @return
	 */
	TaggedOperation<Op> store(TaggedOperation<Op> operation);

	/**
	 * Store a new operation using the specified history identifier as a base.
	 * This method returns a transformed operation that other editors should
	 * apply.
	 *
	 * @param historyBase
	 *   the history identifier the operation applies to
	 * @param token
	 *   token used to identify this operation by the sender
	 * @param operation
	 *   the operation to store and apply
	 * @return
	 */
	TaggedOperation<Op> store(long historyBase, String token, Op operation);
}
