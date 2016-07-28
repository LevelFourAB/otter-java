package se.l4.otter.model;

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
