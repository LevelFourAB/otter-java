package se.l4.otter.operations.internal;

import java.util.ArrayList;
import java.util.List;

import se.l4.otter.operations.Composer;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * Default implementation of {@link Composer}. Based on the same algorithm
 * that Apache Wave uses to merge composed operations in a tree like manner.
 *
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public class DefaultComposer<Op extends Operation<?>>
	implements Composer<Op>
{
	private final OTType<Op> type;

	private final List<Op> operations;

	public DefaultComposer(OTType<Op> type)
	{
		this.type = type;

		operations = new ArrayList<>();
	}

	@Override
	public Composer<Op> add(Op op)
	{
		if(operations.isEmpty())
		{
			operations.add(op);
		}
		else
		{
			for(int i=0, n=operations.size(); i<n; i++)
			{
				Op nextOp = operations.get(i);
				if(nextOp == null)
				{
					operations.set(i, op);
					return this;
				}

				operations.set(i, null);
				op = type.compose(nextOp, op);
			}
			operations.add(op);
		}

		return this;
	}

	@Override
	public Op done()
	{
		Op result = null;
		for(Op op : operations)
		{
			if(op != null)
			{
				if(result == null)
				{
					result = op;
				}
				else
				{
					result = type.compose(op, result);
				}
			}
		}
		return result;
	}
}
