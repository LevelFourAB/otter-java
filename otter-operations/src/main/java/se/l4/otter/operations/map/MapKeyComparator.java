package se.l4.otter.operations.map;

import java.util.Comparator;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.internal.map.MapOperation;

public class MapKeyComparator
	implements Comparator<Operation<MapOperationHandler>>
{
	public static MapKeyComparator INSTANCE = new MapKeyComparator();
	
	private MapKeyComparator()
	{
	}
	
	@Override
	public int compare(Operation<MapOperationHandler> o1, Operation<MapOperationHandler> o2)
	{
		return ((MapOperation) o1).getKey().compareTo(((MapOperation) o2).getKey());
	}
}
