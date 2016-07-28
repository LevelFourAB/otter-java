package se.l4.otter.engine;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.l4.otter.operations.Composer;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;

/**
 * {@link EditorControl} implementation that stores operations using
 * {@link OperationHistory}. This implementation uses locking to guarantee
 * consistency. If created without specifying a {@link Lock} it will use a
 * {@link ReentrantLock} and the instance must be reused for all edits to
 * the same document/model. Optionally you can specify your own lock if you
 * for example want to support edits using several servers. 
 * 
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public class DefaultEditorControl<Op extends Operation<?>>
	implements EditorControl<Op>
{
	private final OperationHistory<Op> history;
	private final Lock lock;

	public DefaultEditorControl(OperationHistory<Op> history)
	{
		this(history, new ReentrantLock());
	}
	
	public DefaultEditorControl(OperationHistory<Op> history, Lock lock)
	{
		this.lock = lock;
		this.history = history;
	}
	
	@Override
	public TaggedOperation<Op> getLatest()
	{
		long id = history.getLatest();
		try(CloseableIterator<Op> it = history.until(id + 1))
		{
			Composer<Op> composer = history.getType().newComposer();
			
			while(it.hasNext())
			{
				Op op = it.next();
				composer.add(op);
			}
			
			Op composed = composer.done();
			return new TaggedOperation<>(id, null, composed);
		}
	}
	
	@Override
	public TaggedOperation<Op> store(TaggedOperation<Op> operation)
	{
		return store(operation.getHistoryId(), operation.getToken(), operation.getOperation());
	}
	
	@Override
	public TaggedOperation<Op> store(long historyBase, String token, Op operation)
	{
		lock.lock();
		try
		{
			long latest = history.getLatest();
			
			// Get all of the operations that have occurred after our history
			Op composed;
			try(CloseableIterator<Op> it = history.from(historyBase + 1))
			{
				Composer<Op> composer = history.getType().newComposer();
				
				while(it.hasNext())
				{
					composer.add(it.next());
				}
				
				composed = composer.done();
			}
			
			// Transform the new operation on top of the composed operation
			Op transformed;
			if(composed == null)
			{
				transformed = operation;
			}
			else
			{
				OperationPair<Op> pair = history.getType().transform(composed, operation);
				transformed = pair.getRight();
			}
			
			latest = history.store(transformed);
			return new TaggedOperation<>(latest, token, transformed);
		}
		finally
		{
			lock.unlock();
		}
	}
}
