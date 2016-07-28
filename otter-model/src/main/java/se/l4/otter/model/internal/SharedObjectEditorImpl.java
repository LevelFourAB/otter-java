package se.l4.otter.model.internal;

import java.util.function.Consumer;
import java.util.function.Supplier;

import se.l4.otter.model.DefaultModel;
import se.l4.otter.model.SharedObject;
import se.l4.otter.model.spi.SharedObjectEditor;
import se.l4.otter.operations.Operation;

public class SharedObjectEditorImpl<Op extends Operation<?>>
	implements SharedObjectEditor<Op>
{
	private final String id;
	private final String type;
	private final Supplier<Op> supplier;
	private final Consumer<Op> applier;
	private final DefaultModel model;

	public SharedObjectEditorImpl(
			DefaultModel model,
			String id,
			String type,
			Supplier<Op> supplier,
			Consumer<Op> applier)
	{
		this.model = model;
		this.id = id;
		this.type = type;
		this.supplier = supplier;
		this.applier = applier;
	}
	
	@Override
	public String getId()
	{
		return id;
	}
	
	@Override
	public String getType()
	{
		return type;
	}
	
	@Override
	public Op getCurrent()
	{
		return supplier.get();
	}
	
	@Override
	public void send(Op op)
	{
		applier.accept(op);
	}
	
	@Override
	public SharedObject getObject(String id, String type)
	{
		return model.getObject(id, type);
	}
}
