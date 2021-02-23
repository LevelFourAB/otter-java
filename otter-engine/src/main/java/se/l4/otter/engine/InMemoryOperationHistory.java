package se.l4.otter.engine;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * A {@link OperationHistory} that simply stores everything in memory without
 * any compaction. This class is useful for testing, but for long term storage
 * you will need something better.
 *
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public class InMemoryOperationHistory<Op extends Operation<?>>
	implements OperationHistory<Op>
{
	private final OTType<Op> type;
	private final SortedMap<Long, Op> operations;

	public InMemoryOperationHistory(OTType<Op> type, Op initial)
	{
		this.type = type;

		operations = new ConcurrentSkipListMap<>();
		operations.put(1l, initial);
	}

	@Override
	public OTType<Op> getType()
	{
		return type;
	}

	@Override
	public long store(Op op)
	{
		synchronized(this)
		{
			long id = operations.lastKey() + 1;
			operations.put(id, op);
			return id;
		}
	}

	@Override
	public long getLatest()
	{
		return operations.lastKey();
	}

	@Override
	public CloseableIterator<Op> between(long start, long end)
	{
		SortedMap<Long, Op> map = operations.subMap(start, end);
		return new IteratorImpl<>(map.values().iterator());
	}

	@Override
	public CloseableIterator<Op> from(long historyId)
	{
		SortedMap<Long, Op> map = operations.tailMap(historyId);
		return new IteratorImpl<>(map.values().iterator());
	}

	@Override
	public CloseableIterator<Op> until(long historyId)
	{
		SortedMap<Long, Op> map = operations.headMap(historyId);
		return new IteratorImpl<>(map.values().iterator());
	}

	private static class IteratorImpl<T>
		implements CloseableIterator<T>
	{
		private final Iterator<T> it;

		public IteratorImpl(Iterator<T> it)
		{
			this.it = it;
		}

		@Override
		public void close()
		{
		}

		@Override
		public boolean hasNext()
		{
			return it.hasNext();
		}

		@Override
		public T next()
		{
			return it.next();
		}
	}
}
