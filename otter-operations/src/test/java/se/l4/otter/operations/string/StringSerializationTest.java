package se.l4.otter.operations.string;

import org.junit.Test;

import se.l4.exobytes.Serializer;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.SerializerTestHelper;

public class StringSerializationTest
{
	@Test
	public void test1()
	{
		test(StringDelta.builder()
			.retain(2)
			.insert("Hello World")
			.delete("Cookie")
			.done()
		);
	}

	@Test
	public void test2()
	{
		test(StringDelta.builder()
			.retain(2)
			.updateAnnotations()
				.set("key", null, true)
				.done()
			.insert("Hello World")
			.delete("Cookie")
			.updateAnnotations()
				.remove("key", true)
				.done()
			.done()
		);
	}

	@Test
	public void test3()
	{
		test("[[\"retain\",2],[\"annotations\",{\"key\":{\"oldValue\":null,\"newValue\":true}}],[\"insert\",\"Hello World\"],[\"delete\",\"Cookie\"]]", StringDelta.builder()
			.retain(2)
			.updateAnnotations()
				.set("key", null, true)
				.done()
			.insert("Hello World")
			.delete("Cookie")
			.done()
		);
	}

	private void test(Operation<StringHandler> op)
	{
		StringType type = new StringType();
		Serializer<Operation<StringHandler>> serializer = type.getSerializer();
		SerializerTestHelper.testSymmetry(serializer, op);
	}

	private void test(String json, Operation<StringHandler> op)
	{
		StringType type = new StringType();
		Serializer<Operation<StringHandler>> serializer = type.getSerializer();
		SerializerTestHelper.testStatic(json, serializer, op);
	}
}
