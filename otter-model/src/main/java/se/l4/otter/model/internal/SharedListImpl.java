package se.l4.otter.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.l4.otter.lock.CloseableLock;
import se.l4.otter.model.SharedList;
import se.l4.otter.model.spi.AbstractSharedObject;
import se.l4.otter.model.spi.DataValues;
import se.l4.otter.model.spi.SharedObjectEditor;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.list.ListDelta;
import se.l4.otter.operations.list.ListHandler;

public class SharedListImpl<T>
	extends AbstractSharedObject<Operation<ListHandler>>
	implements SharedList<T>
{
	private final List<T> values;
	
	public SharedListImpl(SharedObjectEditor<Operation<ListHandler>> editor)
	{
		super(editor);
		
		values = new ArrayList<>();
		
		editor.getCurrent().apply(new ListHandler()
		{
			@Override
			public void retain(int length)
			{
				throw new OperationException("Latest value invalid, must only contain inserts.");
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void insert(Object item)
			{
				values.add((T) DataValues.fromData(editor, item));
			}
			
			@Override
			public void delete(Object item)
			{
				throw new OperationException("Latest value invalid, must only contain inserts.");
			}
		});
		
		editor.setOperationHandler(this::apply);
	}
	
	private void apply(Operation<ListHandler> op, boolean local)
	{
		op.apply(new ListHandler()
		{
			int index = 0;
			
			@Override
			public void retain(int length)
			{
				index += length;
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void insert(Object item)
			{
				values.add(index, (T) DataValues.fromData(editor, item));
				index += 1;
			}
			
			@Override
			public void delete(Object item)
			{
				values.remove(index);
			}
		});
	}
	
	@Override
	public int length()
	{
		return values.size();
	}
	
	@Override
	public T get(int index)
	{
		if(index >= values.size())
		{
            throw new IndexOutOfBoundsException("Index must be less than length. Was " + index + " but length is " + values.size());
		}
		
		return values.get(index);
	}
	
	@Override
	public boolean contains(T value)
	{
		return values.contains(value);
	}
	
	@Override
	public void clear()
	{
		try(CloseableLock lock = editor.lock())
		{
			ListDelta<Operation<ListHandler>> delta = ListDelta.builder();
			for(T item : values)
			{
				delta.delete(DataValues.toData(item));
			}
			
			editor.apply(delta.done());
		}
	}
	
	@Override
	public void add(T item)
	{
		try(CloseableLock lock = editor.lock())
		{
			editor.apply(ListDelta.builder()
				.retain(values.size())
				.insert(DataValues.toData(item))
				.done()
			);
		}
	}
	
	@Override
	public void addAll(Collection<? extends T> items)
	{
		try(CloseableLock lock = editor.lock())
		{
			ListDelta<Operation<ListHandler>> delta = ListDelta.builder()
				.retain(values.size());
			
			for(T item : items)
			{
				delta.insert(DataValues.toData(item));
			}
			
			editor.apply(delta.done());
		}
	}
	
	@Override
	public void insert(int index, T item)
	{
		try(CloseableLock lock = editor.lock())
		{
			int length = length();
			editor.apply(ListDelta.builder()
				.retain(index)
				.insert(DataValues.toData(item))
				.retain(length - index)
				.done()
			);
		}
	}
	
	@Override
	public void insertAll(int index, Collection<? extends T> items)
	{
		try(CloseableLock lock = editor.lock())
		{
			int length = length();
			ListDelta<Operation<ListHandler>> delta = ListDelta.builder()
				.retain(index);
			
			for(T item : items)
			{
				delta.insert(DataValues.toData(item));
			}
			
			delta.retain(length - index);
			editor.apply(delta.done());
		}
	}
	
	@Override
	public void remove(int index)
	{
		try(CloseableLock lock = editor.lock())
		{
			int length = length();
			editor.apply(ListDelta.builder()
				.retain(index)
				.delete(DataValues.toData(values.get(index)))
				.retain(length - index - 1)
				.done()
			);
		}
	}
	
	@Override
	public void removeRange(int fromIndex, int toIndex)
	{
		try(CloseableLock lock = editor.lock())
		{
			int length = length();
			ListDelta<Operation<ListHandler>> delta = ListDelta.builder()
				.retain(fromIndex);
			
			for(int i=fromIndex; i<toIndex; i++)
			{
				delta.delete(DataValues.toData(values.get(i)));
			}
			
			delta.retain(length - (toIndex - fromIndex));
			editor.apply(delta.done());
		}
	}
	
	@Override
	public void set(int index, T value)
	{
		try(CloseableLock lock = editor.lock())
		{
			int length = length();
			editor.apply(ListDelta.builder()
				.retain(index)
				.insert(DataValues.toData(value))
				.delete(DataValues.toData(values.get(index)))
				.retain(length - index - 1)
				.done()
			);
		}
	}
}
