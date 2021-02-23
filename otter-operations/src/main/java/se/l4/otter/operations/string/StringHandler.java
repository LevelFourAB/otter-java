package se.l4.otter.operations.string;

/**
 * Handler for string operations.
 *
 * @author Andreas Holstenson
 *
 */
public interface StringHandler
{
	/**
	 * Insert characters.
	 *
	 * @param s
	 */
	void insert(String s);

	/**
	 * Delete characters.
	 *
	 * @param s
	 */
	void delete(String s);

	/**
	 * Retain a number of characters.
	 *
	 * @param count
	 */
	void retain(int count);

	/**
	 * Annotations are being changed at the current index.
	 *
	 * @param change
	 */
	void annotationUpdate(AnnotationChange change);
}
