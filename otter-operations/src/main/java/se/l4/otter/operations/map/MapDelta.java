package se.l4.otter.operations.map;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.internal.map.DefaultMapDelta;

/**
 * Utility for building a delta for a map structure.
 *
 * @author Andreas Holstenson
 *
 */
public interface MapDelta<ReturnPath>
{
	/**
	 * Indicate that a new value should be set for the given key.
	 *
	 * @param key
	 *   the key to update
	 * @param oldValue
	 *   the value being replaced
	 * @param newValue
	 *   the value to set
	 * @return
	 */
	MapDelta<ReturnPath> set(String key, Object oldValue, Object newValue);

	/**
	 * Indicate that we are done building this delta.
	 *
	 * @return
	 */
	ReturnPath done();

	/**
	 * Get {@link MapDelta} that builds a {@link Operation}.
	 *
	 * @return
	 */
	static MapDelta<Operation<MapHandler>> builder()
	{
		return new DefaultMapDelta<>(o -> o);
	}

}
