package se.l4.otter.model.internal;

import java.util.HashMap;
import java.util.Map;

import se.l4.otter.engine.Editor;
import se.l4.otter.model.DefaultModel;
import se.l4.otter.model.Model;
import se.l4.otter.model.ModelBuilder;
import se.l4.otter.model.SharedObject;
import se.l4.otter.model.spi.SharedObjectEditor;
import se.l4.otter.model.spi.SharedObjectFactory;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedHandler;

public class DefaultModelBuilder
	implements ModelBuilder
{
	private final Editor<Operation<CombinedHandler>> editor;
	private final Map<String, SharedObjectFactory<?, ?>> types;

	public DefaultModelBuilder(Editor<Operation<CombinedHandler>> editor)
	{
		this.editor = editor;

		types = new HashMap<>();

		addType("map", SharedMapImpl::new);
		addType("string", SharedStringImpl::new);
		addType("list", e -> new SharedListImpl<Object>((SharedObjectEditor) e));
	}

	@Override
	public <T extends SharedObject, Op extends Operation<?>> ModelBuilder addType(String id, SharedObjectFactory<T, Op> factory)
	{
		types.put(id, factory);
		return this;
	}

	@Override
	public Model build()
	{
		return new DefaultModel(editor, types);
	}
}
