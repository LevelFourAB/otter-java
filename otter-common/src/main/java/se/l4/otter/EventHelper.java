package se.l4.otter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Helper for triggering events.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class EventHelper<T>
{
	private final List<T> listeners;

	public EventHelper()
	{
		listeners = new ArrayList<>();
	}

	/**
	 * Add a listener that should be triggered.
	 *
	 * @param listener
	 */
	public void add(T listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove a listener that should no longer be triggered.
	 *
	 * @param listener
	 */
	public void remove(T listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Trigger a listener by calling the given consumer for every registered
	 * listener.
	 *
	 * @param c
	 */
	public void trigger(Consumer<T> c)
	{
		for(T listener : listeners)
		{
			c.accept(listener);
		}
	}
}
