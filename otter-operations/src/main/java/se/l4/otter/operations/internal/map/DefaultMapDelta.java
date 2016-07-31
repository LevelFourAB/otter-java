package se.l4.otter.operations.internal.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import se.l4.otter.operations.DefaultCompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.map.MapDelta;
import se.l4.otter.operations.map.MapHandler;

/**
 * Default implementation of {@link MapDelta}. Supports multiple {@link #set(String, Object, Object)}
 * operations by tracking them in a local map and using that to create the
 * operation list.
 * 
 * @author Andreas Holstenson
 *
 * @param <ReturnPath>
 */
public class DefaultMapDelta<ReturnPath>
	implements MapDelta<ReturnPath>
{
	private final Function<Operation<MapHandler>, ReturnPath> resultHandler;
	private final Map<String, ValuePair> changes;

	public DefaultMapDelta(Function<Operation<MapHandler>, ReturnPath> resultHandler)
	{
		this.resultHandler = resultHandler;
		
		changes = new HashMap<>();
	}
	
	@Override
	public MapDelta<ReturnPath> set(String key, Object oldValue, Object newValue)
	{
		if(changes.containsKey(key))
		{
			ValuePair pair = changes.get(key);
			if(! Objects.equals(oldValue, pair.newValue))
			{
				throw new OperationException("Trying to set key `" + key + "`, but given old value does not match previous set; " + oldValue + " != " + pair.newValue);
			}
			
			changes.put(key, new ValuePair(pair.oldValue, newValue));
		}
		else
		{
			changes.put(key,  new ValuePair(oldValue, newValue));
		}
		return this;
	}
	
	@Override
	public ReturnPath done()
	{
		List<Operation<MapHandler>> operations = new ArrayList<>();
		for(Map.Entry<String, ValuePair> e : changes.entrySet())
		{
			operations.add(new MapSet(e.getKey(), e.getValue().oldValue, e.getValue().newValue));
		}
		
		return resultHandler.apply(new DefaultCompoundOperation<>(operations));
	}
	
	private static class ValuePair
	{
		private final Object oldValue;
		private final Object newValue;
		
		public ValuePair(Object oldValue, Object newValue)
		{
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
	}
}
