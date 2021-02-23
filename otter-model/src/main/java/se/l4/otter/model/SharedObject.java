package se.l4.otter.model;

import se.l4.otter.model.spi.AbstractSharedObject;
import se.l4.otter.model.spi.SharedObjectEditor;

/**
 * Object that in a {@link Model} that uses realtime editing.
 *
 * <h2>Implementing an object</h2>
 *
 * Every SharedObject uses a {@link SharedObjectEditor} to make and receive
 * changes. A good start for any object is to extend
 * {@link AbstractSharedObject}.
 *
 * @author Andreas Holstenson
 *
 */
public interface SharedObject
{
	/**
	 * Get the unique identifier this object has been assigned.
	 *
	 * @return
	 */
	String getObjectId();

	/**
	 * Get the type of this object.
	 *
	 * @return
	 */
	String getObjectType();
}
