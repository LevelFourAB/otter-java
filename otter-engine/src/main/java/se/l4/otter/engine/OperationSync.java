package se.l4.otter.engine;

import java.util.function.Consumer;

import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * Synchronization between {@link Editor} instances.
 *
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public interface OperationSync<Op extends Operation<?>>
	extends AutoCloseable
{
	/**
	 * Get the type this sync handles.
	 *
	 * @return
	 */
	OTType<Op> getType();

	/**
	 * Connect and start listening for changes. This will return the latest
	 * version of the document/model being edited.
	 *
	 * @param listener
	 *   listener that will receive updates
	 * @return
	 */
	TaggedOperation<Op> connect(Consumer<TaggedOperation<Op>> listener);

	/**
	 * Send an edit to other editors.
	 *
	 * @param op
	 */
	void send(TaggedOperation<Op> op);

	/**
	 * Close this sync. The sync will no longer be able to receive operations.
	 */
	@Override
	void close();
}
