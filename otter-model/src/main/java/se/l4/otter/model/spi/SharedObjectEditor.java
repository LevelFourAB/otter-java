package se.l4.otter.model.spi;

import se.l4.otter.model.SharedObject;
import se.l4.otter.operations.Operation;

/**
 * Helper for implementations of {@link SharedObject}, contains id, type,
 * latest value and a method that can should be called when an operation is
 * performed locally on the object.
 * 
 * @author Andreas Holstenson
 *
 */
public interface SharedObjectEditor<Op extends Operation<?>>
{
	/**
	 * Get the identifier being used.
	 * 
	 * @return
	 */
	String getId();
	
	/**
	 * Get the type being used.
	 * 
	 * @return
	 */
	String getType();
	
	/**
	 * Get the current value.
	 * 
	 * @return
	 */
	Op getCurrent();
	
	/**
	 * Indicate that an operation has been applied.
	 * 
	 * @param op
	 */
	void send(Op op);

	/**
	 * Get an object from the model.
	 * 
	 * @param id
	 * @param type
	 * @return
	 */
	SharedObject getObject(String id, String type);
}
