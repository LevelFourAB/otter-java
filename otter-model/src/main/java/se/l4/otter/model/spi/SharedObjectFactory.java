package se.l4.otter.model.spi;

import se.l4.otter.model.SharedObject;
import se.l4.otter.operations.Operation;

/**
 * Factory for {@link SharedObject}s.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 * @param <Op>
 */
public interface SharedObjectFactory<T extends SharedObject, Op extends Operation<?>>
{
	/**
	 * Create an object that uses the specified editor.
	 *
	 * @param editor
	 * @return
	 */
	T create(SharedObjectEditor<Op> editor);
}
