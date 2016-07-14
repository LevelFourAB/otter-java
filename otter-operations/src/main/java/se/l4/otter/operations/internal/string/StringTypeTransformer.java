package se.l4.otter.operations.internal.string;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringOperationHandler;
import se.l4.otter.operations.string.StringType;
import se.l4.otter.operations.util.MutableOperationIterator;

/**
 * Transformer used by {@link StringType}.
 * 
 * @author Andreas Holstenson
 *
 */
public class StringTypeTransformer
{
	private final MutableOperationIterator<StringOperationHandler> left;
	private final MutableOperationIterator<StringOperationHandler> right;
	
	private final StringDelta<Operation<StringOperationHandler>> deltaLeft;
	private final StringDelta<Operation<StringOperationHandler>> deltaRight;

	public StringTypeTransformer(Operation<StringOperationHandler> left, Operation<StringOperationHandler> right)
	{
		this.left = new MutableOperationIterator<>(left);
		this.right = new MutableOperationIterator<>(right);
		
		deltaLeft = StringDelta.builder();
		deltaRight = StringDelta.builder();
	}
	
	public OperationPair<Operation<StringOperationHandler>> perform()
	{
		while(left.hasNext() && right.hasNext())
		{
			Operation<StringOperationHandler> op1 = left.next();
			Operation<StringOperationHandler> op2 = right.next();
			
			if(op1 instanceof StringRetain)
			{
				handleRetain(op1, op2);
			}
			else if(op1 instanceof StringInsert)
			{
				handleInsert(op1, op2);
			}
			else if(op1 instanceof StringDelete)
			{
				handleDelete(op1, op2);
			}
		}
		
		
		while(left.hasNext())
		{
			Operation<StringOperationHandler> op = left.next();
			op.apply(deltaLeft.asHandler());
		}
		
		while(right.hasNext())
		{
			Operation<StringOperationHandler> op = right.next();
			op.apply(deltaRight.asHandler());
		}
		
		return new OperationPair<>(deltaLeft.done(), deltaRight.done());
	}

	private void handleRetain(Operation<StringOperationHandler> op1, Operation<StringOperationHandler> op2)
	{
		int length1 = ((StringRetain) op1).getLength();
		if(op2 instanceof StringRetain)
		{
			int length2 = ((StringRetain) op2).getLength();
			
			if(length1 > length2)
			{
				/*
				 * Left is longer than right, retain length of right and
				 * replace left with remaining.
				 */
				deltaLeft.retain(length2);
				deltaRight.retain(length2);
				
				left.replace(new StringRetain(length1 - length2));
			}
			else if(length1 < length2)
			{
				/**
				 * Left is shorter than right, retain length of left and
				 * replace right with remaining.
				 */
				deltaLeft.retain(length1);
				deltaRight.retain(length1);
				
				right.replace(new StringRetain(length2 -length1));
			}
			else
			{
				// Same length, retain both
				deltaLeft.retain(length1);
				deltaRight.retain(length2);
			}
		}
		else if(op2 instanceof StringInsert)
		{
			/*
			 * Right is an insert, left retains combined length and right
			 * performs the insert.
			 */
			String value2 = ((StringInsert) op2).getValue();
			int length2 = value2.length();
			
			deltaLeft.retain(length1 + length2);
			deltaRight.insert(value2);
		}
		else if(op2 instanceof StringDelete)
		{
			String value2 = ((StringDelete) op2).getValue();
			int length2 = value2.length();
			
			if(length1 > length2)
			{
				/*
				 * Left is longer than right, let right delete and replace
				 * left with retain of remaining.
				 */
				deltaRight.delete(value2);
				
				left.replace(new StringRetain(length1 - length2));
			}
			else if(length1 < length2)
			{
				/**
				 * Left is shorter than right, let right delete and replace
				 * right with remaining.
				 */
				deltaRight.delete(value2.substring(0, length1));
				
				right.replace(new StringDelete(value2.substring(length1)));
			}
			else
			{
				// Same length, simply delete
				deltaRight.delete(value2);
			}
		}
	}
	
	private void handleInsert(Operation<StringOperationHandler> op1, Operation<StringOperationHandler> op2)
	{
		String value1 = ((StringInsert) op1).getValue();
		int length1 = value1.length();
		
		if(op2 instanceof StringRetain)
		{
			/*
			 * Right is retain, let left be insert and right be a retain
			 * of combined length.
			 */
			int length2 = ((StringRetain) op2).getLength();
			
			deltaLeft.insert(value1);
			deltaRight.retain(length1 + length2);
		}
		else if(op2 instanceof StringInsert)
		{
			/**
			 * Both are inserts, mirror each other with retains.
			 */
			String value2 = ((StringInsert) op2).getValue();
			int length2 = value2.length();
			
			deltaLeft.insert(value1);
			deltaLeft.retain(length2);
			
			deltaRight.retain(length1);
			deltaRight.insert(value2);
		}
		else if(op2 instanceof StringDelete)
		{
			/**
			 * Right is a delete, let left be an insert and right a retain
			 * plus a delete.
			 */
			String value2 = ((StringDelete) op2).getValue();
			
			deltaLeft.insert(value1);
			deltaRight.retain(length1)
				.delete(value2);
		}
	}

	private void handleDelete(Operation<StringOperationHandler> op1, Operation<StringOperationHandler> op2)
	{
		String value1 = ((StringDelete) op1).getValue();
		int length1 = value1.length();
		
		if(op2 instanceof StringRetain)
		{
			int length2 = ((StringRetain) op2).getLength();
			if(length1 > length2)
			{
				/**
				 * Left is longer than right, delete up to length of right
				 * and replace left with delete for remaining.
				 */
				deltaLeft.delete(value1.substring(0, length2));
				
				left.replace(new StringDelete(value1.substring(length2)));
			}
			else if(length1 < length2)
			{
				/**
				 * Left is shorter than right, delete all and replace right
				 * with retain of remaining.
				 */
				deltaLeft.delete(value1);
				
				right.replace(new StringRetain(length2 - length1));
			}
			else
			{
				// Same length, just delete
				deltaLeft.delete(value1);
			}
		}
		else if(op2 instanceof StringInsert)
		{
			/*
			 * Right is an insertion, just insert into right and insert
			 * retain and delete in left.
			 */
			String value2 = ((StringInsert) op2).getValue();
			int length2 = value2.length();
			
			deltaRight.insert(value2);
			deltaLeft
				.retain(length2)
				.delete(value1);
		}
		else if(op2 instanceof StringDelete)
		{
			String value2 = ((StringDelete) op2).getValue();
			int length2 = value2.length();
			
			if(length1 > length2)
			{
				/*
				 * Left is longer than right, replace left with a delete of
				 * remaining text.
				 */
				left.replace(new StringDelete(value1.substring(length2)));
			}
			else if(length1 < length2)
			{
				/**
				 * Left is shorter than right, replace right with a delete of
				 * remaining text.
				 */
				right.replace(new StringDelete(value2.substring(length1)));
			}
			else
			{
				// Do nothing
			}
		}
	}
}
