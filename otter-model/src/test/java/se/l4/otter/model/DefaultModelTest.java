package se.l4.otter.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.l4.otter.engine.DefaultEditor;
import se.l4.otter.engine.DefaultEditorControl;
import se.l4.otter.engine.Editor;
import se.l4.otter.engine.InMemoryOperationHistory;
import se.l4.otter.engine.LocalOperationSync;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedDelta;
import se.l4.otter.operations.combined.CombinedHandler;
import se.l4.otter.operations.combined.CombinedType;
import se.l4.otter.operations.combined.CombinedTypeBuilder;

public class DefaultModelTest
{
	private static final CombinedType TYPE = new CombinedTypeBuilder()
		.build();

	private DefaultEditorControl<Operation<CombinedHandler>> control;
	private LocalOperationSync<Operation<CombinedHandler>> sync;

	@Before
	public void setupEditor()
	{
		control = new DefaultEditorControl<>(
			new InMemoryOperationHistory<>(TYPE, CombinedDelta.builder().done()
		));
		sync = new LocalOperationSync<>(control);
	}

	@After
	public void close()
		throws IOException
	{
		sync.close();
	}

	private Editor<Operation<CombinedHandler>> editor()
	{
		return new DefaultEditor<>(sync);
	}

	private Model model()
	{
		return Model.builder(editor()).build();
	}

	@Test
	public void testRootMap()
	{
		Model m1 = model();

		sync.suspend();
		m1.set("key", "value");

		assertThat("m1 before sync", m1.get("key"), is("value"));

		sync.resume();
		sync.waitForEmpty();

		assertThat("m1 after sync", m1.get("key"), is("value"));

		Model m2 = model();
		assertThat("m2", m2.get("key"), is("value"));
	}

	@Test
	public void testRootMapEvents()
	{
		Model m1 = model();
		Model m2 = model();

		AtomicReference<Object> b = new AtomicReference<>();
		m2.addChangeListener(new SharedMap.Listener() {
			@Override
			public void valueRemoved(String key, Object oldValue)
			{
			}

			@Override
			public void valueChanged(String key, Object oldValue, Object newValue)
			{
				b.set(newValue);
			}
		});

		m1.set("key", "value");
		sync.waitForEmpty();

		assertThat("set value", m2.get("key"), is("value"));
		assertThat("event value", b.get(), is("value"));
	}

	@Test
	public void testNewString()
	{
		Model m1 = model();

		SharedString s1 = m1.newString();
		s1.set("Hello World");
		m1.set("string", s1);

		sync.waitForEmpty();

		Model m2 = model();
		SharedString s2 = m2.get("string");
		assertThat(s2.get(), is("Hello World"));

		s1.set("Hello Cookies");
		sync.waitForEmpty();
		assertThat(s2.get(), is("Hello Cookies"));
	}
}
