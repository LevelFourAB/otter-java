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

	@Test
	public void testAnnotationRetain1()
	{
		reversibleTest(
			setAnnotation(20, 4, 6, "hello", null, "world"),
			retain(20),
			setAnnotation(20, 4, 6, "hello", null, "world"),
			retain(20)
		);
	}

	@Test
	public void testAnnotationDelete1()
	{
		// A's annotation spatially before B's deletion
		reversibleTest(
			setAnnotation(20, 4, 6, "hello", null, "world"),
			delete(20, 7, "ab"),
			setAnnotation(18, 4, 6, "hello", null, "world"),
			delete(20, 7, "ab")
		);
	}

	@Test
	public void testAnnotationDelete2()
	{
		// A's annotation spatially after B's deletion
		reversibleTest(
			setAnnotation(20, 4, 6, "hello", null, "world"),
			delete(20, 1, "ab"),
			setAnnotation(18, 2, 4, "hello", null, "world"),
			delete(20, 1, "ab")
		);
	}

	@Test
	public void testAnnotationDelete3()
	{
		// A's annotation spatially adjacent to and before B's deletion
		reversibleTest(
			setAnnotation(20, 4, 6, "hello", null, "world"),
			delete(20, 6, "abc"),
			setAnnotation(17, 4, 6, "hello", null, "world"),
			delete(20, 6, "abc")
		);
	}

	@Test
	public void testAnnotationDelete4()
	{
		// A's annotation spatially adjacent to and after B's deletion
		reversibleTest(
			setAnnotation(20, 4, 6, "hello", null, "world"),
			delete(20, 1, "abc"),
			setAnnotation(17, 1, 3, "hello", null, "world"),
			delete(20, 1, "abc")
		);
	}

	@Test
	public void testAnnotationDelete5()
	{
		// A's annotation overlaps B's deletion
		reversibleTest(
			setAnnotation(20, 4, 6, "hello", null, "world"),
			delete(20, 1, "abcd"),
			setAnnotation(16, 1, 2, "hello", null, "world"),
			delete(20, 1, "abcd")
		);
	}

	@Test
	public void testAnnotationDelete6()
	{
		// A's annotation fully inside B's deletion
		reversibleTest(
			setAnnotation(20, 2, 3, "hello", null, "world"),
			delete(20, 1, "abcd"),
			retain(16),
			delete(20, 1, "abcd")
		);
	}

	@Test
	public void testAnnotationInsert1()
	{
		// A's annotation spatially after B's insertion
		reversibleTest(
			setAnnotation(20, 3, 5, "hello", null, "world"),
			insert(20, 2, "abcd"),
			setAnnotation(24, 7, 9, "hello", null, "world"),
			insert(20, 2, "abcd")
		);
	}

	@Test
	public void testAnnotationInsert2()
	{
		// A's annotation encloses B's insertion
		reversibleTest(
			setAnnotation(20, 3, 5, "hello", null, "world"),
			insert(20, 4, "abcd"),
			setAnnotation(24, 3, 9, "hello", null, "world"),
			insert(20, 4, "abcd")
		);
	}

	@Test
	public void testAnnotationInsert3()
	{
		// A's annotation spatially adjacent to and after B's insertion
		reversibleTest(
			setAnnotation(20, 3, 5, "hello", null, "world"),
			insert(20, 3, "abcd"),
			setAnnotation(24, 3, 9, "hello", null, "world"),
			insert(20, 3, "abcd")
		);
	}

	@Test
	public void testAnnotationInsert4()
	{
		// A's annotation spatially adjacent to and before B's insertion
		reversibleTest(
			setAnnotation(20, 3, 5, "hello", null, "world"),
			insert(20, 5, "abcd"),
			setAnnotation(24, 3, 5, "hello", null, "world"),
			insert(20, 5, "abcd")
		);
	}

	@Test
	public void testAnnotationAnnotation1()
	{
		// A's annotation overlaps B's annotation and has different key
		reversibleTest(
			setAnnotation(20, 2, 6, "hello", "initial", "world"),
			setAnnotation(20, 5, 9, "hi", "initial", "there"),
			setAnnotation(20, 2, 6, "hello", "initial", "world"),
			setAnnotation(20, 5, 9, "hi", "initial", "there")
		);
	}

	@Test
	public void testAnnotationAnnotation2()
	{
		// A's annotation overlaps B's annotation and has different key
		reversibleTest(
			setAnnotation(20, 2, 9, "hello", "initial", "world"),
			setAnnotation(20, 5, 7, "hi", "initial", "there"),
			setAnnotation(20, 2, 9, "hello", "initial", "world"),
			setAnnotation(20, 5, 7, "hi", "initial", "there")
		);
	}

	@Test
	public void testAnnotationAnnotation3()
	{
		// A's annotation spatially before B's annotation
		reversibleTest(
			setAnnotation(20, 2, 5, "hello", "initial", "world"),
			setAnnotation(20, 6, 9, "hello", "initial", "there"),
			setAnnotation(20, 2, 5, "hello", "initial", "world"),
			setAnnotation(20, 6, 9, "hello", "initial", "there")
		);
	}

	@Test
	public void testAnnotationAnnotation4()
	{
		// A's annotation spatially adjacent to and before B's annotation
		reversibleTest(
			setAnnotation(20, 2, 5, "hello", "initial", "world"),
			setAnnotation(20, 5, 9, "hello", "initial", "there"),
			setAnnotation(20, 2, 5, "hello", "initial", "world"),
			setAnnotation(20, 5, 9, "hello", "initial", "there")
		);
	}

	@Test
	public void testAnnotationAnnotation5()
	{
		// Annotations overlap
		reversibleTest(
			setAnnotation(20, 2, 6, "hello", "initial", "world"),
			setAnnotation(20, 5, 9, "hello", "initial", "there"),
			setAnnotation(20, 2, 6, "hello", "initial", "world"),
			setAnnotation(20, 5, 9, "hello", "initial",  "there")
		);
	}

	@Test
	public void testAnnotationAnnotation6()
	{
		// A's annotation is within B's
		reversibleTest(
			setAnnotation(20, 5, 7, "hello", "initial", "world"),
			setAnnotation(20, 2, 9, "hello", "initial", "there"),
			setAnnotation(20, 5, 7, "hello", "initial", "world"),
			setAnnotation(20, 2, 9, "hello", "initial",  "there")
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

	private static Operation<StringHandler> setAnnotation(int size, int start, int end, String key, String oldValue, String newValue)
	{
		StringDelta<Operation<StringHandler>> delta = StringDelta.builder()
			.retain(start);

		if(newValue == null)
		{
			delta.updateAnnotations()
				.remove(key, oldValue)
				.done();
		}
		else
		{
			delta.updateAnnotations()
				.set(key, oldValue, newValue)
				.done();
		}

		delta.retain(end - start);

		if(newValue != null)
		{
			delta.updateAnnotations()
				.remove(key, newValue)
				.done();
		}

		return delta.retain(size - end)
			.done();
	}

	private void test(Operation<StringHandler> left, Operation<StringHandler> right,
			Operation<StringHandler> expectedLeft, Operation<StringHandler> expectedRight)
	{
		StringType type = new StringType();

		// Sanity check the test - needs to be composable
		Operation<StringHandler> cLeft = type.compose(left, expectedRight);
		Operation<StringHandler> rLeft = type.compose(right, expectedLeft);
		assertThat("composed", cLeft, is(rLeft));

		OperationPair<Operation<StringHandler>> op = type.transform(left, right);

		// Then check that the values are what we expect
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
