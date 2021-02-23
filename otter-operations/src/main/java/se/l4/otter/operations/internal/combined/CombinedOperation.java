package se.l4.otter.operations.internal.combined;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedHandler;

public interface CombinedOperation
	extends Operation<CombinedHandler>
{
	/**
	 * Get the identifier that the operation targets.
	 *
	 * @return
	 */
	String getId();
}
