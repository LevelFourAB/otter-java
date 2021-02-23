package se.l4.otter.operations.list;

import se.l4.exobytes.Serializer;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;
import se.l4.otter.operations.internal.list.ListOperationSerializer;
import se.l4.otter.operations.internal.list.ListTypeComposer;
import se.l4.otter.operations.internal.list.ListTypeTransformer;
import se.l4.otter.operations.string.StringType;

/**
 * Operation Transformation type for lists. Use {@link ListDelta} to construct
 * operations.
 *
 * <p>
 * The support for list is very similar to {@link StringType strings}. It
 * uses three operations, retain, insert and delete.
 *
 * @author Andreas Holstenson
 *
 */
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
		return ListOperationSerializer.INSTANCE;
	}

}
