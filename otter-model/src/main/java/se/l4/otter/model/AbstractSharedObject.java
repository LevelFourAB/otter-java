package se.l4.otter.model;

import se.l4.otter.model.spi.SharedObjectEditor;
import se.l4.otter.operations.Operation;

public class AbstractSharedObject<Op extends Operation<?>>
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
