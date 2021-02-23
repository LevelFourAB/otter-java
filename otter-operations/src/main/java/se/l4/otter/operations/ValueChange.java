package se.l4.otter.operations;

/**
 * Change to a key, keeping track of the old and new value.
 *
 * @author Andreas Holstenson
 *
 */
public class ValueChange
{
	private final Object oldValue;
	private final Object newValue;

	public ValueChange(Object oldValue, Object newValue)
	{
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Object getOldValue()
	{
		return oldValue;
	}

	public Object getNewValue()
	{
		return newValue;
	}

	@Override
	public String toString()
	{
		return oldValue + " -> " + newValue;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
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
		ValueChange other = (ValueChange) obj;
		if(newValue == null)
		{
			if(other.newValue != null)
				return false;
		}
		else if(!newValue.equals(other.newValue))
			return false;
		if(oldValue == null)
		{
			if(other.oldValue != null)
				return false;
		}
		else if(!oldValue.equals(other.oldValue))
			return false;
		return true;
	}


}
