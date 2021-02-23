package se.l4.otter.operations.map;

import java.util.Comparator;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.internal.map.MapOperation;

public class MapKeyComparator
	implements Comparator<Operation<MapHandler>>
{
	public static MapKeyComparator INSTANCE = new MapKeyComparator();

	private MapKeyComparator()
	{
	}

	@Override
	public int compare(Operation<MapHandler> o1, Operation<MapHandler> o2)
	{
		return ((MapOperation) o1).getKey().compareTo(((MapOperation) o2).getKey());
	}
}
