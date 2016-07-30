package se.l4.otter.operations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import com.google.common.base.Throwables;

import se.l4.commons.serialization.Serializer;
import se.l4.commons.serialization.format.JsonInput;
import se.l4.commons.serialization.format.JsonOutput;

public class SerializerTestHelper
{
	private SerializerTestHelper()
	{
	}
	
	public static <T> void testSymmetry(Serializer<T> serializer, T value)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try(JsonOutput json = new JsonOutput(out))
		{
			serializer.write(value, null, json);
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}
		
		T readValue;
		try(JsonInput in = new JsonInput(new ByteArrayInputStream(out.toByteArray())))
		{
			readValue = serializer.read(in);
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}
		
		assertThat(readValue, is(value));
	}
	
	public static <T> void testStatic(String json, Serializer<T> serializer, T value)
	{
		T readValue;
		try(JsonInput in = new JsonInput(new StringReader(json)))
		{
			readValue = serializer.read(in);
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}
		
		assertThat(readValue, is(value));
	}
}
