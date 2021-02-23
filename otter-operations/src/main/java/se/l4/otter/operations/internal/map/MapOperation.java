package se.l4.otter.operations.internal.map;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.map.MapHandler;

public interface MapOperation
	extends Operation<MapHandler>
{
	/**
	 * Get the key this operation acts upon.
	 *
	 * @return
	 */
	String getKey();
}
