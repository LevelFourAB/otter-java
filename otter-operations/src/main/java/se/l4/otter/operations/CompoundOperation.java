package se.l4.otter.operations;

import java.util.Collections;
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
		
		return Collections.singletonList(op);
	}
}
