package se.l4.otter.operations.map;

import java.util.ArrayList;
import java.util.List;

import se.l4.commons.serialization.Serializer;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.DefaultCompoundOperation;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;
import se.l4.otter.operations.internal.map.MapOperation;
import se.l4.otter.operations.internal.map.MapSet;
import se.l4.otter.operations.util.MutableOperationIterator;

/**
 * {@link OTType Operational Transformation type} for basic
 * operations against a map structure.
 * 
 * <p>
 * All operations in this type extend {@link MapOperation} which gives them a
 * key. The key is used to sort operations during composition and transformation
 * to make these operations possible for several operations.
 *  
 * @author Andreas Holstenson
 *
 */
public class MapType
	implements OTType<Operation<MapOperationHandler>>
{
	@Override
	public Operation<MapOperationHandler> compose(Operation<MapOperationHandler> left, Operation<MapOperationHandler> right)
	{
		MapKeyComparator comparator = MapKeyComparator.INSTANCE;
		MutableOperationIterator<MapOperationHandler> it1 = new MutableOperationIterator<>(CompoundOperation.toList(left, comparator));
		MutableOperationIterator<MapOperationHandler> it2 = new MutableOperationIterator<>(CompoundOperation.toList(right, comparator));
		
		List<Operation<MapOperationHandler>> result = new ArrayList<>();
		while(it1.hasNext())
		{
			MapOperation op1 = (MapOperation) it1.next();
			
			boolean handled = false;
			while(it2.hasNext())
			{
				MapOperation op2 = (MapOperation) it2.next();
			
				int compared = comparator.compare(op1, op2);
				
				if(compared > 0)
				{
					/*
					 * Left key is larger than right, so push the right key
					 * onto result as we can't combine it with anything.
					 */
					result.add(op2);
					continue;
				}
				else if(compared < 0)
				{
					/**
					 * Left key is smaller than right, release it back for
					 * handling and try next left operation.
					 */
					it2.back();
				}
				else
				{
					if(op1 instanceof MapSet && op2 instanceof MapSet)
					{
						MapSet s1 = (MapSet) op1;
						MapSet s2 = (MapSet) op2;
					
						// Take old value from left and new value from right
						it1.replace(new MapSet(s1.getKey(), s1.getOldValue(), s2.getNewValue()));
					}
					
					handled = true;
				}
				
				break;
			}
			
			if(! handled)
			{
				result.add(op1);
			}
		}
		
		while(it2.hasNext())
		{
			Operation<MapOperationHandler> op = it2.next();
			result.add(op);
		}
		
		result.sort(comparator);
		return new DefaultCompoundOperation<>(result);
	}

	@Override
	public OperationPair<Operation<MapOperationHandler>> transform(Operation<MapOperationHandler> left, Operation<MapOperationHandler> right)
	{
		MapKeyComparator comparator = MapKeyComparator.INSTANCE;
		MutableOperationIterator<MapOperationHandler> it1 = new MutableOperationIterator<>(CompoundOperation.toList(left, comparator));
		MutableOperationIterator<MapOperationHandler> it2 = new MutableOperationIterator<>(CompoundOperation.toList(right, comparator));

		List<Operation<MapOperationHandler>> deltaLeft = new ArrayList<>();
		List<Operation<MapOperationHandler>> deltaRight = new ArrayList<>();
		
		while(it1.hasNext())
		{
			MapOperation op1 = (MapOperation) it1.next();
			
			boolean handled = false;
			while(it2.hasNext())
			{
				MapOperation op2 = (MapOperation) it2.next();
			
				int compared = comparator.compare(op1, op2);
				if(compared > 0)
				{
					/**
					 * Left key is more than right, no transformation against
					 * left key to be done. Push right onto delta right. 
					 */
					deltaRight.add(op2);
					continue;
				}
				else if(compared < 0)
				{
					/*
					 * Left key is less than right, back up right by one and
					 * let left key be added to delta left. 
					 */
					it2.back();
				}
				else
				{
					if(op1 instanceof MapSet && op2 instanceof MapSet)
					{
						// Transform the two set operations against each other
						MapSet s1 = (MapSet) op1;
						MapSet s2 = (MapSet) op2;
						
						deltaRight.add(new MapSet(s2.getKey(), s1.getNewValue(), s2.getNewValue()));
					}
					
					handled = true;
				}
				
				break;
			}
			
			if(! handled)
			{
				deltaLeft.add(op1);
			}
		}
		
		while(it2.hasNext())
		{
			Operation<MapOperationHandler> op = it2.next();
			deltaRight.add(op);
		}
		
		return new OperationPair<Operation<MapOperationHandler>>(
			new DefaultCompoundOperation<>(deltaLeft),
			new DefaultCompoundOperation<>(deltaRight)
		);
	}

	@Override
	public Serializer<Operation<MapOperationHandler>> getSerializer()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
