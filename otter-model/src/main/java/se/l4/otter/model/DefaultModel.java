package se.l4.otter.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.l4.otter.engine.Editor;
import se.l4.otter.engine.EditorListener;
import se.l4.otter.engine.events.ChangeEvent;
import se.l4.otter.lock.CloseableLock;
import se.l4.otter.model.internal.SharedObjectEditorImpl;
import se.l4.otter.model.spi.SharedObjectEditor;
import se.l4.otter.model.spi.SharedObjectFactory;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.combined.CombinedDelta;
import se.l4.otter.operations.combined.CombinedHandler;
import se.l4.otter.operations.combined.CombinedType;

/**
 * This is the default implementation of {@link Model} on top of an instance
 * of {@link Editor}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultModel
	implements Model
{
	private final Editor<Operation<CombinedHandler>> editor;
	private final CombinedType otType;
	private final Map<String, SharedObjectFactory<?, ?>> types;
	
	private final Map<String, SharedObjectEditorImpl> editors;
	private final Map<String, SharedObject> objects;
	private final Map<String, Operation<?>> values;
	
	private int latestId;
	private final SharedMap root;
	
	private final List<Runnable> queuedEvents;
	
	/**
	 * Create a new model over the given editor.
	 * 
	 * @param editor
	 * @param types 
	 */
	public DefaultModel(Editor<Operation<CombinedHandler>> editor, Map<String, SharedObjectFactory<?, ?>> types)
	{
		this.editor = editor;
		this.types = types;
		this.otType = (CombinedType) editor.getType();
		
		queuedEvents = new ArrayList<>();
		
		editors = new HashMap<>();
		objects = new HashMap<>();
		values = new HashMap<>();
		
		CombinedHandler handler = createHandler();
		
		try(CloseableLock lock = editor.lock())
		{
			Operation<CombinedHandler> current = editor.getCurrent();
			if(current != null)
			{
				current.apply(handler);
			}
			
			editor.addListener(new EditorListener<Operation<CombinedHandler>>()
			{
				@Override
				public void editorChanged(ChangeEvent<Operation<CombinedHandler>> event)
				{
					if(event.isRemote())
					{
						try(CloseableLock lock = lock())
						{
							/*
							 * Perform operation in a lock to ensure that events
							 * are triggered after the apply has finished.
							 */
							event.getOperation().apply(handler);
						}
					}
				}
			});
		}
		
		root = (SharedMap) getObject("root", "map");
	}
	
	@Override
	public String getObjectId()
	{
		return "root";
	}
	
	@Override
	public String getObjectType()
	{
		return "map";
	}
	
	private CombinedHandler createHandler()
	{
		return new CombinedHandler()
		{
			@Override
			public void update(String id, String type, Operation<?> change)
			{
				if(values.containsKey(id))
				{
					Operation<?> current = values.get(id);
					Operation<?> composed = otType.compose(type, current, change);
					values.put(id, composed);
					
					SharedObjectEditorImpl editor = editors.get(id);
					editor.operationApplied(change, false);
				}
				else
				{
					values.put(id, change);
					
					SharedObject object = createObject(id, type);
					objects.put(id, object);
				}
			}
		};
	}
	
	private void apply(String id, String type, Operation<?> op)
	{
		try(CloseableLock lock = lock())
		{
			SharedObjectEditorImpl objectEditor = editors.get(id);
			if(values.containsKey(id))
			{
				Operation<?> current = values.get(id);
				Operation<?> composed = otType.compose(type, current, op);
				values.put(id, composed);
			}
			else
			{
				values.put(id, op);
			}
			
			editor.apply(
				CombinedDelta.builder()
					.update(id, type, op)
					.done()
			);
			
			if(objectEditor != null)
			{
				objectEditor.operationApplied(op, false);
			}
		}
	}
	
	@Override
	public CloseableLock lock()
	{
		CloseableLock lock = editor.lock();
		return new CloseableLock()
		{
			@Override
			public void close()
			{
				try
				{
					try
					{
						for(Runnable r : queuedEvents)
						{
							r.run();
						}
					}
					finally
					{
						queuedEvents.clear();
					}
				}
				finally
				{
					lock.close();
				}
			}
		};
	}
	
	@Override
	public void close()
	{
		editor.close();
	}
	
	/**
	 * Create an instance of {@link SharedObjectEditor} for the given id
	 * and type.
	 * 
	 * @param id
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <Op extends Operation<?>> SharedObjectEditor<Op> create(String id, String type)
	{
		SharedObjectEditorImpl<Op> editor = new SharedObjectEditorImpl<>(
			this,
			id,
			type,
			() -> (Op) values.get(id),
			op -> apply(id, type, op),
			queuedEvents::add
		);
		
		editors.put(id, editor);
		return editor;
	}
	
	public SharedObject getObject(String id, String type)
	{
		SharedObject object = objects.get(id);
		if(object == null)
		{
			values.put(id, CompoundOperation.empty());
			object = createObject(id, type);
			objects.put(id, object);
		}
		
		return object;
	}
	
	private SharedObject createObject(String id, String type)
	{
		SharedObjectFactory<?, ?> factory = types.get(type);
		if(factory == null)
		{
			throw new OperationException("Unknown type: " + type);
		}
		
		return factory.create(create(id, type));
	}
	
	private <T extends SharedObject> T initObject(String type)
	{
		String id = nextId();
		values.put(id, CompoundOperation.empty());
		
		SharedObject result = createObject(id, type);
		objects.put(id, result);
		return (T) result;
	}
	
	private String nextId()
	{
		return editor.getId() + "-" + latestId;
	}
	
	@Override
	public SharedMap newMap()
	{
		return initObject("map");
	}
	
	@Override
	public SharedString newString()
	{
		return initObject("string");
	}
	
	@Override
	public <T> SharedList<T> newList()
	{
		return initObject("list");
	}
	
	@Override
	public <T extends SharedObject> T newObject(String type, Class<T> objectType)
	{
		return initObject(type);
	}
	
	@Override
	public boolean containsKey(String key)
	{
		return root.containsKey(key);
	}
	
	@Override
	public <T> T get(String key)
	{
		return root.get(key);
	}
	
	@Override
	public void remove(String key)
	{
		root.remove(key);
	}
	
	@Override
	public void set(String key, Object value)
	{
		root.set(key, value);
	}
	
	@Override
	public void addChangeListener(Listener listener)
	{
		root.addChangeListener(listener);
	}
	
	@Override
	public void removeChangeListener(Listener listener)
	{
		root.removeChangeListener(listener);
	}
}
