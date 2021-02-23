package se.l4.otter.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A lock implementation that wraps a {@link Lock} and returns a
 * {@link CloseableLock} for it.
 *
 * @author Andreas Holstenson
 *
 */
public class DefaultLock
{
	private final Lock lock;
	private final CloseableLock instance;

	public DefaultLock()
	{
		this(new ReentrantLock());
	}

	public DefaultLock(Lock lock)
	{
		this.lock = lock;

		this.instance = createInstance();
	}

	private CloseableLock createInstance()
	{
		return new CloseableLock()
		{
			@Override
			public void close()
			{
				lock.unlock();
			}
		};
	}

	public CloseableLock acquire()
	{
		lock.lock();
		return instance;
	}
}
