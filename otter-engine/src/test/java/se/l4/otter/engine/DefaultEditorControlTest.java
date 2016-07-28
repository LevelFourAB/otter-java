package se.l4.otter.engine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringOperationHandler;
import se.l4.otter.operations.string.StringType;

public class DefaultEditorControlTest
{
	private EditorControl<Operation<StringOperationHandler>> control;

	@Before
	public void setupEditorControl()
	{
		control = new DefaultEditorControl<>(
			new InMemoryOperationHistory<>(new StringType(), StringDelta.builder()
				.insert("Hello World")
				.done()
			)
		);
	}
	
	@Test
	public void testGetLatest()
	{
		TaggedOperation<Operation<StringOperationHandler>> latest = control.getLatest();
		assertThat(latest.getOperation(), is(StringDelta.builder()
			.insert("Hello World")
			.done()
		));
	}
	
	@Test
	public void testEdit()
	{
		long historyId = control.getLatest().getHistoryId();
		control.store(historyId, "1", StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookie")
			.done()
		);
		
		TaggedOperation<Operation<StringOperationHandler>> latest = control.getLatest();
		assertThat(latest.getOperation(), is(StringDelta.builder()
			.insert("Hello Cookie")
			.done()
		));
	}
	
	@Test
	public void testEditsFromSameHistoryId()
	{
		long historyId = control.getLatest().getHistoryId();
		control.store(historyId, "1", StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookie")
			.done()
		);
		
		control.store(historyId, "1", StringDelta.builder()
			.retain(11)
			.insert("!")
			.done()
		);
		
		TaggedOperation<Operation<StringOperationHandler>> latest = control.getLatest();
		assertThat(latest.getOperation(), is(StringDelta.builder()
			.insert("Hello Cookie!")
			.done()
		));
	}
}
