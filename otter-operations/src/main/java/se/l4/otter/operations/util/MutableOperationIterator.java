package se.l4.otter.operations.util;

import java.util.Iterator;
import java.util.ListIterator;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;

import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.Operation;

/**
 * {@link Iterator} for a list of {@link Operation}s which supports basic
 * mutation of its iteration order.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class MutableOperationIterator<T>
	implements Iterator<Operation<T>>
{
	private final ListIterator<Operation<T>> it;

	public MutableOperationIterator(Operation<T> op)
	{
		this(CompoundOperation.toList(op));
	}

	public MutableOperationIterator(ListIterable<Operation<T>> ops)
	{
		this.it = Lists.mutable.ofAll(ops).listIterator();
	}

	@Override
	public boolean hasNext()
	{
		return it.hasNext();
	}

	@Override
	public Operation<T> next()
	{
		return it.next();
	}

	/**
	 * Go back one step so the current operation is handled again.
	 *
	 */
	public void back()
	{
		it.previous();
	}

	/**
	 * Replace the current operation.
	 *
	 * @param op
	 */
	public void replace(Operation<T> op)
	{
		it.set(op);
		it.previous();
	}
}
