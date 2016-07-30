package se.l4.otter.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.l4.otter.model.AbstractSharedObject;
import se.l4.otter.model.SharedList;
import se.l4.otter.model.spi.HasApply;
import se.l4.otter.model.spi.SharedObjectEditor;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.list.ListDelta;
import se.l4.otter.operations.list.ListHandler;

public class SharedListImpl<T>
	extends AbstractSharedObject<Operation<ListHandler>>
	implements SharedList<T>, HasApply<Operation<ListHandler>>
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
				values.add((T) item);
			}
			
			@Override
			public void delete(Object item)
			{
				throw new OperationException("Latest value invalid, must only contain inserts.");
			}
		});
	}
	
	@Override
	public void apply(Operation<ListHandler> op)
	{
		System.out.println(" <- " + op);
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
				values.add(index, (T) item);
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
	
	private void applyAndSend(Operation<ListHandler> op)
	{
		apply(op);
		editor.send(op);
	}
	
	@Override
	public void clear()
	{
		ListDelta<Operation<ListHandler>> delta = ListDelta.builder();
		for(T item : values)
		{
			delta.delete(item);
		}
		
		applyAndSend(delta.done());
	}
	
	@Override
	public void add(T item)
	{
		System.out.println(values);
		
		applyAndSend(ListDelta.builder()
			.retain(values.size())
			.insert(item)
			.done()
		);
	}
	
	@Override
	public void addAll(Collection<? extends T> items)
	{
		ListDelta<Operation<ListHandler>> delta = ListDelta.builder()
			.retain(values.size());
		
		for(T item : items)
		{
			delta.insert(item);
		}
		
		applyAndSend(delta.done());
	}
	
	@Override
	public void insert(int index, T item)
	{
		int length = length();
		applyAndSend(ListDelta.builder()
			.retain(index)
			.insert(item)
			.retain(length - index)
			.done()
		);
	}
	
	@Override
	public void insertAll(int index, Collection<? extends T> items)
	{
		int length = length();
		ListDelta<Operation<ListHandler>> delta = ListDelta.builder()
			.retain(index);
		
		for(T item : items)
		{
			delta.insert(item);
		}
		
		delta.retain(length - index);
		applyAndSend(delta.done());
	}
	
	@Override
	public void remove(int index)
	{
		int length = length();
		applyAndSend(ListDelta.builder()
			.retain(index)
			.delete(values.get(index))
			.retain(length - index - 1)
			.done()
		);
	}
	
	@Override
	public void removeRange(int fromIndex, int toIndex)
	{
		int length = length();
		ListDelta<Operation<ListHandler>> delta = ListDelta.builder()
			.retain(fromIndex);
		
		for(int i=fromIndex; i<toIndex; i++)
		{
			delta.delete(values.get(i));
		}
		
		delta.retain(length - (toIndex - fromIndex));
		applyAndSend(delta.done());
	}
	
	@Override
	public void set(int index, T value)
	{
		int length = length();
		applyAndSend(ListDelta.builder()
			.retain(index)
			.insert(value)
			.delete(values.get(index))
			.retain(length - index - 1)
			.done()
		);
	}
}
