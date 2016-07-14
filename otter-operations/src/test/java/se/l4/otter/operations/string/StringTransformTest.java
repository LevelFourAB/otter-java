package se.l4.otter.operations.string;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;

public class StringTransformTest
{
	@Test
	public void testInsertionBeforeServer()
	{
		reversibleTest(
			insert(20, 1, "a"),
			insert(20, 2, "1"),
			insert(21, 1, "a"),
			insert(21, 3, "1")
		);
	}
	
	@Test
	public void testInsertionAtSameLocationAsServer()
	{
		test(
			insert(20, 2, "abc"),
			insert(20, 2, "123"),
			insert(23, 2, "abc"),
			insert(23, 5, "123")
		);
	}
	
	@Test
	public void testDelete()
	{
		reversibleTest(
			insert(20, 1, "abc"),
	        delete(20, 2, "de"),
	        insert(18, 1, "abc"),
	        delete(23, 5, "de")
		);
	}
	
	@Test
	public void testOther()
	{
		reversibleTest(
		    delete(20, 1, "abcde"),
		    delete(20, 7, "fg"),
		    delete(18, 1, "abcde"),
		    delete(15, 2, "fg")
		);
		
		reversibleTest(
			delete(20, 1, "abcde"),
			delete(20, 6, "fg"),
			delete(18, 1, "abcde"),
			delete(15, 1, "fg")
		);

		// A's deletion overlaps B's deletion
		reversibleTest(
			delete(20, 1, "abcde"),
			delete(20, 3, "cdefghi"),
			delete(13, 1, "ab"),
			delete(15, 1, "fghi")
		);
		
		// A's deletion a subset of B's deletion
		reversibleTest(
			delete(20, 1, "abcdefg"),
			delete(20, 3, "cd"),
			delete(18, 1, "abefg"),
			retain(13)
		);
		
		// A's deletion identical to B's deletion
		reversibleTest(
			delete(20, 1, "abcdefg"),
			delete(20, 1, "abcdefg"),
			retain(13),
			retain(13)
		);
	}

	private static Operation<StringOperationHandler> retain(int i)
	{
		return StringDelta.builder()
			.retain(i)
			.done();
	}

	public static Operation<StringOperationHandler> insert(int size, int location, String characters)
	{
		return StringDelta.builder()
			.retain(location)
			.insert(characters)
			.retain(size-location)
			.done();
	}
	
	public static Operation<StringOperationHandler> delete(int size, int location, String characters)
	{
		return StringDelta.builder()
			.retain(location)
			.delete(characters)
			.retain(size-location-characters.length())
			.done();
	}
	
	private void test(Operation<StringOperationHandler> client, Operation<StringOperationHandler> server,
			Operation<StringOperationHandler> expectedClient, Operation<StringOperationHandler> expectedServer)
	{
		StringType type = new StringType();
		OperationPair<Operation<StringOperationHandler>> op = type.transform(client, server);
		assertThat("client", op.getFirst(), is(expectedClient));
		assertThat("server", op.getSecond(), is(expectedServer));
	}
	
	private void reversibleTest(Operation<StringOperationHandler> client, Operation<StringOperationHandler> server,
			Operation<StringOperationHandler> expectedClient, Operation<StringOperationHandler> expectedServer)
	{
		test(client, server, expectedClient, expectedServer);
		test(server, client, expectedServer, expectedClient);
	}
	
}
