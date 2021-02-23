package se.l4.otter.operations.combined;

import com.google.common.collect.ImmutableMap;

import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.list.ListType;
import se.l4.otter.operations.map.MapType;
import se.l4.otter.operations.string.StringType;

/**
 * Builder for instances of {@link CombinedType}.
 *
 * @author Andreas Holstenson
 *
 */
public class CombinedTypeBuilder
{
	private final ImmutableMap.Builder<String, OTType<Operation<?>>> types;

	public CombinedTypeBuilder()
	{
		types = ImmutableMap.builder();

		withSubType("map", new MapType());
		withSubType("list", new ListType());
		withSubType("string", new StringType());
	}

	public <Op extends Operation<?>> CombinedTypeBuilder withSubType(String id, OTType<Op> type)
	{
		types.put(id, (OTType) type);

		return this;
	}

	public CombinedType build()
	{
		return new CombinedType(types.build());
	}
}
