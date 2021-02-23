package se.l4.otter.model.internal;

import java.util.function.Consumer;
import java.util.function.Supplier;

import se.l4.otter.lock.CloseableLock;
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
	private final Consumer<Runnable> eventQueuer;

	private OperationHandler<Op> handler;

	public SharedObjectEditorImpl(
			DefaultModel model,
			String id,
			String type,
			Supplier<Op> supplier,
			Consumer<Op> applier,
			Consumer<Runnable> eventQueuer)
	{
		this.model = model;
		this.id = id;
		this.type = type;
		this.supplier = supplier;
		this.applier = applier;
		this.eventQueuer = eventQueuer;
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
	public CloseableLock lock()
	{
		return model.lock();
	}

	@Override
	public void apply(Op op)
	{
		applier.accept(op);
	}

	public void operationApplied(Op op, boolean local)
	{
		if(handler == null) return;

		handler.newOperation(op, local);
	}

	@Override
	public SharedObject getObject(String id, String type)
	{
		return model.getObject(id, type);
	}

	@Override
	public void setOperationHandler(OperationHandler<Op> handler)
	{
		this.handler = handler;
	}

	@Override
	public void queueEvent(Runnable runnable)
	{
		eventQueuer.accept(runnable);
	}
}
