package se.l4.otter.model.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventHelper<T>
{
	private final List<T> listeners;
	
	public EventHelper()
	{
		listeners = new ArrayList<>();
	}
	
	public void add(T listener)
	{
		listeners.add(listener);
	}
	
	public void remove(T listener)
	{
		listeners.remove(listener);
	}
	
	public void trigger(Consumer<T> c)
	{
		for(T listener : listeners)
		{
			c.accept(listener);
		}
	}
}
