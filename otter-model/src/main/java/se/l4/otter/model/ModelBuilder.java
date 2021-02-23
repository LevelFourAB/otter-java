package se.l4.otter.model;

import se.l4.otter.engine.Editor;
import se.l4.otter.model.spi.SharedObjectFactory;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * Builder for instances of {@link Model}.
 *
 * @author Andreas Holstenson
 *
 */
public interface ModelBuilder
{
	/**
	 * Register a custom type with this model. This also requires that
	 * the {@link Editor} used has access to a {@link OTType} with the same
	 * id.
	 *
	 * @param id
	 *   the identifier of the type, must match the one used in the {@link OTType}
	 * @param factory
	 *   the factory to use when creating instances
	 * @return
	 */
	<T extends SharedObject, Op extends Operation<?>> ModelBuilder addType(String id, SharedObjectFactory<T, Op> factory);

	/**
	 * Build the model.
	 *
	 * @return
	 */
	Model build();
}
