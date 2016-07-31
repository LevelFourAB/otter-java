package se.l4.otter.operations.list;

import org.junit.Test;

import se.l4.commons.serialization.Serializer;
import se.l4.otter.data.DataArray;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.SerializerTestHelper;

public class ListSerializationTest
{
	@Test
	public void test1()
	{
		test(ListDelta.builder()
			.retain(2)
			.insert("Hey!")
			.delete(new DataArray())
			.done()
		);
	}
	
	@Test
	public void test2()
	{
		test(ListDelta.builder()
			.insert(null)
			.insert("one")
			.insert("two")
			.done()
		);
	}
	
	@Test
	public void test3()
	{
		test("[[\"retain\",2],[\"insert\",[\"Hey!\"]],[\"delete\",[[]]]]", ListDelta.builder()
			.retain(2)
			.insert("Hey!")
			.delete(new DataArray())
			.done()
		);
	}
	
	private void test(Operation<ListHandler> op)
	{
		ListType type = new ListType();
		Serializer<Operation<ListHandler>> serializer = type.getSerializer();
		SerializerTestHelper.testSymmetry(serializer, op);
	}
	
	private void test(String json, Operation<ListHandler> op)
	{
		ListType type = new ListType();
		Serializer<Operation<ListHandler>> serializer = type.getSerializer();
		SerializerTestHelper.testStatic(json, serializer, op);
	}
}
