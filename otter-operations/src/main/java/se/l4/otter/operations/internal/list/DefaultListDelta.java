package se.l4.otter.operations.internal.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.list.ListDelta;
import se.l4.otter.operations.list.ListHandler;

/**
 * Default implementation of {@link ListDelta}.
 *
 * @author Andreas Holstenson
 *
 * @param <ReturnPath>
 */
public class DefaultListDelta<ReturnPath>
	implements ListDelta<ReturnPath>
{
	private enum State
	{
		RETAIN,
		INSERT,
		DELETE
	}

	private final Function<Operation<ListHandler>, ReturnPath> resultHandler;
	private final List<Operation<ListHandler>> ops;

	private State state;
	private int retainCount;
	private final List<Object> buffer;

	public DefaultListDelta(Function<Operation<ListHandler>, ReturnPath> resultHandler)
	{
		this.resultHandler = resultHandler;

		ops = new ArrayList<>();
		buffer = new ArrayList<>();
	}

	private void switchState(State state)
	{
		if(this.state != state)
		{
			flush();
		}

		this.state = state;
	}

	private void flush()
	{
		if(this.state == null) return;

		switch(this.state)
		{
			case RETAIN:
				if(retainCount > 0)
				{
					ops.add(new ListRetain(retainCount));
					retainCount = 0;
				}
				break;
			case INSERT:
				ops.add(new ListInsert(buffer.toArray()));
				buffer.clear();
				break;
			case DELETE:
				ops.add(new ListDelete(buffer.toArray()));
				buffer.clear();
				break;
		}
	}

	@Override
	public ListDelta<ReturnPath> retain(int count)
	{
		switchState(State.RETAIN);
		retainCount += count;
		return this;
	}

	@Override
	public ListDelta<ReturnPath> insert(Object item)
	{
		switchState(State.INSERT);
		buffer.add(item);
		return this;
	}

	@Override
	public ListDelta<ReturnPath> insertMultiple(Collection<? extends Object> items)
	{
		switchState(State.INSERT);
		buffer.addAll(items);
		return this;
	}

	@Override
	public ListDelta<ReturnPath> insertMultiple(Object... items)
	{
		switchState(State.INSERT);
		for(Object o : items)
		{
			buffer.add(o);
		}
		return this;
	}

	@Override
	public ListDelta<ReturnPath> delete(Object current)
	{
		switchState(State.DELETE);
		buffer.add(current);
		return this;
	}

	@Override
	public ListDelta<ReturnPath> deleteMultiple(Collection<? extends Object> current)
	{
		switchState(State.DELETE);
		buffer.addAll(current);
		return this;
	}

	@Override
	public ListDelta<ReturnPath> deleteMultiple(Object... current)
	{
		switchState(State.DELETE);
		for(Object o : current)
		{
			buffer.add(o);
		}
		return this;
	}

	@Override
	public ListDelta<ReturnPath> adopt(Operation<ListHandler> op)
	{
		if(op instanceof ListRetain)
		{
			this.retain(((ListRetain) op).getLength());
		}
		else if(op instanceof ListInsert)
		{
			this.insertMultiple(((ListInsert) op).getItems());
		}
		else if(op instanceof ListDelete)
		{
			this.deleteMultiple(((ListDelete) op).getItems());
		}
		else
		{
			throw new IllegalArgumentException("Unsupported operation: " + op);
		}

		return this;
	}

	@Override
	public ReturnPath done()
	{
		flush();
		return resultHandler.apply(CompoundOperation.create(ops));
	}
}
