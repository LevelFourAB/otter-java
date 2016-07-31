package se.l4.otter.engine;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import se.l4.otter.EventHelper;
import se.l4.otter.engine.events.ChangeEvent;
import se.l4.otter.lock.CloseableLock;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;

/**
 * Default implementation of {@link Editor}. This editor uses an instance
 * of {@link OperationSync} to synchronize with a central server. It also
 * requires a unique session id that is used to identify when its changes
 * have been accepted by the server.
 * 
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public class DefaultEditor<Op extends Operation<?>>
	implements Editor<Op>
{
	enum State
	{
		SYNCHRONIZED,
		AWAITING_CONFIRM,
		AWAITING_CONFIRM_WITH_BUFFER
	}

	@SuppressWarnings("rawtypes")
	private static final Consumer[] EMPTY = new Consumer[0];
	
	private final String id;
	private final OTType<Op> type;
	private final OperationSync<Op> sync;
	private final Lock lock;
	
	private final EventHelper<EditorListener<Op>> listeners;
	
	private State state;
	
	private long parentHistoryId;
	private int lastId;
	
	private TaggedOperation<Op> lastSent;
	private TaggedOperation<Op> buffer;

	private Op current;
	
	private Op composing;
	private boolean locked;

	private CloseableLock closeableLock;
	
	@SuppressWarnings("unchecked")
	public DefaultEditor(String uniqueSessionId, OperationSync<Op> sync)
	{
		this.id = uniqueSessionId;
		this.sync = sync;
		this.type = sync.getType();
		lock = new ReentrantLock();
		
		state = State.SYNCHRONIZED;
		
		listeners = new EventHelper<>();
		
		closeableLock = new CloseableLock()
		{
			@Override
			public void close()
			{
				locked = false;
				if(composing != null)
				{
					apply(composing);
					composing = null;
				}
				
				lock.unlock();
			}
		};
		
		lock.lock();
		try
		{
			TaggedOperation<Op> initial = sync.connect(this::receive);
			parentHistoryId = initial.getHistoryId();
			current = initial.getOperation();
		}
		finally
		{
			lock.unlock();
		}
	}
	
	@Override
	public String getId()
	{
		return id;
	}
	
	@Override
	public OTType<Op> getType()
	{
		return type;
	}
	
	@Override
	public Op getCurrent()
	{
		return current;
	}
	
	@Override
	public void addListener(EditorListener<Op> listener)
	{
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(EditorListener<Op> listener)
	{
		listeners.remove(listener);
	}
	
	private void receive(TaggedOperation<Op> op)
	{
		lock.lock();
		try
		{
			switch(state)
			{
				case SYNCHRONIZED:
					/*
					 * No local changes, simply send operation to listeners.
					 */
					parentHistoryId = op.getHistoryId();
					composeAndTriggerListeners(op.getOperation());
					break;
				case AWAITING_CONFIRM:
					if(lastSent.getToken().equals(op.getToken()))
					{
						/*
						 * This is the operation we previously sent, we have
						 * already applied this locally so we can safely switch
						 * to synchronized state.
						 */
						parentHistoryId = op.getHistoryId();
						this.state = State.SYNCHRONIZED;
						
					}
					else
					{
						/*
						 * Someone else has edited the document before our own
						 * operation was applied. Transform the incoming operation
						 * over our sent operation.
						 */
						OperationPair<Op> transformed = type.transform(
							op.getOperation(),
							lastSent.getOperation()
						);
						
						/*
						 * We stay in our current state but replace lastSent
						 * with the transformed operation so any other edits
						 * can be safely applied.
						 */
						lastSent = new TaggedOperation<>(
							op.getHistoryId(),
							lastSent.getToken(),
							transformed.getRight()
						);
						
						parentHistoryId = op.getHistoryId();
						composeAndTriggerListeners(transformed.getLeft());
					}
					break;
				case AWAITING_CONFIRM_WITH_BUFFER:
					if(lastSent.getToken().equals(op.getToken()))
					{
						/*
						 * This is the operation we previously sent, so request
						 * that we send our buffer and switch to awaiting
						 * confirm.
						 */
						parentHistoryId = op.getHistoryId();
						state = State.AWAITING_CONFIRM;
						
						buffer = new TaggedOperation<>(
							op.getHistoryId(),
							buffer.getToken(),
							buffer.getOperation()
						);
						lastSent = buffer;
						
						sync.send(buffer);
					}
					else
					{
						/*
						 * Someone else has edited the document, rewrite
						 * both the incoming and our buffered operation.
						 */
						OperationPair<Op> transformed = type.transform(
							op.getOperation(),
							lastSent.getOperation()
						);
						
						/*
						 * As for awaiting confirm, we replace lastSent with
						 * a transformed operation.
						 */
						lastSent = new TaggedOperation<>(
							op.getHistoryId(),
							lastSent.getToken(),
							transformed.getRight()
						);
						
						/*
						 * Transform the already transformed remote operation
						 * over our buffer.
						 */
						transformed = type.transform(
							buffer.getOperation(),
							transformed.getLeft()
						);
						
						buffer = new TaggedOperation<>(
							op.getHistoryId(),
							buffer.getToken(),
							transformed.getLeft()
						);
						
						parentHistoryId = op.getHistoryId();
						composeAndTriggerListeners(transformed.getRight());
					}
					break;
				default:
					throw new AssertionError("Unknown state: " + state);
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	@Override
	public void apply(Op op)
	{
		lock.lock();
		try
		{
			if(locked)
			{
				// If we are currently locked, compose together with previous op
				composing = composing == null ? op : type.compose(composing, op);
				return;
			}
			
			// Compose together with the current operation
			current = type.compose(current, op);
			
			switch(state)
			{
				case SYNCHRONIZED:
				{
					/*
					 * Create a tagged version with a unique token and
					 * start tracking when it is applied.
					 */
					TaggedOperation<Op> tagged = new TaggedOperation<>(
						parentHistoryId,
						id + "-" + (lastId++),
						op
					);
					
					state = State.AWAITING_CONFIRM;
					lastSent = tagged;
					sync.send(tagged);
					
					break;
				}
				case AWAITING_CONFIRM:
				{
					/*
					 * We are already waiting for another operation to be applied,
					 * buffer this one.
					 */
					TaggedOperation<Op> tagged = new TaggedOperation<>(
						parentHistoryId,
						id + "-" + (lastId++),
						op
					);
					
					buffer = tagged;
					state = State.AWAITING_CONFIRM_WITH_BUFFER;
					break;
				}
				case AWAITING_CONFIRM_WITH_BUFFER:
				{
					/*
					 * We have something buffered, compose the buffer together
					 * with this edit.
					 */
					buffer = new TaggedOperation<>(
						buffer.getHistoryId(),
						buffer.getToken(),
						type.compose(buffer.getOperation(), op)
					);
					break;
				}
				default:
					throw new AssertionError("Unknown state: " + state);
			}
			
			ChangeEvent<Op> event = new ChangeEvent<>(op, true);
			listeners.trigger(l -> l.editorChanged(event));
		}
		finally
		{
			lock.unlock();
		}
	}
	
	private void composeAndTriggerListeners(Op op)
	{
		current = type.compose(current, op);
		
		ChangeEvent<Op> event = new ChangeEvent<>(op, false);
		listeners.trigger(l -> l.editorChanged(event));
	}

	@Override
	public CloseableLock lock()
	{
		lock.lock();
		locked = true;
		return closeableLock;
	}
}
