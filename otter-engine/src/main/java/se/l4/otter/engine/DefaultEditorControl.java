package se.l4.otter.engine;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.l4.otter.lock.CloseableLock;
import se.l4.otter.operations.Composer;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;
import se.l4.ylem.ids.LongIdGenerator;
import se.l4.ylem.ids.SimpleLongIdGenerator;

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
	private final LongIdGenerator idGenerator;

	private final CloseableLock closeableLock;

	public DefaultEditorControl(OperationHistory<Op> history)
	{
		this(history, new ReentrantLock());
	}

	public DefaultEditorControl(OperationHistory<Op> history, Lock lock)
	{
		this(history, lock, new SimpleLongIdGenerator());
	}

	public DefaultEditorControl(OperationHistory<Op> history, Lock lock, LongIdGenerator idGenerator)
	{
		this.lock = lock;
		this.history = history;
		this.idGenerator = idGenerator;

		closeableLock = new CloseableLock()
		{
			@Override
			public void close()
			{
				lock.unlock();
			}
		};
	}

	@Override
	public OTType<Op> getType()
	{
		return history.getType();
	}

	@Override
	public CloseableLock lock()
	{
		lock.lock();
		return closeableLock;
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

			long sessionId = idGenerator.next();

			return new TaggedOperation<>(id, toString(sessionId), composed);
		}
	}

	@Override
	public long getLatestVersion()
	{
		return history.getLatest();
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

	private final static char[] DIGITS = {
		'0', '1', '2', '3', '4', '5',
		'6', '7', '8', '9', 'a', 'b',
		'c', 'd', 'e', 'f', 'g', 'h',
		'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't',
		'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F',
		'G', 'H', 'I', 'J', 'K', 'L',
		'M', 'N', 'O', 'P', 'Q', 'R',
		'S', 'T', 'U', 'V', 'W', 'X',
		'Y', 'Z'
	};

	private final static int MAX = DIGITS.length;

	public static String toString(long i)
	{
		char[] buf = new char[11];
		int charPos = 10;

		int radix = MAX;
		i = -i;
		while(i <= -radix)
		{
			buf[charPos--] = DIGITS[(int) (-(i % radix))];
			i = i / radix;
		}
		buf[charPos] = DIGITS[(int) (-i)];

		return new String(buf, charPos, (11 - charPos));
	}
}
