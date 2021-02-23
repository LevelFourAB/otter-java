package se.l4.otter.operations.map;

import java.util.ArrayList;

import org.junit.Test;

import se.l4.exobytes.Serializer;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.SerializerTestHelper;

public class MapSerializationTest
{
	@Test
	public void test1()
	{
		test(MapDelta.builder()
			.set("abc", null, "Cookies")
			.done()
		);
	}

	@Test
	public void test2()
	{
		test(MapDelta.builder()
			.set("abc", "Cookies", null)
			.done()
		);
	}

	@Test
	public void test3()
	{
		test(MapDelta.builder()
			.set("abc", null, new ArrayList<>())
			.done()
		);
	}

	@Test
	public void test4()
	{
		test(MapDelta.builder()
			.set("abc", null, new ArrayList<>())
			.set("def", null, "Cookies")
			.done()
		);
	}

	@Test
	public void test5()
	{
		test("[[\"set\",{\"key\":\"abc\",\"newValue\":[]}],[\"set\",{\"key\":\"def\",\"newValue\":\"Cookies\"}]]", MapDelta.builder()
			.set("abc", null, new ArrayList<>())
			.set("def", null, "Cookies")
			.done()
		);
	}

	private void test(Operation<MapHandler> op)
	{
		MapType type = new MapType();
		Serializer<Operation<MapHandler>> serializer = type.getSerializer();
		SerializerTestHelper.testSymmetry(serializer, op);
	}

	private void test(String json, Operation<MapHandler> op)
	{
		MapType type = new MapType();
		Serializer<Operation<MapHandler>> serializer = type.getSerializer();
		SerializerTestHelper.testStatic(json, serializer, op);
	}
}
