package se.l4.otter.operations.string;

import org.junit.Test;

import se.l4.commons.serialization.Serializer;
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
		test("\"__2;++'Hello World';--'Cookie';\"", StringDelta.builder()
			.retain(2)
			.insert("Hello World")
			.delete("Cookie")
			.done()
		);
	}
	
	private void test(Operation<StringOperationHandler> op)
	{
		StringType type = new StringType();
		Serializer<Operation<StringOperationHandler>> serializer = type.getSerializer();
		SerializerTestHelper.testSymmetry(serializer, op);
	}
	
	private void test(String json, Operation<StringOperationHandler> op)
	{
		StringType type = new StringType();
		Serializer<Operation<StringOperationHandler>> serializer = type.getSerializer();
		SerializerTestHelper.testStatic(json, serializer, op);
	}
}
