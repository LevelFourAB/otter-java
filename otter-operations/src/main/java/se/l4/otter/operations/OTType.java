package se.l4.otter.operations;

import se.l4.exobytes.Serializer;
import se.l4.otter.operations.internal.DefaultComposer;

public interface OTType<Op extends Operation<?>>
{
	/**
	 * Compose the given operations.
	 *
	 * @param left
	 *   left operation
	 * @param right
	 *   operation to compose over left
	 * @return
	 */
	Op compose(Op left, Op right);

	/**
	 * Transform one operation over another.
	 *
	 * @param left
	 *   left operation
	 * @param right
	 *   right operation
	 * @return
	 */
	OperationPair<Op> transform(Op left, Op right);

	/**
	 * Get a serializer that can be used to read and write operations handled
	 * by this type.
	 *
	 * @return
	 */
	Serializer<Op> getSerializer();

	default Composer<Op> newComposer()
	{
		return new DefaultComposer<>(this);
	}
}
