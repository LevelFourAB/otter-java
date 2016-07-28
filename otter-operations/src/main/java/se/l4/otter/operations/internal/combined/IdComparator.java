package se.l4.otter.operations.internal.combined;

import java.util.Comparator;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedTarget;

/**
 * Comparator that sorts {@link CombinedOperation} by their identifier.
 * 
 * @author Andreas Holstenson
 *
 */
public class IdComparator
	implements Comparator<Operation<CombinedTarget>>
{
	public static IdComparator INSTANCE = new IdComparator();
	
	private IdComparator()
	{
	}
	
	@Override
	public int compare(Operation<CombinedTarget> o1, Operation<CombinedTarget> o2)
	{
		return ((CombinedOperation) o1).getId().compareTo(((CombinedOperation) o2).getId());
	}
}
