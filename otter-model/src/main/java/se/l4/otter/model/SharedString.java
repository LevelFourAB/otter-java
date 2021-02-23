package se.l4.otter.model;

public interface SharedString
	extends SharedObject
{
	/**
	 * Get the current value of the string.
	 *
	 * @return
	 */
	String get();

	/**
	 * Set the value of this string.
	 *
	 * @param newValue
	 */
	void set(String newValue);

	/**
	 * Append a value to the string.
	 *
	 * @param text
	 */
	void append(String value);

	/**
	 * Remove text between the given indexes.
	 *
	 * @param fromIndex
	 *   index to start removing from (inclusive)
	 * @param toIndex
	 *   index to end removing at (exclusive)
	 */
	void remove(int fromIndex, int toIndex);

	/**
	 * Insert text at the given index.
	 *
	 * @param idx
	 * @param value
	 */
	void insert(int idx, String value);
}
