package se.l4.otter.operations.internal.string;

import java.util.function.Function;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringHandler;

public class DefaultStringDelta<ReturnPath>
	implements StringDelta<ReturnPath>
{
	private final Function<Operation<StringHandler>, ReturnPath> resultHandler;
	private final StringOperationBuilder builder;
	private final StringNormalizer normalizer;
	
	public DefaultStringDelta(Function<Operation<StringHandler>, ReturnPath> resultHandler)
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
	public StringHandler asHandler()
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
