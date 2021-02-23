package se.l4.otter.operations.list;

import java.util.Collection;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.internal.list.DefaultListDelta;

/**
 * Delta builder for {@link ListType}.
 *
 * @author Andreas Holstenson
 *
 */
public interface ListDelta<ReturnPath>
{
	/**
	 * Retain a number of items in the list.
	 *
	 * @param count
	 * @return
	 */
	ListDelta<ReturnPath> retain(int count);

	/**
	 * Insert a single item into the list at the current position.
	 *
	 * @param item
	 * @return
	 */
	ListDelta<ReturnPath> insert(Object item);

	/**
	 * Insert multiple items into the list at the current position.
	 *
	 * @param items
	 * @return
	 */
	ListDelta<ReturnPath> insertMultiple(Collection<? extends Object> items);

	/**
	 * Insert multiple items into the list at the current position
	 *
	 * @param items
	 * @return
	 */
	ListDelta<ReturnPath> insertMultiple(Object... items);

	/**
	 * Delete an item from the current position.
	 *
	 * @param current
	 *   the value to be removed
	 * @return
	 */
	ListDelta<ReturnPath> delete(Object current);

	/**
	 * Delete several items from the current position.
	 *
	 * @param current
	 *   the values that are to be removed
	 * @return
	 */
	ListDelta<ReturnPath> deleteMultiple(Collection<? extends Object> current);

	/**
	 * Delete several items from the current position.
	 *
	 * @param current
	 *   the values that are to be removed
	 * @return
	 */
	ListDelta<ReturnPath> deleteMultiple(Object... current);

	/**
	 * Advanced usage: Adopt a raw operation.
	 *
	 * @param op
	 * @return
	 */
	ListDelta<ReturnPath> adopt(Operation<ListHandler> op);

	/**
	 * Indicate that we are done building this delta.
	 *
	 * @return
	 */
	ReturnPath done();

	/**
	 * Get {@link ListDelta} that builds a {@link Operation}.
	 *
	 * @return
	 */
	static ListDelta<Operation<ListHandler>> builder()
	{
		return new DefaultListDelta<>(o -> o);
	}

}
