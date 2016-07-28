package se.l4.otter.model;

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
