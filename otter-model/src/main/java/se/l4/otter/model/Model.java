package se.l4.otter.model;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedDelta;
import se.l4.otter.operations.combined.CombinedTarget;

public interface Model
	extends SharedMap
{
	/**
	 * Create a new map instance.
	 * 
	 * @return
	 */
	SharedMap newMap();
	
	/**
	 * Create a new string instance.
	 * 
	 * @return
	 */
	SharedString newString();
	
	/**
	 * Get the intitial operation that a new model represents.
	 * 
	 * @return
	 */
	static Operation<CombinedTarget> getInitialModel()
	{
		return CombinedDelta.builder()
			.done();
	}
}
