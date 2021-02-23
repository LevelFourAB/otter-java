package se.l4.otter.operations.internal.list;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.list.ListHandler;

/**
 * Retain a number of items in a list.
 *
 * @author Andreas Holstenson
 *
 */
public class ListRetain
	implements Operation<ListHandler>
{
	private final int length;

	public ListRetain(int count)
	{
		this.length = count;
	}

	public int getLength()
	{
		return length;
	}

	@Override
	public void apply(ListHandler target)
	{
		target.retain(length);
	}

	@Override
	public Operation<ListHandler> invert()
	{
		return this;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[length=" + length+ "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
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
		ListRetain other = (ListRetain) obj;
		if(length != other.length)
			return false;
		return true;
	}
}
