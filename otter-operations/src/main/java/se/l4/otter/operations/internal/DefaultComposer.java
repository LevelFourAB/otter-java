package se.l4.otter.operations.internal;

import se.l4.otter.operations.Composer;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * Default implementation of {@link Composer}.
 * 
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public class DefaultComposer<Op extends Operation<?>>
	implements Composer<Op>
{
	private final OTType<Op> type;
	private Op result;

	public DefaultComposer(OTType<Op> type)
	{
		this.type = type;
	}
	
	@Override
	public Composer<Op> add(Op op)
	{
		if(result == null)
		{
			result = op;
		}
		else
		{
			result = type.compose(result, op);
		}
		return this;
	}
	
	@Override
	public Op done()
	{
		return result;
	}
}
