package se.l4.otter.operations.internal.string;

import org.eclipse.collections.api.list.ListIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.otter.operations.ComposeException;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.AnnotationChange;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringHandler;
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

	private final MutableOperationIterator<StringHandler> left;
	private final MutableOperationIterator<StringHandler> right;

	private final AnnotationNormalizingDelta<Operation<StringHandler>> delta;

	private AnnotationChange annotationChanges;

	public StringTypeComposer(
		ListIterable<Operation<StringHandler>> first,
		ListIterable<Operation<StringHandler>> second)
	{
		log.debug("Composing {} with {}", first, second);

		delta = new AnnotationNormalizingDelta<>(StringDelta.builder(), () -> {
			AnnotationChange change = annotationChanges;
			annotationChanges = null;
			return change;
		});

		left = new MutableOperationIterator<>(first);
		right = new MutableOperationIterator<>(second);
	}

	public Operation<StringHandler> perform()
	{
		while(left.hasNext() && right.hasNext())
		{
			Operation<StringHandler> op1 = left.next();
			Operation<StringHandler> op2 = right.next();

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
			else if(op1 instanceof StringAnnotationChange)
			{
				handleAnnotationChange(op1, op2);
			}
		}

		while(left.hasNext())
		{
			Operation<StringHandler> op1 = left.next();
			if(op1 instanceof StringAnnotationChange || op1 instanceof StringDelete)
			{
				/*
				 * Annotation changes are zero-sized so they can always be
				 * composed.
				 */
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
			Operation<StringHandler> op = right.next();
			delta.adopt(op);
		}

		return delta.done();
	}

	private void handleRetain(Operation<StringHandler> op1,
			Operation<StringHandler> op2)
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
			String value2 = ((StringDelete) op2).getValue();
			int length2 = value2.length();

			if(length1 < length2)
			{
				delta.delete(value2.substring(0, length1));
				right.replace(new StringDelete(value2.substring(length1)));
			}
			else if(length2 < length1)
			{
				// Only replace if we deleted less than we retain
				delta.delete(value2);
				left.replace(new StringRetain(length1 - length2));
			}
			else
			{
				delta.delete(value2);
			}
		}
		else if(op2 instanceof StringAnnotationChange)
		{
			annotationChanges = DefaultAnnotationChange.merge(annotationChanges, ((StringAnnotationChange) op2).getChange());
			left.back();
		}
	}

	private void handleInsert(Operation<StringHandler> op1,
			Operation<StringHandler> op2)
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
		else if(op2 instanceof StringAnnotationChange)
		{
			annotationChanges = DefaultAnnotationChange.merge(annotationChanges, ((StringAnnotationChange) op2).getChange());
			left.back();
		}
	}

	private void handleDelete(Operation<StringHandler> op1, Operation<StringHandler> op2)
	{
		String value1 = ((StringDelete) op1).getValue();

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
		else if(op2 instanceof StringAnnotationChange)
		{
			annotationChanges = DefaultAnnotationChange.merge(annotationChanges, ((StringAnnotationChange) op2).getChange());
			left.back();
		}
	}

	private void handleAnnotationChange(Operation<StringHandler> op1, Operation<StringHandler> op2)
	{
		AnnotationChange change1 = ((StringAnnotationChange) op1).getChange();

		if(op2 instanceof StringAnnotationChange)
		{
			/*
			 * Right operation is also an annotation change. Merge the two
			 * annotations.
			 */
			AnnotationChange merged = DefaultAnnotationChange.merge(change1, ((StringAnnotationChange) op2).getChange());
			annotationChanges = DefaultAnnotationChange.merge(annotationChanges, merged);
		}
		else
		{
			/*
			 * Right operation is something else, queue the annotation changes
			 * and handle right again.
			 */
			annotationChanges = DefaultAnnotationChange.merge(annotationChanges, change1);
			right.back();
		}
	}
}
