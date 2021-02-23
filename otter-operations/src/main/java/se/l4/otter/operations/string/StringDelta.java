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
	 * Update the current annotations.
	 *
	 * @return
	 */
	AnnotationChangeBuilder<StringDelta<ReturnPath>> updateAnnotations();

	/**
	 * Update the current annotations.
	 *
	 * @param change
	 * @return
	 */
	StringDelta<ReturnPath> updateAnnotations(AnnotationChange change);

	/**
	 * Advanced usage: Adopt a raw operation.
	 *
	 * @param op
	 * @return
	 */
	StringDelta<ReturnPath> adopt(Operation<StringHandler> op);

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
	static StringDelta<Operation<StringHandler>> builder()
	{
		return new DefaultStringDelta<>(o -> o);
	}

}
