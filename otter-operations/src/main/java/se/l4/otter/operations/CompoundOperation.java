package se.l4.otter.operations;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;

/**
 * {@link Operation} that is composed of several other {@link Operation}s.
 *
 * @author Andreas Holstenson
 *
 * @param <Handler>
 */
public interface CompoundOperation<Handler>
	extends Operation<Handler>
{
	/**
	 * Get all of the operations.
	 *
	 * @return
	 */
	ListIterable<Operation<Handler>> getOperations();

	/**
	 * Create a compound operation over the give iterable.
	 * 
	 * @param ops
	 * @return
	 */
	static <H> CompoundOperation<H> create(Iterable<Operation<H>> ops)
	{
		return new DefaultCompoundOperation<>(
			Lists.immutable.ofAll(ops)
		);
	}

	/**
	 * Turn a operation into a list of operations.
	 *
	 * @param op
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T> ListIterable<Operation<T>> toList(Operation<T> op)
	{
		if(op instanceof CompoundOperation)
		{
			return ((CompoundOperation) op).getOperations();
		}
		else if(op == null)
		{
			return Lists.immutable.empty();
		}

		return Lists.immutable.of(op);
	}

	static <T> Operation<T> empty()
	{
		return new DefaultCompoundOperation<>(Lists.immutable.empty());
	}
}
