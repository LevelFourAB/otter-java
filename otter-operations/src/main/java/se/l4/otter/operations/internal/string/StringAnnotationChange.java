package se.l4.otter.operations.internal.string;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.AnnotationChange;
import se.l4.otter.operations.string.StringHandler;

/**
 * Change for the current annotations.
 *
 * @author Andreas Holstenson
 *
 */
public class StringAnnotationChange
	implements Operation<StringHandler>
{
	private final AnnotationChange change;

	public StringAnnotationChange(AnnotationChange change)
	{
		this.change = change;
	}

	public AnnotationChange getChange()
	{
		return change;
	}

	@Override
	public void apply(StringHandler target)
	{
		target.annotationUpdate(change);
	}

	@Override
	public Operation<StringHandler> invert()
	{
		return new StringAnnotationChange(change.invert());
	}

	@Override
	public String toString()
	{
		return "@" + change;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((change == null) ? 0 : change.hashCode());
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
		StringAnnotationChange other = (StringAnnotationChange) obj;
		if(change == null)
		{
			if(other.change != null)
				return false;
		}
		else if(!change.equals(other.change))
			return false;
		return true;
	}
}
