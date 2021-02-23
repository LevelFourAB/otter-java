package se.l4.otter.operations.string;

import org.eclipse.collections.api.list.ListIterable;

import se.l4.exobytes.Serializer;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;
import se.l4.otter.operations.internal.string.StringOperationSerializer;
import se.l4.otter.operations.internal.string.StringTypeComposer;
import se.l4.otter.operations.internal.string.StringTypeTransformer;

public class StringType
	implements OTType<Operation<StringHandler>>
{
	private final Serializer<Operation<StringHandler>> serializer;

	public StringType()
	{
		serializer = new StringOperationSerializer();
	}

	@Override
	public Operation<StringHandler> compose(Operation<StringHandler> op1, Operation<StringHandler> op2)
	{
		return compose(
			CompoundOperation.toList(op1),
			CompoundOperation.toList(op2)
		);
	}

	private Operation<StringHandler> compose(
		ListIterable<Operation<StringHandler>> ops1,
		ListIterable<Operation<StringHandler>> ops2)
	{
		return new StringTypeComposer(ops1, ops2).perform();
	}

	@Override
	public OperationPair<Operation<StringHandler>> transform(Operation<StringHandler> left, Operation<StringHandler> right)
	{
		return new StringTypeTransformer(left, right).perform();
	}

	@Override
	public Serializer<Operation<StringHandler>> getSerializer()
	{
		return serializer;
	}
}
