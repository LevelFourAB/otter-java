package se.l4.otter.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link CompoundOperation}.
 *
 * @author Andreas Holstenson
 *
 * @param <Handler>
 */
public class DefaultCompoundOperation<Handler>
	implements CompoundOperation<Handler>
{
	private final List<Operation<Handler>> operations;

	public DefaultCompoundOperation(List<Operation<Handler>> operations)
	{
		this.operations = Collections.unmodifiableList(new ArrayList<>(operations));
	}

	@Override
	public void apply(Handler target)
	{
		for(Operation<Handler> op : operations)
		{
			op.apply(target);
		}
	}

	@Override
	public List<Operation<Handler>> getOperations()
	{
		return operations;
	}

	@Override
	public Operation<Handler> invert()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + operations;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operations == null) ? 0 : operations.hashCode());
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
		DefaultCompoundOperation other = (DefaultCompoundOperation) obj;
		if(operations == null)
		{
			if(other.operations != null)
				return false;
		}
		else if(!operations.equals(other.operations))
			return false;
		return true;
	}
}
