package se.l4.otter.data;

import java.io.IOException;
import java.util.Map;

import se.l4.commons.serialization.SerializationException;
import se.l4.commons.serialization.Serializer;
import se.l4.commons.serialization.format.StreamingInput;
import se.l4.commons.serialization.format.StreamingOutput;
import se.l4.commons.serialization.format.Token;

/**
 * Serializer that can be used for writing and reading basic data values,
 * such as {@link String}, long, int, short, double, float, boolean, 
 * {@link DataArray} and {@link DataObject}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DataSerializer
	implements Serializer<Object>
{
	public static final DataSerializer INSTANCE = new DataSerializer();

	@Override
	public Object read(StreamingInput in)
		throws IOException
	{
		return readDynamic(in);
	}
	
	/**
	 * Depending on the next token read either a value, a list or a map.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private static Object readDynamic(StreamingInput in)
		throws IOException
	{
		switch(in.peek())
		{
			case VALUE:
				in.next();
				return in.getValue();
			case NULL:
				in.next();
				return in.getValue();
			case LIST_START:
				return readList(in);
			case OBJECT_START:
				return readObject(in);
		}
		
		throw new SerializationException("Unable to read, unknown start of value: " + in.peek());
	}
	
	private static DataObject readObject(StreamingInput in)
		throws IOException
	{
		DataObject result = new DataObject();
		
		in.next();
		while(in.peek() != Token.OBJECT_END)
		{
			in.next(Token.KEY);
			String key = in.getString();
			
			Object value = readDynamic(in);
			
			result.put(key, value);
		}
		
		in.next(Token.OBJECT_END);
		
		return result;
	}
	
	private static DataArray readList(StreamingInput in)
		throws IOException
	{
		DataArray result = new DataArray();
		
		in.next(Token.LIST_START);
		while(in.peek() != Token.LIST_END)
		{
			// Read the value
			Object value = readDynamic(in);
			result.add(value);
		}
		
		in.next(Token.LIST_END);
		
		return result;
	}
	
	@Override
	public void write(Object object, String name, StreamingOutput out)
		throws IOException
	{
		if(object == null)
		{
			out.writeNull(name);
		}
		else if(object instanceof DataObject)
		{
			out.writeObjectStart(name);
			for(Map.Entry<String, Object> e : ((DataObject) object).entrySet())
			{
				write(e.getValue(), e.getKey(), out);
			}
			out.writeObjectEnd(name);
		}
		else if(object instanceof DataArray)
		{
			out.writeListStart(name);
			for(Object o : (DataArray) object)
			{
				write(o, "entry", out);
			}
			out.writeListEnd(name);
		}
		else if(object instanceof Long || object instanceof Integer || object instanceof Short)
		{
			out.write(name, ((Number) object).longValue());
		}
		else if(object instanceof Double || object instanceof Float)
		{
			out.write(name, ((Number) object).doubleValue());
		}
		else if(object instanceof String)
		{
			out.write(name, (String) object);
		}
		else if(object instanceof Boolean)
		{
			out.write(name, (Boolean) object);
		}
		else
		{
			throw new SerializationException("Unsupported type of data: " + object);
		}
	}

}
