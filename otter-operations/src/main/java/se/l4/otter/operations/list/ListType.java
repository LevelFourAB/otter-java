package se.l4.otter.operations.list;

import se.l4.commons.serialization.Serializer;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;
import se.l4.otter.operations.internal.list.ListTypeComposer;
import se.l4.otter.operations.internal.list.ListTypeTransformer;

public class ListType
	implements OTType<Operation<ListHandler>>
{
	@Override
	public Operation<ListHandler> compose(Operation<ListHandler> left, Operation<ListHandler> right)
	{
		return new ListTypeComposer(
			CompoundOperation.toList(left),
			CompoundOperation.toList(right)
		).perform();
	}
	
	@Override
	public OperationPair<Operation<ListHandler>> transform(Operation<ListHandler> left, Operation<ListHandler> right)
	{
		return new ListTypeTransformer(
			left,
			right
		).perform();
	}
	
	@Override
	public Serializer<Operation<ListHandler>> getSerializer()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
