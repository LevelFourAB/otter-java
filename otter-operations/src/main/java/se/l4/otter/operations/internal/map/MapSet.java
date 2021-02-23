package se.l4.otter.operations.internal.map;

import java.util.Map;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.map.MapHandler;

/**
 * Operation that sets a value in a {@link Map}.
 *
 * @author Andreas Holstenson
 *
 */
public class MapSet
	implements MapOperation
{
	private final String key;
	private final Object oldValue;
	private final Object newValue;

	public MapSet(String key, Object oldValue, Object newValue)
	{
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	public Object getNewValue()
	{
		return newValue;
	}

	public Object getOldValue()
	{
		return oldValue;
	}

	@Override
	public void apply(MapHandler handler)
	{
		if(newValue == null)
		{
			handler.remove(key, oldValue);
		}
		else
		{
			handler.put(key, oldValue, newValue);
		}
	}

	@Override
	public Operation<MapHandler> invert()
	{
		return new MapSet(key, newValue, oldValue);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[key=" + key + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		MapSet other = (MapSet) obj;
		if(key == null)
		{
			if(other.key != null)
				return false;
		}
		else if(!key.equals(other.key))
			return false;
		if(newValue == null)
		{
			if(other.newValue != null)
				return false;
		}
		else if(!newValue.equals(other.newValue))
			return false;
		if(oldValue == null)
		{
			if(other.oldValue != null)
				return false;
		}
		else if(!oldValue.equals(other.oldValue))
			return false;
		return true;
	}

}
