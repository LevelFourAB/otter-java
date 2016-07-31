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
		Operation<StringHandler> op1 = StringDelta.builder()
			.insert("Hello World")
			.done();
		
		Operation<StringHandler> op2 = StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.done();
		
		Operation<StringHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.insert("Hello Cookies")
			.done())
		);
	}

	@Test
	public void testCompose2()
	{
		Operation<StringHandler> op1 = StringDelta.builder()
			.retain(6)
			.insert("World")
			.done();
		
		Operation<StringHandler> op2 = StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.done();
		
		Operation<StringHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.retain(6)
			.insert("Cookies")
			.done())
		);
	}
	
	@Test
	public void testCompose3()
	{
		Operation<StringHandler> op1 = StringDelta.builder()
			.retain(6)
			.insert("World")
			.done();
		
		Operation<StringHandler> op2 = StringDelta.builder()
			.retain(6)
			.retain(1)
			.delete("orld")
			.insert(" ")
			.insert("Cookies")
			.done();
		
		Operation<StringHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.retain(6)
			.insert("W Cookies")
			.done())
		);
	}
	

	@Test
	public void testCompose4()
	{
		Operation<StringHandler> op1 = StringDelta.builder()
			.insert("Hello ")
			.retain(5)
			.done();
		
		Operation<StringHandler> op2 = StringDelta.builder()
			.retain(11)
			.done();
		
		Operation<StringHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.insert("Hello ")
			.retain(5)
			.done())
		);
	}
	
	@Test
	public void testCompose5()
	{
		Operation<StringHandler> op1 = StringDelta.builder()
			.delete("Hello ")
			.retain(5)
			.done();
		
		Operation<StringHandler> op2 = StringDelta.builder()
			.insert("Cookie ")
			.retain(5)
			.done();
		
		Operation<StringHandler> r = compose(op1, op2);
		
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
		Operation<StringHandler> op1 = StringDelta.builder()
			.insert("Cookie ")
			.retain(5)
			.done();
		
		Operation<StringHandler> op2 = StringDelta.builder()
			.delete("Cookie")
			.retain(5)
			.done();
		
		try
		{
			Operation<StringHandler> o = compose(op1, op2);
			fail("Should not be composable, but got " + o);
		}
		catch(ComposeException e)
		{
		}
	}
	
	@Test
	public void testCompose7()
	{
		Operation<StringHandler> op1 = StringDelta.builder()
			.insert("Hello Cookie")
			.done();
		
		Operation<StringHandler> op2 = StringDelta.builder()
			.retain(6)
			.insert("!")
			.retain(6)
			.done();
		
		Operation<StringHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.insert("Hello !Cookie")
			.done()
		));
	}
	
	@Test
	public void testCompose8()
	{
		Operation<StringHandler> op1 = StringDelta.builder()
			.retain(6)
			.insert("!")
			.done();
		
		Operation<StringHandler> op2 = StringDelta.builder()
			.insert("Cookie")
			.retain(7)
			.done();
		
		Operation<StringHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.insert("Cookie")
			.retain(6)
			.insert("!")
			.done()
		));
	}
	
	@Test
	public void testCompose9()
	{
		Operation<StringHandler> op1 = StringDelta.builder()
			.insert("Hello World!")
			.done();
		
		Operation<StringHandler> op2 = StringDelta.builder()
			.retain(6)
			.delete("World")
			.insert("Cookies")
			.retain(1)
			.done();
		
		Operation<StringHandler> r = compose(op1, op2);
		
		assertThat(r, is(StringDelta.builder()
			.insert("Hello Cookies!")
			.done()
		));
	}
	
	private Operation<StringHandler> compose(Operation<StringHandler> op1,
			Operation<StringHandler> op2)
	{
		StringType helper = new StringType();
		return helper.compose(op1, op2);
	}
}
