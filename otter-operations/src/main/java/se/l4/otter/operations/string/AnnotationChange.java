package se.l4.otter.operations.string;

import java.util.Set;

import se.l4.otter.operations.ValueChange;

public interface AnnotationChange
{
	/**
	 * Get if this change is actually empty.
	 *
	 * @return
	 */
	boolean isEmpty();

	/**
	 * Get if the given key has changed.
	 *
	 * @param key
	 * @return
	 */
	boolean containsKey(String key);

	/**
	 * Get the new value of the given key.
	 *
	 * @param key
	 */
	<T> T get(String key);

	/**
	 * Get the recored change for the give key.
	 *
	 * @param key
	 * @return
	 */
	ValueChange getChange(String key);

	/**
	 * Get if the given key is being removed.
	 *
	 * @param key
	 * @return
	 */
	boolean isRemoval(String key);

	/**
	 * Get all of the keys that have changed.
	 *
	 * @return
	 */
	Set<String> keys();

	/**
	 * Invert the change.
	 *
	 * @return
	 */
	AnnotationChange invert();
}
