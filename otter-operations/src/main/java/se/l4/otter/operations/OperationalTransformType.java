package se.l4.otter.operations;

import se.l4.commons.serialization.Serializer;

public interface OperationalTransformType<Op extends Operation<?>>
{
	/**
	 * Compose the given operations.
	 * 
	 * @param ops
	 * @return
	 */
	Op compose(Op op1, Op op2);
	
	/**
	 * Transform 
	 * 
	 * @param client
	 *   operation performed by client
	 * @param op2
	 *   operation performed by server
	 * @return
	 */
	OperationPair<Op> transform(Op client, Op server);
	
	/**
	 * Get a serializer that can be used to read and write operations handled
	 * by this type.
	 * 
	 * @return
	 */
	Serializer<Op> getSerializer();
}
