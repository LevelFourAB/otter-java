package se.l4.otter.operations.map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import se.l4.otter.operations.Operation;

public class MapComposeTest
{
	@Test
	public void testSameKeyAndValue()
	{
		Operation<MapOperationHandler> op1 = MapDelta.builder()
			.set("one", null, "abc")
			.done();
		
		Operation<MapOperationHandler> op2 = MapDelta.builder()
			.set("one", null, "abc")
			.done();
		
		Operation<MapOperationHandler> r = compose(op1, op2);
		assertThat(r, is(MapDelta.builder()
			.set("one", null, "abc")
			.done()
		));
	}
	
	@Test
	public void testSameKey()
	{
		Operation<MapOperationHandler> op1 = MapDelta.builder()
			.set("one", null, "abc")
			.done();
		
		Operation<MapOperationHandler> op2 = MapDelta.builder()
			.set("one", "abc", "def")
			.done();
		
		Operation<MapOperationHandler> r = compose(op1, op2);
		assertThat(r, is(MapDelta.builder()
			.set("one", null, "def")
			.done()
		));
	}
	
	@Test
	public void testDifferentKeys1()
	{
		Operation<MapOperationHandler> op1 = MapDelta.builder()
			.set("one", null, "abc")
			.done();
		
		Operation<MapOperationHandler> op2 = MapDelta.builder()
			.set("two", null, "def")
			.done();
		
		Operation<MapOperationHandler> r = compose(op1, op2);
		assertThat(r, is(MapDelta.builder()
			.set("one", null, "abc")
			.set("two", null, "def")
			.done()
		));
	}
	
	@Test
	public void testDifferentKeys2()
	{
		Operation<MapOperationHandler> op1 = MapDelta.builder()
			.set("two", null, "def")
			.done();
		
		Operation<MapOperationHandler> op2 = MapDelta.builder()
			.set("one", null, "abc")
			.done();
		
		Operation<MapOperationHandler> r = compose(op1, op2);
		assertThat(r, is(MapDelta.builder()
			.set("one", null, "abc")
			.set("two", null, "def")
			.done()
		));
	}
	
	@Test
	public void testMultipleDifferentKeys1()
	{
		Operation<MapOperationHandler> op1 = MapDelta.builder()
			.set("one", null, "abc")
			.set("two", null, "def")
			.done();
		
		Operation<MapOperationHandler> op2 = MapDelta.builder()
			.set("one", "abc", "def")
			.done();
		
		Operation<MapOperationHandler> r = compose(op1, op2);
		assertThat(r, is(MapDelta.builder()
			.set("one", null, "def")
			.set("two", null, "def")
			.done()
		));
	}
	
	@Test
	public void testMultipleDifferentKeys2()
	{
		Operation<MapOperationHandler> op1 = MapDelta.builder()
			.set("two", null, "def")
			.set("one", null, "abc")
			.done();
		
		Operation<MapOperationHandler> op2 = MapDelta.builder()
			.set("one", "abc", "def")
			.done();
		
		Operation<MapOperationHandler> r = compose(op1, op2);
		assertThat(r, is(MapDelta.builder()
			.set("one", null, "def")
			.set("two", null, "def")
			.done()
		));
	}
	
	@Test
	public void testMultipleDifferentKeys3()
	{
		Operation<MapOperationHandler> op1 = MapDelta.builder()
			.set("one", null, "abc")
			.set("two", null, "def")
			.done();
		
		Operation<MapOperationHandler> op2 = MapDelta.builder()
			.set("two", "def", "ghi")
			.done();
		
		Operation<MapOperationHandler> r = compose(op1, op2);
		assertThat(r, is(MapDelta.builder()
			.set("one", null, "abc")
			.set("two", null, "ghi")
			.done()
		));
	}
	
	@Test
	public void testMultipleDifferentKeys4()
	{
		Operation<MapOperationHandler> op1 = MapDelta.builder()
			.set("one", null, "abc")
			.set("two", null, "def")
			.done();
		
		Operation<MapOperationHandler> op2 = MapDelta.builder()
			.set("two", "def", "ghi")
			.set("one", "abc", "def")
			.done();
		
		Operation<MapOperationHandler> r = compose(op1, op2);
		assertThat(r, is(MapDelta.builder()
			.set("one", null, "def")
			.set("two", null, "ghi")
			.done()
		));
	}
	
	@Test
	public void testMultipleSameKey()
	{
		Operation<MapOperationHandler> op1 = MapDelta.builder()
			.set("one", null, "abc")
			.done();
		
		Operation<MapOperationHandler> op2 = MapDelta.builder()
			.set("one", "abc", "def")
			.set("one", "def", "ghi")
			.done();
		
		Operation<MapOperationHandler> r = compose(op1, op2);
		assertThat(r, is(MapDelta.builder()
			.set("one", null, "ghi")
			.done()
		));
	}
	
	private Operation<MapOperationHandler> compose(Operation<MapOperationHandler> op1,
			Operation<MapOperationHandler> op2)
	{
		MapType helper = new MapType();
		return helper.compose(op1, op2);
	}
}
