package se.l4.otter.operations.string;

/**
 * Builder for changes to annotations.
 *
 * @author Andreas Holstenson
 *
 * @param <ReturnPath>
 */
public interface AnnotationChangeBuilder<ReturnPath>
{
	/**
	 * Set the value of the given annotation key.
	 *
	 * @param key
	 *   the key to set
	 * @param oldValue
	 *   the previous value of the annotation
	 * @param newValue
	 *   the new value of the annotation
	 * @return
	 */
	AnnotationChangeBuilder<ReturnPath> set(String key, Object oldValue, Object newValue);

	/**
	 * Remove the given annotation key.
	 *
	 * @param key
	 *   the key to remove
	 * @param currentValue
	 *   the current value of the annotation key
	 * @return
	 */
	AnnotationChangeBuilder<ReturnPath> remove(String key, Object currentValue);

	/**
	 * Build the change and return to the previous builder.
	 *
	 * @return
	 */
	ReturnPath done();
}
