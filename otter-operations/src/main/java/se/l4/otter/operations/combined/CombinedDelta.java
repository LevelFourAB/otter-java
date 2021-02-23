package se.l4.otter.operations.combined;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.internal.combined.DefaultCombinedDelta;

/**
 * Delta builder for combined operations.
 *
 * @author Andreas Holstenson
 *
 * @param <ReturnPath>
 */
public interface CombinedDelta<ReturnPath>
{
	/**
	 * Update the object with the given id with the specified operation.
	 *
	 * @param id
	 * @param type
	 * @param op
	 * @return
	 */
	CombinedDelta<ReturnPath> update(String id, String type, Operation<?> op);

	/**
	 * Indicate that we are done building this delta.
	 *
	 * @return
	 */
	ReturnPath done();


	/**
	 * Get {@link CombinedDelta} that builds a {@link Operation}.
	 *
	 * @return
	 */
	static CombinedDelta<Operation<CombinedHandler>> builder()
	{
		return new DefaultCombinedDelta<>(o -> o);
	}
}
