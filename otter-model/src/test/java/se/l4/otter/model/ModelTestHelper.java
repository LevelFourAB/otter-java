package se.l4.otter.model;

import java.util.concurrent.atomic.AtomicInteger;

import se.l4.otter.engine.DefaultEditor;
import se.l4.otter.engine.DefaultEditorControl;
import se.l4.otter.engine.InMemoryOperationHistory;
import se.l4.otter.engine.LocalOperationSync;
import se.l4.otter.engine.OperationSync;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedDelta;
import se.l4.otter.operations.combined.CombinedTarget;
import se.l4.otter.operations.combined.CombinedType;
import se.l4.otter.operations.combined.CombinedTypeBuilder;

public class ModelTestHelper
{
	private static final CombinedType TYPE = new CombinedTypeBuilder()
		.build();
	
	private static final AtomicInteger EDITOR = new AtomicInteger();
		
	public static LocalOperationSync<Operation<CombinedTarget>> createSync()
	{
		DefaultEditorControl<Operation<CombinedTarget>> control = new DefaultEditorControl<>(
			new InMemoryOperationHistory<>(TYPE, CombinedDelta.builder().done()
		));
		return new LocalOperationSync<>(control);
	}
	
	public static Model createModel(OperationSync<Operation<CombinedTarget>> sync)
	{
		return new DefaultModel(new DefaultEditor<>("e" + EDITOR.incrementAndGet(), sync));
	}
}
