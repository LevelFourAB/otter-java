package se.l4.otter.engine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import se.l4.otter.lock.CloseableLock;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringHandler;
import se.l4.otter.operations.string.StringType;

public class DefaultEditorTest
{
	private static final StringType TYPE = new StringType();

	private DefaultEditorControl<Operation<StringHandler>> control;
	private LocalOperationSync<Operation<StringHandler>> sync;

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

	private Editor<Operation<StringHandler>> editor()
	{
		return new DefaultEditor<>(sync);
	}

	@Test
	public void test1()
	{
		Editor<Operation<StringHandler>> editor = editor();

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
		Editor<Operation<StringHandler>> e1 = editor();
		Editor<Operation<StringHandler>> e2 = editor();

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
		Editor<Operation<StringHandler>> e1 = editor();
		Editor<Operation<StringHandler>> e2 = editor();

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
		Editor<Operation<StringHandler>> e1 = editor();
		Editor<Operation<StringHandler>> e2 = editor();

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
		Editor<Operation<StringHandler>> e1 = editor();
		Editor<Operation<StringHandler>> e2 = editor();

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

	@Test
	public void testMultiple5()
	{
		Editor<Operation<StringHandler>> e1 = editor();
		Editor<Operation<StringHandler>> e2 = editor();

		sync.suspend();
		e1.apply(StringDelta.builder()
			.insert("a")
			.retain(11)
			.done()
		);

		e2.apply(StringDelta.builder()
			.insert("b")
			.retain(11)
			.done()
		);

		sync.resume();

		sync.waitForEmpty();

		assertThat(e1.getCurrent(), is(StringDelta.builder()
			.insert("abHello World")
			.done()
		));

		assertThat(e2.getCurrent(), is(StringDelta.builder()
			.insert("abHello World")
			.done()
		));
	}

	@Test
	public void testMultiple6()
	{
		Editor<Operation<StringHandler>> e1 = editor();
		Editor<Operation<StringHandler>> e2 = editor();

		sync.suspend();
		e1.apply(StringDelta.builder()
			.insert("a")
			.retain(11)
			.done()
		);

		e2.apply(StringDelta.builder()
			.insert("b")
			.retain(11)
			.done()
		);

		e2.apply(StringDelta.builder()
			.retain(1)
			.insert("c")
			.retain(11)
			.done()
		);

		sync.resume();

		sync.waitForEmpty();

		assertThat(e1.getCurrent(), is(StringDelta.builder()
			.insert("abcHello World")
			.done()
		));

		assertThat(e2.getCurrent(), is(StringDelta.builder()
			.insert("abcHello World")
			.done()
		));
	}

	@Test
	public void testMultiple7()
	{
		Editor<Operation<StringHandler>> e1 = editor();
		Editor<Operation<StringHandler>> e2 = editor();

		sync.suspend();
		e1.apply(StringDelta.builder()
			.insert("a")
			.retain(11)
			.done()
		);

		e2.apply(StringDelta.builder()
			.insert("b")
			.retain(11)
			.done()
		);

		e2.apply(StringDelta.builder()
			.retain(1)
			.insert("c")
			.retain(11)
			.done()
		);

		e1.apply(StringDelta.builder()
			.retain(1)
			.insert("d")
			.retain(11)
			.done()
		);

		sync.resume();

		sync.waitForEmpty();

		assertThat(e1.getCurrent(), is(StringDelta.builder()
			.insert("abdcHello World")
			.done()
		));

		assertThat(e2.getCurrent(), is(StringDelta.builder()
			.insert("abdcHello World")
			.done()
		));
	}

	@Test
	public void testLock1()
	{
		Editor<Operation<StringHandler>> e1 = editor();
		Editor<Operation<StringHandler>> e2 = editor();

		try(CloseableLock lock = e1.lock())
		{
			e1.apply(StringDelta.builder()
				.retain(6)
				.delete("World")
				.insert("Cookies")
				.done()
			);

			// This should lock everything if e1 would actually send the event
			sync.waitForEmpty();

			assertThat(e2.getCurrent(), is(StringDelta.builder()
				.insert("Hello World")
				.done()
			));
		}

		sync.waitForEmpty();

		assertThat(e2.getCurrent(), is(StringDelta.builder()
			.insert("Hello Cookies")
			.done()
		));
	}
}
