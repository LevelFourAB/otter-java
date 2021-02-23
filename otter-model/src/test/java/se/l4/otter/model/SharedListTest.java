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

public class SharedListTest
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

		SharedList<Object> list = m.newList();
		assertThat(list, notNullValue());
	}

	/**
	 * Test that an add is correctly applied on two separate open models.
	 */
	@Test
	public void testAdd1()
	{
		Model m1 = model();
		Model m2 = model();

		SharedList<Object> list1 = m1.newList();
		m1.set("list", list1);

		sync.waitForEmpty();

		SharedList<Object> list2 = m2.get("list");

		list1.add("test");

		sync.waitForEmpty();

		assertThat(list1.length(), is(1));
		assertThat(list1.get(0), is("test"));

		assertThat(list2.length(), is(1));
		assertThat(list2.get(0), is("test"));
	}

	/**
	 * Test that an add is correctly picked up when a new model is opened.
	 */
	@Test
	public void testAdd2()
	{
		Model m1 = model();

		SharedList<Object> list1 = m1.newList();
		m1.set("list", list1);

		list1.add("test");
		assertThat(list1.length(), is(1));
		assertThat(list1.get(0), is("test"));

		sync.waitForEmpty();

		Model m2 = model();
		SharedList<Object> list2 = m2.get("list");
		assertThat(list2.length(), is(1));
		assertThat(list2.get(0), is("test"));
	}

	/**
	 * Test that several concurrent adds resolve to the same values in two
	 * separate lists.
	 */
	@Test
	public void testAdd3()
	{
		Model m1 = model();
		Model m2 = model();

		SharedList<Object> list1 = m1.newList();
		m1.set("list", list1);

		sync.waitForEmpty();

		SharedList<Object> list2 = m2.get("list");

		sync.suspend();

		list1.add("one");
		assertThat(list1.length(), is(1));
		assertThat(list1.get(0), is("one"));

		list2.add("two");
		assertThat(list2.length(), is(1));
		assertThat(list2.get(0), is("two"));

		sync.resume();

		sync.waitForEmpty();

		assertThat(list1.length(), is(2));
		assertThat(list2.length(), is(2));

		assertThat(list1.get(0), is(list2.get(0)));
		assertThat(list1.get(1), is(list2.get(1)));
	}

	@Test
	public void testRemove1()
	{
		Model m1 = model();
		Model m2 = model();

		SharedList<Object> list1 = m1.newList();
		m1.set("list", list1);

		sync.waitForEmpty();

		SharedList<Object> list2 = m2.get("list");

		list1.add("test");

		sync.waitForEmpty();

		list2.remove(0);

		sync.waitForEmpty();

		assertThat(list1.length(), is(0));
		assertThat(list2.length(), is(0));
	}

	@Test
	public void testRemove2()
	{
		Model m1 = model();
		Model m2 = model();

		SharedList<Object> list1 = m1.newList();
		m1.set("list", list1);

		sync.waitForEmpty();

		SharedList<Object> list2 = m2.get("list");

		list1.add("one");
		list1.add("two");
		list1.add("three");

		sync.waitForEmpty();

		list2.remove(1);

		sync.waitForEmpty();

		assertThat(list1.length(), is(2));
		assertThat(list2.length(), is(2));

		assertThat(list1.get(0), is("one"));
		assertThat(list1.get(1), is("three"));
	}
}
