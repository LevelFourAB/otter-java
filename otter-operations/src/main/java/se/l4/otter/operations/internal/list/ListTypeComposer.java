package se.l4.otter.operations.internal.list;

import java.util.Arrays;

import org.eclipse.collections.api.list.ListIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.otter.operations.ComposeException;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.internal.string.StringTypeComposer;
import se.l4.otter.operations.list.ListDelta;
import se.l4.otter.operations.list.ListHandler;
import se.l4.otter.operations.list.ListType;
import se.l4.otter.operations.string.StringType;
import se.l4.otter.operations.util.MutableOperationIterator;

/**
 * Composer for {@link ListType}. This is very similar to how strings are
 * handled by {@link StringType} and {@link StringTypeComposer}.
 *
 * @author Andreas Holstenson
 *
 */
public class ListTypeComposer
{
	private static final Logger log = LoggerFactory.getLogger(ListTypeComposer.class);

	private final MutableOperationIterator<ListHandler> left;
	private final MutableOperationIterator<ListHandler> right;

	private final ListDelta<Operation<ListHandler>> delta;

	public ListTypeComposer(
		ListIterable<Operation<ListHandler>> first,
		ListIterable<Operation<ListHandler>> second
	)
	{
		log.debug("Composing {} with {}", first, second);

		delta = ListDelta.builder();

		left = new MutableOperationIterator<>(first);
		right = new MutableOperationIterator<>(second);
	}

	public Operation<ListHandler> perform()
	{
		while(left.hasNext() && right.hasNext())
		{
			Operation<ListHandler> op1 = left.next();
			Operation<ListHandler> op2 = right.next();

			log.trace("  Compose {} with {}", op1, op2);

			if(op1 instanceof ListRetain)
			{
				handleRetain(op1, op2);
			}
			else if(op1 instanceof ListInsert)
			{
				handleInsert(op1, op2);
			}
			else if(op1 instanceof ListDelete)
			{
				handleDelete(op1, op2);
			}
		}

		while(left.hasNext())
		{
			Operation<ListHandler> op1 = left.next();
			if(op1 instanceof ListDelete)
			{
				delta.adopt(op1);
			}
			else
			{
				throw new ComposeException("Operation size mismatch");
			}
		}

		// Apply all of the remaining operations
		while(right.hasNext())
		{
			Operation<ListHandler> op = right.next();
			delta.adopt(op);
		}

		return delta.done();
	}

	private void handleRetain(Operation<ListHandler> op1,
			Operation<ListHandler> op2)
	{
		int length1 = ((ListRetain) op1).getLength();

		if(op2 instanceof ListRetain)
		{
			// Both operations are retains
			int length2 = ((ListRetain) op2).getLength();

			if(length1 < length2)
			{
				// Left operation is shorter, retain left count and rewrite right
				delta.retain(length1);

				right.replace(new ListRetain(length2 - length1));
			}
			else if(length1 > length2)
			{
				// Right operation is shorter, retain right and rewrite left
				delta.retain(length2);

				left.replace(new ListRetain(length1 - length2));
			}
			else
			{
				// Matching lengths, no need to do anything special
				delta.retain(length1);
			}
		}
		else if(op2 instanceof ListInsert)
		{
			/*
			 * Right operation is an insert, simply insert and then handle
			 * left retain again.
			 */
			delta.adopt(op2);
			left.back();
		}
		else if(op2 instanceof ListDelete)
		{
			// Second operation is a delete, add it to the delta and replace ourselves
			Object[] items2 = ((ListDelete) op2).getItems();
			int length2 = items2.length;

			if(length1 < length2)
			{
				delta.deleteMultiple(Arrays.copyOfRange(items2, 0, length1));
				right.replace(new ListDelete(Arrays.copyOfRange(items2, length1, items2.length)));
			}
			else if(length1 > length2)
			{
				// Only replace if we deleted less than we retain
				delta.deleteMultiple(items2);
				left.replace(new ListRetain(length1 - length2));
			}
			else
			{
				delta.deleteMultiple(items2);
			}
		}
	}

	private void handleInsert(Operation<ListHandler> op1,
			Operation<ListHandler> op2)
	{
		Object[] items1 = ((ListInsert) op1).getItems();
		int length1 = items1.length;

		if(op2 instanceof ListRetain)
		{
			int length2 = ((ListRetain) op2).getLength();
			if(length1 < length2)
			{
				/*
				 * Left can fit into the right retain, insert all of and
				 * replace with left over retain count.
				 */
				delta.adopt(op1);
				right.replace(new ListRetain(length2 - length1));
			}
			else if(length1 > length2)
			{
				/**
				 * Left is longer than right. Insert substring of left and
				 * replace with rest of value.
				 */
				delta.insertMultiple(Arrays.copyOfRange(items1, 0, length2));
				left.replace(new ListInsert(Arrays.copyOfRange(items1, length2, items1.length)));
			}
			else
			{
				// Both left and right have the same length
				delta.adopt(op1);
			}
		}
		else if(op2 instanceof ListInsert)
		{
			/*
			 * Two inserts, right is inserted first and then we handle left
			 * again.
			 */
			delta.adopt(op2);
			left.back();
		}
		else if(op2 instanceof ListDelete)
		{
			Object[] items2 = ((ListDelete) op2).getItems();
			int length2 = items2.length;

			if(length1 > length2)
			{
				/*
				 * Left insert is longer than right delete, replace left with
				 * remaining items from left.
				 */
				left.replace(new ListInsert(Arrays.copyOfRange(items1, length2, items1.length)));
			}
			else if(length1 < length2)
			{
				/**
				 * Left insert is shorter than right delete, replace right with
				 * remaining items from right.
				 */
				right.replace(new ListDelete(Arrays.copyOfRange(items2, length1, items2.length)));
			}
			else
			{
				// Exact same length, do nothing as they cancel each other
			}
		}
	}

	private void handleDelete(Operation<ListHandler> op1, Operation<ListHandler> op2)
	{
		if(op2 instanceof ListRetain)
		{
			/*
			 * Right operation is a retain, delete left and back up one to
			 * handle right retain again.
			 */
			delta.adopt(op1);
			right.back();
		}
		else if(op2 instanceof ListInsert)
		{
			/**
			 * Right operation is an insert, push left delete followed by
			 * right insert.
			 */
			delta.adopt(op1);
			delta.adopt(op2);
		}
		else if(op2 instanceof ListDelete)
		{
			/**
			 * Right operation is also a delete. Push left delete and back
			 * up so right is handled again.
			 */
			delta.adopt(op1);
			right.back();
		}
	}
}
