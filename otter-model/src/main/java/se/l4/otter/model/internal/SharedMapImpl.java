package se.l4.otter.model.internal;

import java.util.HashMap;
import java.util.Map;

import se.l4.otter.model.SharedMap;
import se.l4.otter.model.SharedObject;
import se.l4.otter.model.spi.EventHelper;
import se.l4.otter.model.spi.HasApply;
import se.l4.otter.model.spi.SharedObjectEditor;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.data.DataArray;
import se.l4.otter.operations.map.MapDelta;
import se.l4.otter.operations.map.MapOperationHandler;

public class SharedMapImpl
	implements SharedMap, HasApply<Operation<MapOperationHandler>>
{
	private final SharedObjectEditor<Operation<MapOperationHandler>> editor;
	private final Map<String, Object> values;
	
	private final MapOperationHandler handler;
	private final EventHelper<Listener> changeListeners;

	public SharedMapImpl(SharedObjectEditor<Operation<MapOperationHandler>> editor)
	{
		this.editor = editor;
		
		values = new HashMap<>();
		
		changeListeners = new EventHelper<>();
		handler = createHandler();
		
		editor.getCurrent().apply(handler);
	}
	
	private MapOperationHandler createHandler()
	{
		return new MapOperationHandler()
		{
			@Override
			public void remove(String key, Object oldValue)
			{
				Object old = values.remove(key);
				changeListeners.trigger(l -> l.valueRemoved(key, old));
			}
			
			@Override
			public void put(String key, Object oldValue, Object newValue)
			{
				DataArray array = (DataArray) newValue;
				String type = (String) array.get(0);
				
				Object value;
				switch(type)
				{
					case "ref":
						value = editor.getObject(
							(String) array.get(1),
							(String) array.get(2)
						);
						break;
					case "value":
						value = array.get(1);
						break;
					default:
						throw new OperationException("Value of shared map " + editor.getId() + " has unknown type of value");
				}
				
				Object old = values.put(key, value);
				changeListeners.trigger(l -> l.valueChanged(key, old, value));
			}
		};
	}
	
	@Override
	public void apply(Operation<MapOperationHandler> op)
	{
		op.apply(handler);
	}
	
	@Override
	public String getObjectId()
	{
		return editor.getId();
	}
	
	@Override
	public String getObjectType()
	{
		return editor.getType();
	}
	
	@Override
	public boolean containsKey(String key)
	{
		return values.containsKey(key);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key)
	{
		return (T) values.get(key);
	}
	
	private Object toValue(Object value)
	{
		DataArray result = new DataArray();
		if(value instanceof SharedObject)
		{
			result.add("ref");
			result.add(((SharedObject) value).getObjectId());
			result.add(((SharedObject) value).getObjectType());
		}
		else
		{
			// TODO: Better value validation
			
			result.add("value");
			result.add(value);
		}
		
		return result;
	}
	
	@Override
	public void remove(String key)
	{
		Object value = values.remove(key);
		if(value != null)
		{
			editor.send(MapDelta.builder()
				.set(key, toValue(value), null)
				.done()
			);
		}
	}
	
	@Override
	public void set(String key, Object value)
	{
		if(value == null)
		{
			throw new IllegalArgumentException("null values are currently not supported");
		}
		
		Object old = values.put(key, value);
		editor.send(MapDelta.builder()
			.set(key, toValue(old), toValue(value))
			.done()
		);
	}
	
	@Override
	public void addChangeListener(Listener listener)
	{
		changeListeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(Listener listener)
	{
		changeListeners.remove(listener);
	}
}
