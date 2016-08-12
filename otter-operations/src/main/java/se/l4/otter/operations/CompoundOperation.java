package se.l4.otter.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
	List<Operation<Handler>> getOperations();
	
	/**
	 * Turn a operation into a list of operations.
	 * 
	 * @param op
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T> List<Operation<T>> toList(Operation<T> op)
	{
		if(op instanceof CompoundOperation)
		{
			return ((CompoundOperation) op).getOperations();
		}
		else if(op == null)
		{
			return Collections.emptyList();
		}
		
		return Collections.singletonList(op);
	}
	
	/**
	 * Turn a operation into a list of operations while sorting the
	 * operations.
	 * 
	 * @param op
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T> List<Operation<T>> toList(Operation<T> op, Comparator<Operation<T>> comparator)
	{
		if(op instanceof CompoundOperation)
		{
			List<Operation<T>> list = new ArrayList<>(((CompoundOperation) op).getOperations());
			Collections.sort(list, comparator);
			return list;
		}
		
		return Collections.singletonList(op);
	}

	static <T> Operation<T> empty()
	{
		return new DefaultCompoundOperation<>(Collections.emptyList());
	}
}
