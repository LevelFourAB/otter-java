package se.l4.otter.operations.map;

import java.util.Map;

/**
 * Handler for operations on {@link Map}.
 *
 * @author Andreas Holstenson
 *
 */
public interface MapHandler
{
	/**
	 * Set the key to the given value.
	 *
	 * @param key
	 * @param value
	 * @see Map#put(Object, Object)
	 */
	void put(String key, Object oldValue, Object newValue);

	/**
	 * Remove the given key.
	 *
	 * @param key
	 * @see Map#remove(Object)
	 */
	void remove(String key, Object oldValue);

	/**
	 * Create a new handler over the given map.
	 *
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static MapHandler over(Map<String, ?> map)
	{
		Map m = (Map) map;
		return new MapHandler()
		{
			@Override
			public void put(String key, Object oldValue, Object value)
			{
				m.put(key, value);
			}

			@Override
			public void remove(String key, Object oldValue)
			{
				m.remove(key);
			}
		};
	}
}
