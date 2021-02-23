package se.l4.otter.operations;

/**
 * Representation of a operation that can be applied to a model. Operations
 * represent changes to the model, for example a stream of inserts and
 * deletions might be used to represent changes to a string.
 *
 * @author Andreas Holstenson
 *
 * @param <Handler>
 */
public interface Operation<Handler>
{
	/**
	 * Apply this operation to the given target.
	 *
	 * @param target
	 */
	void apply(Handler target);

	/**
	 * Invert this operation.
	 *
	 * @return
	 */
	Operation<Handler> invert();
}
