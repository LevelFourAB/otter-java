package se.l4.otter.operations.list;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;

public class ListTransformTest
{
	@Test
	public void testInsertionBeforeRight()
	{
		reversibleTest(
			insert(20, 1, "a"),
			insert(20, 2, "1"),
			insert(21, 1, "a"),
			insert(21, 3, "1")
		);
	}

	@Test
	public void testInsertionAtSameLocation()
	{
		test(
			insert(20, 2, "a", "b", "c"),
			insert(20, 2, "1", "2", "3"),
			insert(23, 2, "a", "b", "c"),
			insert(23, 5, "1", "2", "3")
		);
	}

	@Test
	public void testDelete()
	{
		reversibleTest(
			insert(20, 1, "a", "b", "c"),
	        delete(20, 2, "d", "e"),
	        insert(18, 1, "a", "b", "c"),
	        delete(23, 5, "d", "e")
		);
	}

	@Test
	public void testOther1()
	{
		reversibleTest(
		    delete(20, 1, "a", "b", "c", "d", "e"),
		    delete(20, 7, "f", "g"),
		    delete(18, 1, "a", "b", "c", "d", "e"),
		    delete(15, 2, "f", "g")
		);
	}

	@Test
	public void testOther2()
	{
		reversibleTest(
			delete(20, 1, "a", "b", "c", "d", "e"),
			delete(20, 6, "f", "g"),
			delete(18, 1, "a", "b", "c", "d", "e"),
			delete(15, 1, "f", "g")
		);
	}

	@Test
	public void testOther3()
	{
		// A's deletion overlaps B's deletion
		reversibleTest(
			delete(20, 1, "a", "b", "c", "d", "e"),
			delete(20, 3, "c", "d", "e", "f", "g", "h", "i"),
			delete(13, 1, "a", "b"),
			delete(15, 1, "f", "g", "h", "i")
		);
	}

	@Test
	public void testOther4()
	{
		// A's deletion a subset of B's deletion
		reversibleTest(
			delete(20, 1, "a", "b", "c", "d", "e", "f", "g"),
			delete(20, 3, "c", "d"),
			delete(18, 1, "a", "b", "e", "f", "g"),
			retain(13)
		);
	}

	@Test
	public void testOther5()
	{
		// A's deletion identical to B's deletion
		reversibleTest(
			delete(20, 1, "a", "b", "c", "d", "e", "f", "g"),
			delete(20, 1, "a", "b", "c", "d", "e", "f", "g"),
			retain(13),
			retain(13)
		);
	}

	private static Operation<ListHandler> retain(int i)
	{
		return ListDelta.builder()
			.retain(i)
			.done();
	}

	public static Operation<ListHandler> insert(int size, int location, Object... values)
	{
		return ListDelta.builder()
			.retain(location)
			.insertMultiple(values)
			.retain(size-location)
			.done();
	}

	public static Operation<ListHandler> delete(int size, int location, Object... values)
	{
		return ListDelta.builder()
			.retain(location)
			.deleteMultiple(values)
			.retain(size-location-values.length)
			.done();
	}

	private void test(Operation<?> client, Operation<?> server,
			Operation<?> expectedClient, Operation<?> expectedServer)
	{
		ListType type = new ListType();
		OperationPair op = type.transform((Operation) client, (Operation) server);
		assertThat("left", op.getLeft(), is(expectedClient));
		assertThat("right", op.getRight(), is(expectedServer));
	}

	private void reversibleTest(Operation<?> client, Operation<?> server,
			Operation<?> expectedClient, Operation<?> expectedServer)
	{
		test(client, server, expectedClient, expectedServer);
		test(server, client, expectedServer, expectedClient);
	}

}
