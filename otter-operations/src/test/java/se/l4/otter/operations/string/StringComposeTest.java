package se.l4.otter.operations.string;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import se.l4.otter.operations.ComposeException;
import se.l4.otter.operations.Operation;

public class StringComposeTest
{
	@Test
	public void testCompose1()
	{
		Operation<StringOperationHandler> op1 = StringDelta.builder()
			.insert("Hello World")
			.done();
		
		Operation<StringOperationHandler> op2 = StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.done();
		
		Operation<StringOperationHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.insert("Hello Cookies")
			.done())
		);
	}

	@Test
	public void testCompose2()
	{
		Operation<StringOperationHandler> op1 = StringDelta.builder()
			.retain(6)
			.insert("World")
			.done();
		
		Operation<StringOperationHandler> op2 = StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.done();
		
		Operation<StringOperationHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.retain(6)
			.insert("Cookies")
			.done())
		);
	}
	
	@Test
	public void testCompose3()
	{
		Operation<StringOperationHandler> op1 = StringDelta.builder()
			.retain(6)
			.insert("World")
			.done();
		
		Operation<StringOperationHandler> op2 = StringDelta.builder()
			.retain(6)
			.retain(1)
			.delete("orld")
			.insert(" ")
			.insert("Cookies")
			.done();
		
		Operation<StringOperationHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.retain(6)
			.insert("W Cookies")
			.done())
		);
	}
	

	@Test
	public void testCompose4()
	{
		Operation<StringOperationHandler> op1 = StringDelta.builder()
			.insert("Hello ")
			.retain(5)
			.done();
		
		Operation<StringOperationHandler> op2 = StringDelta.builder()
			.retain(11)
			.done();
		
		Operation<StringOperationHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.insert("Hello ")
			.retain(5)
			.done())
		);
	}
	
	@Test
	public void testCompose5()
	{
		Operation<StringOperationHandler> op1 = StringDelta.builder()
			.delete("Hello ")
			.retain(5)
			.done();
		
		Operation<StringOperationHandler> op2 = StringDelta.builder()
			.insert("Cookie ")
			.retain(5)
			.done();
		
		Operation<StringOperationHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.delete("Hello ")
			.insert("Cookie ")
			.retain(5)
			.done())
		);
	}
	
	@Test
	public void testCompose6()
	{
		Operation<StringOperationHandler> op1 = StringDelta.builder()
			.insert("Cookie ")
			.retain(5)
			.done();
		
		Operation<StringOperationHandler> op2 = StringDelta.builder()
			.delete("Hello ")
			.retain(5)
			.done();
		
		StringType helper = new StringType();
		
		try
		{
			Operation<StringOperationHandler> o = helper.compose(op1, op2);
			fail("Should not be composable, but got " + o);
		}
		catch(ComposeException e)
		{
		}
	}
	
	private Operation<StringOperationHandler> compose(Operation<StringOperationHandler> op1,
			Operation<StringOperationHandler> op2)
	{
		StringType helper = new StringType();
		return helper.compose(op1, op2);
	}
}
