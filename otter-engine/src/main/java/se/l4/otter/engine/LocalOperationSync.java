package se.l4.otter.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;

/**
 * Implementation of {@link OperationSync} for keeping several editors within
 * the same JVM in sync. Mostly useful for testing.
 *
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public class LocalOperationSync<Op extends Operation<?>>
	implements OperationSync<Op>
{
	private static final Logger log = LoggerFactory.getLogger(LocalOperationSync.class);

	@SuppressWarnings("rawtypes")
	private static final Consumer[] EMPTY = new Consumer[0];

	private final EditorControl<Op> control;
	private volatile Consumer<TaggedOperation<Op>>[] listeners;

	private final Thread triggerThread;
	private List<TaggedOperation<Op>> suspended;

	private final BlockingQueue<TaggedOperation<Op>> queue;
	private volatile boolean working;

	@SuppressWarnings("unchecked")
	public LocalOperationSync(EditorControl<Op> control)
	{
		this.control = control;

		listeners = EMPTY;

		queue = new LinkedBlockingQueue<>();
		triggerThread = new Thread(this::send, "local-operation-sync-thread");
		triggerThread.start();
	}

	@Override
	public OTType<Op> getType()
	{
		return control.getType();
	}

	/**
	 * Suspend sending of events.
	 */
	public void suspend()
	{
		if(suspended != null) return;

		suspended = new ArrayList<>();
	}

	public void resume()
	{
		List<TaggedOperation<Op>> suspended = this.suspended;
		this.suspended = null;

		queue.addAll(suspended);
	}

	public void waitForEmpty()
	{
		while(! Thread.interrupted())
		{
			try
			{
				Thread.sleep(100);

				if(queue.isEmpty() && ! working) return;
			}
			catch(InterruptedException e)
			{
				return;
			}
		}
	}

	@Override
	public TaggedOperation<Op> connect(Consumer<TaggedOperation<Op>> listener)
	{
		synchronized(this)
		{
			Consumer<TaggedOperation<Op>>[] listeners = Arrays.copyOf(this.listeners, this.listeners.length + 1);
			listeners[listeners.length - 1] = listener;
			this.listeners = listeners;
		}

		return control.getLatest();
	}

	@Override
	public void send(TaggedOperation<Op> op)
	{
		if(suspended != null)
		{
			suspended.add(op);
			return;
		}

		queue.add(op);
	}

	private void send()
	{
		while(! Thread.interrupted())
		{
			try
			{
				TaggedOperation<Op> tagged = queue.take();
				working = true;
				TaggedOperation<Op> toBroadcast = control.store(tagged);

				Consumer<TaggedOperation<Op>>[] listeners = this.listeners;
				for(Consumer<TaggedOperation<Op>> listener : listeners)
				{
					listener.accept(toBroadcast);
				}
				working = false;
			}
			catch(InterruptedException e)
			{
				return;
			}
			catch(Exception e)
			{
				log.warn("Could not apply operation: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public void close()
	{
		triggerThread.interrupt();
	}
}
