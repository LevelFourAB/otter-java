package se.l4.otter.operations.internal.list;

import java.util.Arrays;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.list.ListHandler;

/**
 * Operation on a list that deletes one or more items.
 *
 * @author Andreas Holstenson
 *
 */
public class ListDelete
	implements Operation<ListHandler>
{
	private final Object[] items;

	public ListDelete(Object[] items)
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
			target.delete(o);
		}
	}

	@Override
	public Operation<ListHandler> invert()
	{
		return new ListInsert(items);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[items=" + Arrays.toString(items) + "]";
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
		ListDelete other = (ListDelete) obj;
		if(!Arrays.equals(items, other.items))
			return false;
		return true;
	}

}
