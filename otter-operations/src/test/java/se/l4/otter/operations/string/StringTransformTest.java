package se.l4.otter.operations.string;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;

public class StringTransformTest
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
			insert(20, 2, "abc"),
			insert(20, 2, "123"),
			insert(23, 2, "abc"),
			insert(23, 5, "123")
		);
	}
	
	@Test
	public void testInsertionWhenEmpty()
	{
		test(
			insert(0, 0, "a"),
			insert(0, 0, "b"),
			insert(1, 0, "a"),
			insert(1, 1, "b")
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
	public void testOther1()
	{
		reversibleTest(
		    delete(20, 1, "abcde"),
		    delete(20, 7, "fg"),
		    delete(18, 1, "abcde"),
		    delete(15, 2, "fg")
		);
	}
	
	@Test
	public void testOther2()
	{
		reversibleTest(
			delete(20, 1, "abcde"),
			delete(20, 6, "fg"),
			delete(18, 1, "abcde"),
			delete(15, 1, "fg")
		);
	}
	
	@Test
	public void testOther3()
	{
		// A's deletion overlaps B's deletion
		reversibleTest(
			delete(20, 1, "abcde"),
			delete(20, 3, "cdefghi"),
			delete(13, 1, "ab"),
			delete(15, 1, "fghi")
		);
	}
	
	@Test
	public void testOther4()
	{
		// A's deletion a subset of B's deletion
		reversibleTest(
			delete(20, 1, "abcdefg"),
			delete(20, 3, "cd"),
			delete(18, 1, "abefg"),
			retain(13)
		);
	}
	
	@Test
	public void testOther5()
	{
		// A's deletion identical to B's deletion
		reversibleTest(
			delete(20, 1, "abcdefg"),
			delete(20, 1, "abcdefg"),
			retain(13),
			retain(13)
		);
	}
	
	@Test
	public void testMix1()
	{
		reversibleTest(
			StringDelta.builder()
				.retain(6)
				.delete("World")
				.insert("Cookies")
				.done(),
				
			StringDelta.builder()
				.retain(11)
				.insert("!")
				.done(),
				
			StringDelta.builder()
				.retain(6)
				.delete("World")
				.insert("Cookies")
				.retain(1)
				.done(),
				
			StringDelta.builder()
				.retain(13)
				.insert("!")
				.done()
		);
	}

	private static Operation<StringHandler> retain(int i)
	{
		return StringDelta.builder()
			.retain(i)
			.done();
	}

	public static Operation<StringHandler> insert(int size, int location, String characters)
	{
		return StringDelta.builder()
			.retain(location)
			.insert(characters)
			.retain(size-location)
			.done();
	}
	
	public static Operation<StringHandler> delete(int size, int location, String characters)
	{
		return StringDelta.builder()
			.retain(location)
			.delete(characters)
			.retain(size-location-characters.length())
			.done();
	}
	
	private void test(Operation<StringHandler> left, Operation<StringHandler> right,
			Operation<StringHandler> expectedLeft, Operation<StringHandler> expectedRight)
	{
		StringType type = new StringType();
		
		OperationPair<Operation<StringHandler>> op = type.transform(left, right);
		assertThat("left", op.getLeft(), is(expectedLeft));
		assertThat("right", op.getRight(), is(expectedRight));
	}
	
	private void reversibleTest(Operation<StringHandler> client, Operation<StringHandler> server,
			Operation<StringHandler> expectedClient, Operation<StringHandler> expectedServer)
	{
		test(client, server, expectedClient, expectedServer);
		test(server, client, expectedServer, expectedClient);
	}
	
}
