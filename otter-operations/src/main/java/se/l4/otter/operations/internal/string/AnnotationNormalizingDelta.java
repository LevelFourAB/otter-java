package se.l4.otter.operations.internal.string;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.ValueChange;
import se.l4.otter.operations.string.AnnotationChange;
import se.l4.otter.operations.string.AnnotationChangeBuilder;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringHandler;

/**
 * Extension to {@link StringDelta} that will attempt to normalize annotations
 * when composing and transforming.
 *
 * @author Andreas Holstenson
 *
 * @param <ReturnPath>
 */
public class AnnotationNormalizingDelta<ReturnPath>
	implements StringDelta<ReturnPath>
{
	private final StringDelta<ReturnPath> delta;
	private final Supplier<AnnotationChange> annotationProvider;
	private final Map<String, ValueChange> tracker;

	public AnnotationNormalizingDelta(StringDelta<ReturnPath> delta, Supplier<AnnotationChange> annotationProvider)
	{
		this.delta = delta;
		this.annotationProvider = annotationProvider;
		tracker = new HashMap<>();
	}

	@Override
	public StringDelta<ReturnPath> insert(String s)
	{
		flush();
		delta.insert(s);
		return this;
	}

	@Override
	public StringDelta<ReturnPath> delete(String s)
	{
		delta.delete(s);
		return this;
	}

	@Override
	public StringDelta<ReturnPath> retain(int count)
	{
		flush();
		delta.retain(count);
		return this;
	}

	@Override
	public AnnotationChangeBuilder<StringDelta<ReturnPath>> updateAnnotations()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public StringDelta<ReturnPath> updateAnnotations(AnnotationChange change)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public StringDelta<ReturnPath> adopt(Operation<StringHandler> op)
	{
		if(! (op instanceof StringDelete))
		{
			flush();
		}

		delta.adopt(op);
		return this;
	}

	@Override
	public ReturnPath done()
	{
		flush();
		return delta.done();
	}

	private void flush()
	{
		AnnotationChange change = annotationProvider.get();
		if(change != null && ! change.isEmpty())
		{
			AnnotationChangeBuilder<StringDelta<ReturnPath>> annotations = delta.updateAnnotations();
			for(String key : change.keys())
			{
				ValueChange previous = tracker.get(key);

				ValueChange value = change.getChange(key);
				if(value.getNewValue() == null)
				{
					// This annotation key is being removed
					tracker.remove(key);
					if(previous != null)
					{
						if(Objects.equals(previous.getNewValue(), value.getOldValue()))
						{
							annotations.remove(key, previous.getNewValue());
						}
					}
					else
					{
						annotations.remove(key, value.getOldValue());
					}
				}
				else
				{
					// Annotation is being changed
					if(previous == null)
					{
						annotations.set(key, value.getOldValue(), value.getNewValue());
					}
					else
					{
						annotations.set(key, previous.getNewValue(), value.getNewValue());
					}

					tracker.put(key, value);
				}
			}
			annotations.done();
		}
	}

}
