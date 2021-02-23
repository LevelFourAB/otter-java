package se.l4.otter.operations.internal.string;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringHandler;

/**
 * Operation on a {@link String} where a number of characters are retained.
 *
 * @author Andreas Holstenson
 *
 */
public class StringRetain
	implements Operation<StringHandler>
{
	private final int length;

	public StringRetain(int count)
	{
		this.length = count;
	}

	public int getLength()
	{
		return length;
	}

	@Override
	public void apply(StringHandler target)
	{
		target.retain(length);
	}

	@Override
	public Operation<StringHandler> invert()
	{
		return this;
	}

	@Override
	public String toString()
	{
		return "_" + length;
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
		StringRetain other = (StringRetain) obj;
		if(length != other.length)
			return false;
		return true;
	}
}
