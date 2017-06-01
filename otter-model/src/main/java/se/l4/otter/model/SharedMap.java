package se.l4.otter.model;

import java.util.function.Supplier;

public interface SharedMap
	extends SharedObject
{
	/**
	 * Check if this map contains the given key.
	 *
	 * @param key
	 * @return
	 */
	boolean containsKey(String key);

	/**
	 * Get a value in this map.
	 *
	 * @param key
	 * @return
	 */
	<T> T get(String key);


	/**
	 * Get the item with the specified key or create it via the given supplier.
	 *
	 * @param index
	 * @param supplier
	 * @return
	 */
	default <T> T get(String key, Supplier<T> supplier)
	{
		if(containsKey(key))
		{
			return get(key);
		}
		else
		{
			T result = supplier.get();
			set(key, result);
			return result;
		}
	}

	/**
	 * Set a value in this map.
	 *
	 * @param key
	 * @param value
	 */
	void set(String key, Object value);

	/**
	 * Remove a key from this map.
	 *
	 * @param key
	 */
	void remove(String key);

	/**
	 * Add a listener that will be triggered when an external change
	 * occurs.
	 *
	 * @param listener
	 */
	void addChangeListener(Listener listener);

	/**
	 * Remove a change listener.
	 *
	 * @param listener
	 */
	void removeChangeListener(Listener listener);

	interface Listener
	{
		void valueChanged(String key, Object oldValue, Object newValue);

		void valueRemoved(String key, Object oldValue);
	}
}
