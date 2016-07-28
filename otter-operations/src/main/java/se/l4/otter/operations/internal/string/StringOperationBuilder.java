package se.l4.otter.operations.internal.string;

import java.util.ArrayList;
import java.util.List;

import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.DefaultCompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringOperationHandler;

public class StringOperationBuilder
	implements StringOperationHandler
{
	private final List<Operation<StringOperationHandler>> operations;
	
	public StringOperationBuilder()
	{
		operations = new ArrayList<>();
	}

	@Override
	public void insert(String s)
	{
		if(! operations.isEmpty())
		{
			/*
			 * Check if the last operation was a delete, in which case we
			 * normalize a bit and enforce that the insert always comes
			 * before the delete. This makes transformation a bit easier
			 * to deal with.
			 */
			Operation<StringOperationHandler> previous = operations.get(operations.size() - 1);
			if(previous instanceof StringDelete)
			{
				operations.set(operations.size() - 1, new StringInsert(s));
				operations.add(previous);
				return;
			}
		}

		operations.add(new StringInsert(s));
	}

	@Override
	public void delete(String s)
	{
		operations.add(new StringDelete(s));
	}

	@Override
	public void retain(int count)
	{
		operations.add(new StringRetain(count));
	}

	public CompoundOperation<StringOperationHandler> build()
	{
		return new DefaultCompoundOperation<>(operations);
	}
}
