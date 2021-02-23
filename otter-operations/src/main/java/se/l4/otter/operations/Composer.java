package se.l4.otter.operations;

/**
 * Interface for easily composing operations together.
 *
 * @author Andreas Holstenson
 *
 */
public interface Composer<Op extends Operation<?>>
{
	/**
	 * Add an operation to be composed.
	 *
	 * @param op
	 * @return
	 */
	Composer<Op> add(Op op);

	/**
	 * Compose and return the composed operation.
	 *
	 * @return
	 */
	Op done();
}
