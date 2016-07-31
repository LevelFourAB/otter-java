package se.l4.otter.operations.internal.combined;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import se.l4.otter.operations.DefaultCompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.combined.CombinedDelta;
import se.l4.otter.operations.combined.CombinedHandler;

public class DefaultCombinedDelta<ReturnPath>
	implements CombinedDelta<ReturnPath>
{
	private final Function<Operation<CombinedHandler>, ReturnPath> resultHandler;
	private final Map<String, Operation<CombinedHandler>> ops;
	
	public DefaultCombinedDelta(Function<Operation<CombinedHandler>, ReturnPath> resultHandler)
	{
		this.resultHandler = resultHandler;
		
		ops = new HashMap<>();
	}

	@Override
	public CombinedDelta<ReturnPath> update(String id, String type, Operation<?> op)
	{
		if(ops.containsKey(id))
		{
			throw new OperationException("Can not update id `" +  id + "` several times");
		}
		
		ops.put(id, new Update(id, type, op));
		
		return this;
	}
	
	@Override
	public ReturnPath done()
	{
		return resultHandler.apply(new DefaultCompoundOperation<>(ImmutableList.copyOf(ops.values())));
	}
}
