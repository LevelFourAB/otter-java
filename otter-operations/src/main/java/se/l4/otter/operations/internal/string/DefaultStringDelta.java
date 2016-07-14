package se.l4.otter.operations.internal.string;

import java.util.function.Function;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringOperationHandler;

public class DefaultStringDelta<ReturnPath>
	implements StringDelta<ReturnPath>
{
	private final Function<Operation<StringOperationHandler>, ReturnPath> resultHandler;
	private final StringOperationBuilder builder;
	private final StringNormalizer normalizer;
	
	public DefaultStringDelta(Function<Operation<StringOperationHandler>, ReturnPath> resultHandler)
	{
		this.resultHandler = resultHandler;
		builder = new StringOperationBuilder();
		normalizer = new StringNormalizer(builder);
	}
	
	@Override
	public StringDelta<ReturnPath> retain(int count)
	{
		normalizer.retain(count);
		return this;
	}
	
	@Override
	public StringDelta<ReturnPath> insert(String s)
	{
		normalizer.insert(s);
		return this;
	}
	
	@Override
	public StringDelta<ReturnPath> delete(String s)
	{
		normalizer.delete(s);
		return this;
	}
	
	@Override
	public StringOperationHandler asHandler()
	{
		return normalizer;
	}
	
	@Override
	public ReturnPath done()
	{
		normalizer.flush();
		return resultHandler.apply(builder.build());
	}
}
