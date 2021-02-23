package se.l4.otter.engine;

import java.util.Iterator;

/**
 * Extension to {@link Iterator} that can also be {@link #close() closed}.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface CloseableIterator<T>
	extends Iterator<T>, AutoCloseable
{
	/**
	 * Close this iterator.
	 *
	 */
	@Override
	void close();
}
