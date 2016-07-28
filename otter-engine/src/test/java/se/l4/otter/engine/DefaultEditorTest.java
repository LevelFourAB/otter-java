package se.l4.otter.engine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringOperationHandler;
import se.l4.otter.operations.string.StringType;

public class DefaultEditorTest
{
	private static final StringType TYPE = new StringType();
	
	private DefaultEditorControl<Operation<StringOperationHandler>> control;
	private LocalOperationSync<Operation<StringOperationHandler>> sync;

	@Before
	public void setupEditor()
	{
		control = new DefaultEditorControl<>(
				new InMemoryOperationHistory<>(TYPE, StringDelta.builder()
				.insert("Hello World")
				.done()
			)
		);
		sync = new LocalOperationSync<>(control);
	}
	
	private Editor<Operation<StringOperationHandler>> editor(String id)
	{
		return new DefaultEditor<>(id, TYPE, sync);
	}
	
	@Test
	public void test1()
	{
		Editor<Operation<StringOperationHandler>> editor = editor("1");
		
		editor.apply(StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.done()
		);
		
		sync.waitForEmpty();
		
		assertThat(editor.getCurrent(), is(StringDelta.builder()
			.insert("Hello Cookies")
			.done()
		));
	}
	
	@Test
	public void testMultiple1()
	{
		Editor<Operation<StringOperationHandler>> e1 = editor("1");
		Editor<Operation<StringOperationHandler>> e2 = editor("2");
		
		e1.apply(StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.done()
		);
		
		sync.waitForEmpty();
		
		assertThat(e2.getCurrent(), is(StringDelta.builder()
			.insert("Hello Cookies")
			.done()
		));
	}
	
	@Test
	public void testMultiple2()
	{
		Editor<Operation<StringOperationHandler>> e1 = editor("1");
		Editor<Operation<StringOperationHandler>> e2 = editor("2");
		
		sync.suspend();
		e1.apply(StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.done()
		);
		
		e2.apply(StringDelta.builder()
			.retain(11)
			.insert("!")
			.done()
		);
		sync.resume();
		
		sync.waitForEmpty();
		
		assertThat(e1.getCurrent(), is(StringDelta.builder()
			.insert("Hello Cookies!")
			.done()
		));
		
		assertThat(e2.getCurrent(), is(StringDelta.builder()
			.insert("Hello Cookies!")
			.done()
		));
		
	}
	
	@Test
	public void testMultiple3()
	{
		Editor<Operation<StringOperationHandler>> e1 = editor("1");
		Editor<Operation<StringOperationHandler>> e2 = editor("2");
		
		sync.suspend();
		e1.apply(StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.done()
		);
		
		e2.apply(StringDelta.builder()
			.retain(11)
			.insert("!")
			.done()
		);
		e2.apply(StringDelta.builder()
			.retain(11)
			.insert("!")
			.retain(1)
			.done()
		);
		sync.resume();
		
		sync.waitForEmpty();
		
		assertThat(e1.getCurrent(), is(StringDelta.builder()
			.insert("Hello Cookies!!")
			.done()
		));
		
		assertThat(e2.getCurrent(), is(StringDelta.builder()
			.insert("Hello Cookies!!")
			.done()
		));
	}
	
	@Test
	public void testMultiple4()
	{
		Editor<Operation<StringOperationHandler>> e1 = editor("1");
		Editor<Operation<StringOperationHandler>> e2 = editor("2");
		
		sync.suspend();
		e1.apply(StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.done()
		);
		
		e2.apply(StringDelta.builder()
			.retain(11)
			.insert("!")
			.done()
		);
		
		e1.apply(StringDelta.builder()
			.retain(12)
			.delete("s")
			.done()
		);
		
		e2.apply(StringDelta.builder()
			.retain(11)
			.insert("!")
			.retain(1)
			.done()
		);
		sync.resume();
		
		sync.waitForEmpty();
		
		assertThat(e1.getCurrent(), is(StringDelta.builder()
			.insert("Hello Cookie!!")
			.done()
		));
		
		assertThat(e2.getCurrent(), is(StringDelta.builder()
			.insert("Hello Cookie!!")
			.done()
		));
	}
}
