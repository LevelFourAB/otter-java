package se.l4.otter.operations.string;

import java.util.List;

import se.l4.commons.serialization.Serializer;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationalTransformType;
import se.l4.otter.operations.OperationPair;
import se.l4.otter.operations.internal.string.StringOperationSerializer;
import se.l4.otter.operations.internal.string.StringTypeComposer;
import se.l4.otter.operations.internal.string.StringTypeTransformer;

public class StringType
	implements OperationalTransformType<Operation<StringOperationHandler>>
{
	private final Serializer<Operation<StringOperationHandler>> serializer;
	
	public StringType()
	{
		serializer = new StringOperationSerializer();
	}

	@Override
	public Operation<StringOperationHandler> compose(Operation<StringOperationHandler> op1, Operation<StringOperationHandler> op2)
	{
		return compose(
			CompoundOperation.toList(op1),
			CompoundOperation.toList(op2)
		);
	}
	
	private Operation<StringOperationHandler> compose(List<Operation<StringOperationHandler>> ops1, List<Operation<StringOperationHandler>> ops2)
	{
		return new StringTypeComposer(ops1, ops2).perform();
	}

	@Override
	public OperationPair<Operation<StringOperationHandler>> transform(Operation<StringOperationHandler> left, Operation<StringOperationHandler> right)
	{
		return new StringTypeTransformer(left, right).perform();
	}
	
	@Override
	public Serializer<Operation<StringOperationHandler>> getSerializer()
	{
		return serializer;
	}
}
