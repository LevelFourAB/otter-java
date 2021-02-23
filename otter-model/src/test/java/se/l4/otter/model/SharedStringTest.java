package se.l4.otter.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.l4.otter.engine.LocalOperationSync;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedHandler;

public class SharedStringTest
{
	private LocalOperationSync<Operation<CombinedHandler>> sync;

	@Before
	public void before()
	{
		sync = ModelTestHelper.createSync();
	}

	@After
	public void close()
		throws IOException
	{
		sync.close();
	}

	public Model model()
	{
		return ModelTestHelper.createModel(sync);
	}

	@Test
	public void testInit()
	{
		Model m = model();

		SharedString string = m.newString();
		assertThat(string, notNullValue());
	}


	/**
	 * Test that several concurrent appends resolve to the same string value.
	 */
	@Test
	public void testConcurrentAppend()
	{
		Model m1 = model();
		Model m2 = model();

		SharedString string1 = m1.newString();
		m1.set("string", string1);

		sync.waitForEmpty();

		SharedString string2 = m2.get("string");

		sync.suspend();

		string1.append("a");
		string2.append("b");

		sync.resume();

		sync.waitForEmpty();

		assertThat(string1.get(), is(string2.get()));
	}
}
