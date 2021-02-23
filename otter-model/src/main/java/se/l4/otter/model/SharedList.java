package se.l4.otter.model;

import java.util.Collection;

public interface SharedList<T>
	extends SharedObject
{
	/**
	 * Clear all items in this list.
	 *
	 */
	void clear();

	/**
	 * Add an item to the end of this list.
	 *
	 * @param item
	 */
	void add(T item);

	/**
	 * Add all of the given items to the end of this list.
	 *
	 * @param items
	 */
	void addAll(Collection<? extends T> items);

	/**
	 * Insert an item at the given position.
	 *
	 * @param index
	 * @param item
	 */
	void insert(int index, T item);

	/**
	 * Insert all of the items at the given position.
	 *
	 * @param index
	 * @param items
	 */
	void insertAll(int index, Collection<? extends T> items);

	/**
	 * Remove the item at the given position.
	 *
	 * @param index
	 */
	void remove(int index);

	/**
	 * Remove several items at the same time.
	 *
	 * @param fromIndex
	 *   the index of the first item to remove (inclusive)
	 * @param toIndex
	 *   the index to stop removing at (exclusive)
	 */
	void removeRange(int fromIndex, int toIndex);

	/**
	 * Replace the item at the given index with a new value.
	 *
	 * @param index
	 * @param value
	 */
	void set(int index, T value);

	/**
	 * Get if the list currently contains the given value.
	 *
	 * @param value
	 * @return
	 */
	boolean contains(T value);

	/**
	 * Get the item at the specified index.
	 *
	 * @param index
	 * @return
	 */
	T get(int index);

	/**
	 * Get the number of items currently in this list.
	 *
	 * @return
	 */
	int length();
}
