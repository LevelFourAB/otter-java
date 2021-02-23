package se.l4.otter.operations.list;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import se.l4.otter.operations.Operation;

public class ListComposeTest
{
	@Test
	public void testCompose1()
	{
		Operation<ListHandler> op1 = ListDelta.builder()
			.insert("one")
			.insert("two")
			.done();

		Operation<ListHandler> op2 = ListDelta.builder()
			.retain(1)
			.delete("two")
			.insert("three")
			.done();

		Operation<ListHandler> r = compose(op1, op2);

		assertThat(r, is(ListDelta.builder()
			.insert("one")
			.insert("three")
			.done())
		);
	}

	@Test
	public void testCompose2()
	{
		Operation<ListHandler> op1 = ListDelta.builder()
			.retain(1)
			.insert("one")
			.done();

		Operation<ListHandler> op2 = ListDelta.builder()
			.retain(1)
			.delete("one")
			.insert("three")
			.done();

		Operation<ListHandler> r = compose(op1, op2);

		assertThat(r, is(ListDelta.builder()
			.retain(1)
			.insert("three")
			.done())
		);
	}

	@Test
	public void testCompose3()
	{
		Operation<ListHandler> op1 = ListDelta.builder()
			.insert("one")
			.done();

		Operation<ListHandler> op2 = ListDelta.builder()
			.delete("one")
			.done();

		Operation<ListHandler> r = compose(op1, op2);

		assertThat(r, is(ListDelta.builder()
			.done())
		);
	}

	@Test
	public void testCompose4()
	{
		Operation<ListHandler> op1 = ListDelta.builder()
			.delete("one")
			.done();

		Operation<ListHandler> op2 = ListDelta.builder()
			.insert("one")
			.done();

		Operation<ListHandler> r = compose(op1, op2);

		assertThat(r, is(ListDelta.builder()
			.delete("one")
			.insert("one")
			.done())
		);
	}


	@Test
	public void testCompose6()
	{
		Operation<ListHandler> op1 = ListDelta.builder()
			.retain(1)
			.delete("one")
			.done();

		Operation<ListHandler> op2 = ListDelta.builder()
			.insert("one")
			.retain(1)
			.done();

		Operation<ListHandler> r = compose(op1, op2);

		assertThat(r, is(ListDelta.builder()
			.insert("one")
			.retain(1)
			.delete("one")
			.done())
		);
	}

	@Test
	public void testCompose7()
	{
		Operation<ListHandler> op1 = ListDelta.builder()
			.retain(4)
			.insert("one")
			.done();

		Operation<ListHandler> op2 = ListDelta.builder()
			.retain(2)
			.delete("one")
			.retain(2)
			.done();

		Operation<ListHandler> r = compose(op1, op2);

		assertThat(r, is(ListDelta.builder()
			.retain(2)
			.delete("one")
			.retain(1)
			.insert("one")
			.done())
		);
	}

	@Test
	public void testCompose8()
	{
		Operation<ListHandler> op1 = ListDelta.builder()
			.retain(1)
			.delete("a")
			.retain(1)
			.done();

		Operation<ListHandler> op2 = ListDelta.builder()
			.insert("a")
			.retain(2)
			.done();

		Operation<ListHandler> r = compose(op1, op2);

		assertThat(r, is(ListDelta.builder()
			.insert("a")
			.retain(1)
			.delete("a")
			.retain(1)
			.done())
		);
	}

	private Operation<ListHandler> compose(Operation<ListHandler> op1,
			Operation<ListHandler> op2)
	{
		ListType helper = new ListType();
		return helper.compose(op1, op2);
	}
}
