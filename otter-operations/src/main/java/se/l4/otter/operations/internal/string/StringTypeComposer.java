package se.l4.otter.operations.internal.string;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.otter.operations.ComposeException;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringOperationHandler;
import se.l4.otter.operations.string.StringType;
import se.l4.otter.operations.util.MutableOperationIterator;

/**
 * Composer for {@link StringType}.
 * @author Andreas Holstenson
 *
 */
public class StringTypeComposer
{
	private static final Logger log = LoggerFactory.getLogger(StringTypeComposer.class);
	
	private final MutableOperationIterator<StringOperationHandler> left;
	private final MutableOperationIterator<StringOperationHandler> right;

	private final StringDelta<Operation<StringOperationHandler>> delta;
	
	public StringTypeComposer(List<Operation<StringOperationHandler>> first, List<Operation<StringOperationHandler> >second)
	{
		log.debug("Composing {} with {}", first, second);
		
		delta = StringDelta.builder();
		
		left = new MutableOperationIterator<>(first);
		right = new MutableOperationIterator<>(second);
	}
	
	public Operation<StringOperationHandler> perform()
	{
		while(left.hasNext() && right.hasNext())
		{
			Operation<StringOperationHandler> op1 = left.next();
			Operation<StringOperationHandler> op2 = right.next();
			
			log.trace("  Compose {} with {}", op1, op2);
			
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
		
		if(left.hasNext())
		{
			throw new ComposeException("Operation size mismatch");
		}
		
		// Apply all of the remaining operations
		while(right.hasNext())
		{
			Operation<StringOperationHandler> op = right.next();
			op.apply(delta.asHandler());
		}
		
		return delta.done();
	}

	private void handleRetain(Operation<StringOperationHandler> op1,
			Operation<StringOperationHandler> op2)
	{
		int length1 = ((StringRetain) op1).getLength();
		
		if(op2 instanceof StringRetain)
		{
			// Both operations are retains
			int length2 = ((StringRetain) op2).getLength();
			
			if(length1 < length2)
			{
				// Left operation is shorter, retain left count and rewrite right
				delta.retain(length1);
				
				right.replace(new StringRetain(length2 - length1));
			}
			else if(length1 > length2)
			{
				// Right operation is shorter, retain right and rewrite left
				delta.retain(length2);
				
				left.replace(new StringRetain(length1 - length2));
			}
			else
			{
				// Matching lengths, no need to do anything special
				delta.retain(length1);
			}
		}
		else if(op2 instanceof StringInsert)
		{
			/*
			 * Right operation is an insert, simply insert and then handle
			 * left retain again.
			 */
			String value = ((StringInsert) op2).getValue();
			delta.insert(value);
			left.back();
		}
		else if(op2 instanceof StringDelete)
		{
			// Second operation is a delete, add it to the delta and replace ourselves
			String value = ((StringDelete) op2).getValue();
			delta.delete(value);
			
			int length2 = value.length();
			if(length2 < length1)
			{
				// Only replace if we deleted less than we retain
				left.replace(new StringRetain(length1 - length2));
			}
		}
	}

	private void handleInsert(Operation<StringOperationHandler> op1,
			Operation<StringOperationHandler> op2)
	{
		String value1 = ((StringInsert) op1).getValue();
		int length1 = value1.length();
		
		if(op2 instanceof StringRetain)
		{
			int length2 = ((StringRetain) op2).getLength();
			if(length1 < length2)
			{
				/*
				 * Left can fit into the right retain, insert all of and
				 * replace with left over retain count.
				 */
				delta.insert(value1);
				right.replace(new StringRetain(length2 - length1));
			}
			else if(length1 > length2)
			{
				/**
				 * Left is longer than right. Insert substring of left and
				 * replace with rest of value.
				 */
				delta.insert(value1.substring(0, length2));
				left.replace(new StringInsert(value1.substring(length2)));
			}
			else
			{
				// Both left and right have the same length
				delta.insert(value1);
			}
		}
		else if(op2 instanceof StringInsert)
		{
			/*
			 * Two inserts, right is inserted first and then we handle left
			 * again.
			 */
			String value2 = ((StringInsert) op2).getValue();
			delta.insert(value2);
			left.back();
		}
		else if(op2 instanceof StringDelete)
		{
			String value2 = ((StringDelete) op2).getValue();
			int length2 = value2.length();
			
			if(length1 > length2)
			{
				/*
				 * Left insert is longer than right delete, replace left with
				 * remaining text from left.
				 */
				left.replace(new StringInsert(value1.substring(length2)));
			}
			else if(length1 < length2)
			{
				/**
				 * Left insert is shorter than right delete, replace right with
				 * remaining text from right.
				 */
				right.replace(new StringDelete(value2.substring(length1)));
			}
			else
			{
				// Exact same length, do nothing as they cancel each other
			}
		}
	}
	
	private void handleDelete(Operation<StringOperationHandler> op1, Operation<StringOperationHandler> op2)
	{
		String value1 = ((StringDelete) op1).getValue();
		int length1 = value1.length();
		
		if(op2 instanceof StringRetain)
		{
			/*
			 * Right operation is a retain, delete left and back up one to
			 * handle right retain again.
			 */
			delta.delete(value1);
			right.back();
		}
		else if(op2 instanceof StringInsert)
		{
			/**
			 * Right operation is an insert, push left delete followed by
			 * right insert.
			 */
			delta.delete(value1);
			delta.insert(((StringInsert) op2).getValue());
		}
		else if(op2 instanceof StringDelete)
		{
			/**
			 * Right operation is also a delete. Push left delete and back
			 * up so right is handled again.
			 */
			delta.delete(value1);
			right.back();
		}
	}
}
