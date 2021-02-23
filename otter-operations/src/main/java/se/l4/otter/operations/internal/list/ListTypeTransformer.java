package se.l4.otter.operations.internal.list;

import java.util.Arrays;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;
import se.l4.otter.operations.TransformException;
import se.l4.otter.operations.list.ListDelta;
import se.l4.otter.operations.list.ListHandler;
import se.l4.otter.operations.list.ListType;
import se.l4.otter.operations.util.MutableOperationIterator;

/**
 * Transformer used by {@link ListType}.
 *
 * @author Andreas Holstenson
 *
 */
public class ListTypeTransformer
{
	private final MutableOperationIterator<ListHandler> left;
	private final MutableOperationIterator<ListHandler> right;

	private final ListDelta<Operation<ListHandler>> deltaLeft;
	private final ListDelta<Operation<ListHandler>> deltaRight;

	public ListTypeTransformer(Operation<ListHandler> left, Operation<ListHandler> right)
	{
		this.left = new MutableOperationIterator<>(left);
		this.right = new MutableOperationIterator<>(right);

		deltaLeft = ListDelta.builder();
		deltaRight = ListDelta.builder();
	}

	public OperationPair<Operation<ListHandler>> perform()
	{
		while(left.hasNext())
		{
			Operation<ListHandler> op1 = left.next();

			if(right.hasNext())
			{
				Operation<ListHandler> op2 = right.next();

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
			else
			{
				/*
				 * Operations are still available, but no matching operations
				 * in right.
				 */
				if(op1 instanceof ListInsert)
				{
					Object[] items1 = ((ListInsert) op1).getItems();
					deltaLeft.adopt(op1);
					deltaRight.retain(items1.length);
				}
				else
				{
					throw new TransformException("Could not transform, mismatch in operation. Current left operation is: " + op1);
				}
			}
		}

		while(right.hasNext())
		{
			Operation<ListHandler> op2 = right.next();
			if(op2 instanceof ListInsert)
			{
				Object[] items2 = ((ListInsert) op2).getItems();
				deltaRight.adopt(op2);
				deltaLeft.retain(items2.length);
			}
			else
			{
				throw new TransformException("Could not transform, mismatch in operation. Current right operation is: " + op2);
			}
		}

		return new OperationPair<>(deltaLeft.done(), deltaRight.done());
	}

	private void handleRetain(Operation<ListHandler> op1, Operation<ListHandler> op2)
	{
		int length1 = ((ListRetain) op1).getLength();
		if(op2 instanceof ListRetain)
		{
			int length2 = ((ListRetain) op2).getLength();

			if(length1 > length2)
			{
				/*
				 * Left is longer than right, retain length of right and
				 * replace left with remaining.
				 */
				deltaLeft.retain(length2);
				deltaRight.retain(length2);

				left.replace(new ListRetain(length1 - length2));
			}
			else if(length1 < length2)
			{
				/**
				 * Left is shorter than right, retain length of left and
				 * replace right with remaining.
				 */
				deltaLeft.retain(length1);
				deltaRight.retain(length1);

				right.replace(new ListRetain(length2 - length1));
			}
			else
			{
				// Same length, retain both
				deltaLeft.retain(length1);
				deltaRight.retain(length2);
			}
		}
		else if(op2 instanceof ListInsert)
		{
			/*
			 * Right is an insertion, just insert into right with a matching
			 * retain into left delta and ask left to be handled again.
			 */
			Object[] items2 = ((ListInsert) op2).getItems();
			int length2 = items2.length;

			deltaLeft.retain(length2);
			deltaRight.adopt(op2);

			left.back();
		}
		else if(op2 instanceof ListDelete)
		{
			Object[] items2 = ((ListDelete) op2).getItems();
			int length2 = items2.length;

			if(length1 > length2)
			{
				/*
				 * Left is longer than right, let right delete and replace
				 * left with retain of remaining.
				 */
				deltaRight.adopt(op2);

				left.replace(new ListRetain(length1 - length2));
			}
			else if(length1 < length2)
			{
				/**
				 * Left is shorter than right, let right delete and replace
				 * right with remaining.
				 */
				deltaRight.deleteMultiple(Arrays.copyOf(items2, length1));

				right.replace(new ListDelete(Arrays.copyOfRange(items2, length1, items2.length)));
			}
			else
			{
				// Same length, simply delete
				deltaRight.adopt(op2);
			}
		}
	}

	private void handleInsert(Operation<ListHandler> op1, Operation<ListHandler> op2)
	{
		Object[] items1 = ((ListInsert) op1).getItems();
		int length1 = items1.length;

		deltaLeft.adopt(op1);
		deltaRight.retain(length1);

		right.back();
	}

	private void handleDelete(Operation<ListHandler> op1, Operation<ListHandler> op2)
	{
		Object[] items1 = ((ListDelete) op1).getItems();
		int length1 = items1.length;

		if(op2 instanceof ListRetain)
		{
			int length2 = ((ListRetain) op2).getLength();
			if(length1 > length2)
			{
				/**
				 * Left is longer than right, delete up to length of right
				 * and replace left with delete for remaining.
				 */
				deltaLeft.deleteMultiple(Arrays.copyOf(items1, length2));

				left.replace(new ListDelete(Arrays.copyOfRange(items1, length2, items1.length)));
			}
			else if(length1 < length2)
			{
				/**
				 * Left is shorter than right, delete all and replace right
				 * with retain of remaining.
				 */
				deltaLeft.adopt(op1);

				right.replace(new ListRetain(length2 - length1));
			}
			else
			{
				// Same length, just delete
				deltaLeft.adopt(op1);
			}
		}
		else if(op2 instanceof ListInsert)
		{
			/*
			 * Right is an insertion, just insert into right with a matching
			 * retain into left delta and ask left to be handled again.
			 */
			Object[] items2 = ((ListInsert) op2).getItems();
			int length2 = items2.length;

			deltaLeft.retain(length2);
			deltaRight.adopt(op2);

			left.back();
		}
		else if(op2 instanceof ListDelete)
		{
			Object[] items2 = ((ListDelete) op2).getItems();
			int length2 = items2.length;

			if(length1 > length2)
			{
				/*
				 * Left is longer than right, replace left with a delete of
				 * remaining text.
				 */
				left.replace(new ListDelete(Arrays.copyOfRange(items1, length2, items1.length)));
			}
			else if(length1 < length2)
			{
				/**
				 * Left is shorter than right, replace right with a delete of
				 * remaining text.
				 */
				right.replace(new ListDelete(Arrays.copyOfRange(items2, length1, items2.length)));
			}
			else
			{
				// Do nothing
			}
		}
	}
}
