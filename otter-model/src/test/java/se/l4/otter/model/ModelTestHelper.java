package se.l4.otter.model;

import se.l4.otter.engine.DefaultEditor;
import se.l4.otter.engine.DefaultEditorControl;
import se.l4.otter.engine.InMemoryOperationHistory;
import se.l4.otter.engine.LocalOperationSync;
import se.l4.otter.engine.OperationSync;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedDelta;
import se.l4.otter.operations.combined.CombinedHandler;
import se.l4.otter.operations.combined.CombinedType;
import se.l4.otter.operations.combined.CombinedTypeBuilder;

public class ModelTestHelper
{
	private static final CombinedType TYPE = new CombinedTypeBuilder()
		.build();

	public static LocalOperationSync<Operation<CombinedHandler>> createSync()
	{
		DefaultEditorControl<Operation<CombinedHandler>> control = new DefaultEditorControl<>(
			new InMemoryOperationHistory<>(TYPE, CombinedDelta.builder().done()
		));
		return new LocalOperationSync<>(control);
	}

	public static Model createModel(OperationSync<Operation<CombinedHandler>> sync)
	{
		return Model.builder(new DefaultEditor<>(sync)).build();
	}
}
