package se.l4.otter.model;

import java.util.HashMap;
import java.util.Map;

import se.l4.otter.engine.Editor;
import se.l4.otter.model.internal.SharedMapImpl;
import se.l4.otter.model.internal.SharedObjectEditorImpl;
import se.l4.otter.model.internal.SharedStringImpl;
import se.l4.otter.model.spi.HasApply;
import se.l4.otter.model.spi.SharedObjectEditor;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.combined.CombinedDelta;
import se.l4.otter.operations.combined.CombinedTarget;
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
	private final Editor<Operation<CombinedTarget>> editor;
	private final CombinedType otType;
	
	private final Map<String, SharedObject> objects;
	private final Map<String, Operation<?>> values;
	
	private int latestId;
	private final SharedMap root;

	/**
	 * Create a new model over the given editor.
	 * 
	 * @param editor
	 */
	public DefaultModel(Editor<Operation<CombinedTarget>> editor)
	{
		this.editor = editor;
		this.otType = (CombinedType) editor.getType();
		
		objects = new HashMap<>();
		values = new HashMap<>();
		
		CombinedTarget handler = createHandler();
		editor.getCurrent().apply(handler);
		editor.addChangeListener(op -> op.apply(handler));
		
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
	
	private CombinedTarget createHandler()
	{
		return new CombinedTarget()
		{
			@Override
			public void update(String id, String type, Operation<?> change)
			{
				if(values.containsKey(id))
				{
					Operation<?> current = values.get(id);
					Operation<?> composed = otType.compose(type, current, change);
					values.put(id, composed);
					
					SharedObject object = objects.get(id);
					((HasApply) object).apply(change);
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
		// TODO: Support for combining related operations when requested
		
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
		return new SharedObjectEditorImpl<>(
			this,
			id,
			type,
			() -> (Op) values.get(id),
			op -> apply(id, type, op)
		);
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
		switch(type)
		{
			case "string":
				return new SharedStringImpl(create(id, type));
			case "map":
				return new SharedMapImpl(create(id, type));
			default:
				throw new OperationException("Unknown type: " + type);
		}
	}
	
	private String nextId()
	{
		return editor.getId() + "-" + latestId;
	}
	
	@Override
	public SharedMap newMap()
	{
		return null;
	}
	
	@Override
	public SharedString newString()
	{
		String id = nextId();
		values.put(id, CompoundOperation.empty());
		
		SharedString result = (SharedString) createObject(id, "string");
		objects.put(id, result);
		return result;
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
