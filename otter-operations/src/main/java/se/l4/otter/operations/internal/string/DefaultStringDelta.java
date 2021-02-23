package se.l4.otter.operations.internal.string;

import java.util.function.Function;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import se.l4.otter.operations.DefaultCompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.AnnotationChange;
import se.l4.otter.operations.string.AnnotationChangeBuilder;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringHandler;

public class DefaultStringDelta<ReturnPath>
	implements StringDelta<ReturnPath>
{
	private enum State
	{
		EMPTY,
		RETAIN,
		INSERT,
		DELETE,
		ANNOTATIONS
	}

	private final Function<Operation<StringHandler>, ReturnPath> resultHandler;
	private final MutableList<Operation<StringHandler>> operations;

	private State state;

	private StringBuilder characters;
	private int retainCount;

	private AnnotationChange annotationChange;

	public DefaultStringDelta(Function<Operation<StringHandler>, ReturnPath> resultHandler)
	{
		this.resultHandler = resultHandler;
		operations = Lists.mutable.empty();

		characters = new StringBuilder();
		state = State.EMPTY;
	}

	public void flush()
	{
		switch(state)
		{
			case RETAIN:
				if(retainCount > 0)
				{
					operations.add(new StringRetain(retainCount));
				}
				break;
			case INSERT:
				if(characters.length() > 0)
				{
					StringInsert op = new StringInsert(characters.toString());

					/*
					 * Check if the last operation was a delete, in which case we
					 * normalize a bit and enforce that the insert always comes
					 * before the delete. This makes transformation a bit easier
					 * to deal with.
					 */
					Operation<StringHandler> previous = operations.isEmpty() ? null : operations.get(operations.size() - 1);
					if(previous instanceof StringDelete)
					{
						operations.set(operations.size() - 1, op);
						operations.add(previous);
					}
					else
					{
						operations.add(op);
					}
				}
				break;
			case DELETE:
				if(characters.length() > 0)
				{
					operations.add(new StringDelete(characters.toString()));
				}
				break;
			case ANNOTATIONS:
				if(annotationChange != null)
				{
					operations.add(new StringAnnotationChange(annotationChange));
				}
				break;
		}

		characters.setLength(0);
		retainCount = 0;
		annotationChange = null;
	}

	private void switchState(State state)
	{
		if(this.state != state)
		{
			flush();
		}

		this.state = state;
	}

	@Override
	public StringDelta<ReturnPath> retain(int count)
	{
		if(count <= 0) return this;

		switchState(State.RETAIN);

		retainCount += count;
		return this;
	}

	@Override
	public StringDelta<ReturnPath> insert(String s)
	{
		switchState(State.INSERT);
		characters.append(s);
		return this;
	}

	@Override
	public StringDelta<ReturnPath> delete(String s)
	{
		switchState(State.DELETE);
		characters.append(s);
		return this;
	}

	@Override
	public AnnotationChangeBuilder<StringDelta<ReturnPath>> updateAnnotations()
	{
		return new DefaultAnnotationChangeBuilder<>(this::updateAnnotations);
	}

	@Override
	public StringDelta<ReturnPath> updateAnnotations(AnnotationChange change)
	{
		if(change.isEmpty()) return this;

		switchState(State.ANNOTATIONS);
		annotationChange = DefaultAnnotationChange.merge(annotationChange, change);
		return this;
	}

	@Override
	public StringDelta<ReturnPath> adopt(Operation<StringHandler> op)
	{
		if(op instanceof StringRetain)
		{
			retain(((StringRetain) op).getLength());
		}
		else if(op instanceof StringInsert)
		{
			insert(((StringInsert) op).getValue());
		}
		else if(op instanceof StringDelete)
		{
			delete(((StringDelete) op).getValue());
		}
		else if(op instanceof StringAnnotationChange)
		{
			updateAnnotations(((StringAnnotationChange) op).getChange());
		}
		else
		{
			throw new IllegalArgumentException("Unknown operation: " + op);
		}

		return this;
	}

	@Override
	public ReturnPath done()
	{
		flush();
		return resultHandler.apply(new DefaultCompoundOperation<>(operations.toImmutable()));
	}
}
