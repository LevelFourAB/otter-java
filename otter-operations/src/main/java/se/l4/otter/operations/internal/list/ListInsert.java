package se.l4.otter.operations.internal.list;

import java.util.Arrays;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.list.ListHandler;

/**
 * Operation on a {@link String} where some characters are inserted into the
 * string at the current position.
 *
 * @author Andreas Holstenson
 *
 */
public class ListInsert
	implements Operation<ListHandler>
{
	private final Object[] items;

	public ListInsert(Object[] items)
	{
		this.items = items;
	}

	public Object[] getItems()
	{
		return items;
	}

	@Override
	public void apply(ListHandler target)
	{
		for(Object o : items)
		{
			target.insert(o);
		}
	}

	@Override
	public Operation<ListHandler> invert()
	{
		return new ListDelete(items);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[value=" + Arrays.toString(items) + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(items);
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
		ListInsert other = (ListInsert) obj;
		if(!Arrays.equals(items, other.items))
			return false;
		return true;
	}
}
