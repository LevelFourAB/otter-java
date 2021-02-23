package se.l4.otter.engine.events;

import se.l4.otter.engine.Editor;

/**
 * Event containing information about an operation that has been applied
 * to an {@link Editor}.
 *
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public class ChangeEvent<Op>
{
	private final Op operation;
	private final boolean local;

	public ChangeEvent(Op operation, boolean local)
	{
		this.operation = operation;
		this.local = local;
	}

	public Op getOperation()
	{
		return operation;
	}

	public boolean isLocal()
	{
		return local;
	}

	public boolean isRemote()
	{
		return ! local;
	}
}
