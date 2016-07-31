package se.l4.otter.operations.internal.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.l4.commons.serialization.Serializer;
import se.l4.commons.serialization.format.StreamingInput;
import se.l4.commons.serialization.format.StreamingOutput;
import se.l4.commons.serialization.format.Token;
import se.l4.otter.data.DataSerializer;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.DefaultCompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.map.MapOperationHandler;

public class MapOperationSerializer
	implements Serializer<Operation<MapOperationHandler>>
{
	public static final MapOperationSerializer INSTANCE = new MapOperationSerializer();
	
	@Override
	public Operation<MapOperationHandler> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);
		
		List<Operation<MapOperationHandler>> ops = new ArrayList<>();
		
		while(in.peek() != Token.LIST_END)
		{
			in.next(Token.LIST_START);
			int idx = 0;
			String type = null;
			while(in.peek() != Token.LIST_END)
			{
				switch(idx)
				{
					case 0:
						in.next(Token.VALUE);
						type = in.getString();
						break;
					case 1:
						switch(type)
						{
							case "set":
								ops.add(readSet(in));
								break;
							default:
								in.skipValue();
								break;
						}
						break;
					default:
						in.skipValue();
						break;
				}
				idx++;
			}
			in.next(Token.LIST_END);
		}
		
		in.next(Token.LIST_END);
		return new DefaultCompoundOperation<>(ops);
	}
	
	private MapSet readSet(StreamingInput in)
		throws IOException
	{
		in.next(Token.OBJECT_START);
		String setKey = null;
		Object oldValue = null;
		Object newValue = null;
		while(in.peek() != Token.OBJECT_END)
		{
			in.next(Token.KEY);
			String key = in.getString();
			
			switch(key)
			{
				case "key":
					in.next(Token.VALUE);
					setKey = in.getString();
					break;
				case "oldValue":
					oldValue = DataSerializer.INSTANCE.read(in);
					break;
				case "newValue":
					newValue = DataSerializer.INSTANCE.read(in);
					break;
				default:
					in.skipValue();
			}
		}
		in.next(Token.OBJECT_END);
		
		return new MapSet(setKey, oldValue, newValue);
	}

	@Override
	public void write(Operation<MapOperationHandler> object, String name, StreamingOutput out)
		throws IOException
	{
		out.writeListStart(name);
		for(Operation<MapOperationHandler> op : CompoundOperation.toList(object))
		{
			if(op instanceof MapSet)
			{
				MapSet set = ((MapSet) op);
				
				out.writeListStart("entry");
				
				out.write("type", "set");
				
				out.writeObjectStart("op");
				
				out.write("key", set.getKey());
				DataSerializer.INSTANCE.write(set.getOldValue(), "oldValue", out);
				DataSerializer.INSTANCE.write(set.getNewValue(), "newValue", out);
				
				out.writeObjectEnd("op");
				
				out.writeListEnd("entry");
			}
			else
			{
				throw new OperationException("Unsupported operation: " + op);
			}
		}
		out.writeListEnd(name);
	}
	
}
