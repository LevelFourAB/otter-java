package se.l4.otter.operations.string;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.internal.string.DefaultStringDelta;

/**
 * Utility for building a delta for {@link StringType}.
 * 
 * @author Andreas Holstenson
 *
 * @param <ReturnPath>
 */
public interface StringDelta<ReturnPath>
{
	/**
	 * Insert characters.
	 * 
	 * @param s
	 */
	StringDelta<ReturnPath> insert(String s);

	/**
	 * Delete characters.
	 * 
	 * @param s
	 */
	StringDelta<ReturnPath> delete(String s);

	/**
	 * Retain characters.
	 * 
	 * @param count
	 */
	StringDelta<ReturnPath> retain(int count);
	
	/**
	 * Get this delta as a {@link StringOperationHandler}.
	 * 
	 * @return
	 */
	StringOperationHandler asHandler();
	
	/**
	 * Indicate that we are done building this delta.
	 * 
	 * @return
	 */
	ReturnPath done();
	
	/**
	 * Get {@link StringDelta} that builds a {@link Operation}.
	 * 
	 * @return
	 */
	static StringDelta<Operation<StringOperationHandler>> builder()
	{
		return new DefaultStringDelta<>(o -> o);
	}

}
