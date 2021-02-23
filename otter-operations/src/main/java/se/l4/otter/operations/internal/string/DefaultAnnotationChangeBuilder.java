package se.l4.otter.operations.internal.string;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.ValueChange;
import se.l4.otter.operations.string.AnnotationChange;
import se.l4.otter.operations.string.AnnotationChangeBuilder;

public class DefaultAnnotationChangeBuilder<ReturnPath>
	implements AnnotationChangeBuilder<ReturnPath>
{
	private final Function<AnnotationChange, ReturnPath> resultReceiver;
	private final Map<String, ValueChange> changes;

	public DefaultAnnotationChangeBuilder(Function<AnnotationChange, ReturnPath> resultReceiver)
	{
		this.resultReceiver = resultReceiver;
		this.changes = new HashMap<>();
	}

	@Override
	public AnnotationChangeBuilder<ReturnPath> set(String key, Object oldValue, Object newValue)
	{
		if(newValue == null)
		{
			throw new OperationException("New value can not be null, use remove to remove the annotation");
		}

		changes.put(key, new ValueChange(oldValue, newValue));
		return this;
	}

	@Override
	public AnnotationChangeBuilder<ReturnPath> remove(String key, Object currentValue)
	{
		if(currentValue == null)
		{
			throw new OperationException("Current value can not be null");
		}

		changes.put(key, new ValueChange(currentValue, null));
		return this;
	}

	@Override
	public ReturnPath done()
	{
		return resultReceiver.apply(new DefaultAnnotationChange(changes));
	}

}
