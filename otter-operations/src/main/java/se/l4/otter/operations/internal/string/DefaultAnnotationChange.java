package se.l4.otter.operations.internal.string;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import se.l4.otter.operations.ValueChange;
import se.l4.otter.operations.string.AnnotationChange;

public class DefaultAnnotationChange
	implements AnnotationChange
{
	private final Map<String, ValueChange> changes;

	public DefaultAnnotationChange(Map<String, ValueChange> changes)
	{
		this.changes = changes;
	}

	@Override
	public boolean isEmpty()
	{
		return changes.isEmpty();
	}

	@Override
	public boolean containsKey(String key)
	{
		return changes.containsKey(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key)
	{
		ValueChange change = getChange(key);
		return change == null ? null : (T) change.getNewValue();
	}

	@Override
	public ValueChange getChange(String key)
	{
		return changes.get(key);
	}

	@Override
	public boolean isRemoval(String key)
	{
		ValueChange change = getChange(key);
		return change == null ? false : change.getNewValue() == null;
	}

	@Override
	public Set<String> keys()
	{
		return changes.keySet();
	}

	@Override
	public AnnotationChange invert()
	{
		Map<String, ValueChange> result = new HashMap<>();

		for(Map.Entry<String, ValueChange> e : changes.entrySet())
		{
			ValueChange change = e.getValue();
			result.put(e.getKey(), new ValueChange(change.getNewValue(), change.getOldValue()));
		}

		return new DefaultAnnotationChange(result);
	}

	public static AnnotationChange merge(AnnotationChange first, AnnotationChange second)
	{
		if(first == null) return second;
		if(second == null) return first;

		Map<String, ValueChange> changes = new HashMap<>();
		for(String key : first.keys())
		{
			changes.put(key, first.getChange(key));
		}

		for(String key : second.keys())
		{
			ValueChange change = second.getChange(key);
			ValueChange current = changes.get(key);
			if(current != null)
			{
				if(Objects.equals(current.getNewValue(), change.getOldValue()))
				{
					/*
					 * Merge them if the last change has the same new value that
					 * this change says is its old value.
					 */
					change = new ValueChange(current.getOldValue(), change.getNewValue());
				}
				else if(change.getNewValue() == null && current.getNewValue() != null)
				{
					/*
					 * The change indicates a removal, but does not seem to
					 * be a continuation of the previous one. Let the
					 * current value win.
					 */
					continue;
				}
			}

			if(! Objects.equals(change.getOldValue(), change.getNewValue()))
			{
				changes.put(key, change);
			}
			else
			{
				changes.remove(key);
			}
		}

		return new DefaultAnnotationChange(changes);
	}

	@Override
	public String toString()
	{
		return changes.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((changes == null) ? 0 : changes.hashCode());
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
		DefaultAnnotationChange other = (DefaultAnnotationChange) obj;
		if(changes == null)
		{
			if(other.changes != null)
				return false;
		}
		else if(!changes.equals(other.changes))
			return false;
		return true;
	}


}
