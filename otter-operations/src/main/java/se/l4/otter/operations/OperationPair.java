package se.l4.otter.operations;

/**
 * A pair of {@link Operation}s.
 *
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public class OperationPair<Op extends Operation<?>>
{
	private final Op first;
	private final Op second;

	public OperationPair(Op first, Op second)
	{
		this.first = first;
		this.second = second;
	}

	public Op getLeft()
	{
		return first;
	}

	public Op getRight()
	{
		return second;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[left=" + first + ", right=" + second + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
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
		OperationPair other = (OperationPair) obj;
		if(first == null)
		{
			if(other.first != null)
				return false;
		}
		else if(!first.equals(other.first))
			return false;
		if(second == null)
		{
			if(other.second != null)
				return false;
		}
		else if(!second.equals(other.second))
			return false;
		return true;
	}
}
