package se.l4.otter.model.spi;

import se.l4.otter.model.SharedObject;
import se.l4.otter.operations.Operation;

/**
 * Abstract implementation of {@link SharedObject}.
 *
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public abstract class AbstractSharedObject<Op extends Operation<?>>
	implements SharedObject
{
	protected final SharedObjectEditor<Op> editor;

	public AbstractSharedObject(SharedObjectEditor<Op> editor)
	{
		this.editor = editor;
	}

	@Override
	public String getObjectId()
	{
		return editor.getId();
	}

	@Override
	public String getObjectType()
	{
		return editor.getType();
	}
}
