package se.l4.otter.operations.combined;

import se.l4.otter.operations.Operation;

public interface CombinedHandler
{
	void update(String id, String type, Operation<?> change);
}
