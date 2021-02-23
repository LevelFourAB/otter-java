package se.l4.otter.operations.internal.map;

import java.io.IOException;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import se.l4.exobytes.Serializer;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.DefaultCompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.map.MapHandler;
import se.l4.otter.operations.map.MapKeyComparator;

public class MapOperationSerializer
	implements Serializer<Operation<MapHandler>>
{
	public static final MapOperationSerializer INSTANCE = new MapOperationSerializer();

	@Override
	public Operation<MapHandler> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);

		MutableList<Operation<MapHandler>> ops = Lists.mutable.empty();

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
						type = in.readString();
						break;
					case 1:
						switch(type)
						{
							case "set":
								ops.add(readSet(in));
								break;
							default:
								in.skipNext();
								break;
						}
						break;
					default:
						in.skipNext();
						break;
				}
				idx++;
			}

			in.next(Token.LIST_END);
		}

		in.next(Token.LIST_END);
		ops.sort(MapKeyComparator.INSTANCE);
		return new DefaultCompoundOperation<>(ops.toImmutable());
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
			in.next(Token.VALUE);
			String key = in.readString();

			switch(key)
			{
				case "key":
					in.next(Token.VALUE);
					setKey = in.readString();
					break;
				case "oldValue":
					in.next();
					oldValue = in.readDynamic();
					break;
				case "newValue":
					in.next();
					newValue = in.readDynamic();
					break;
				default:
					in.skipNext();
			}
		}
		
		in.next(Token.OBJECT_END);

		return new MapSet(setKey, oldValue, newValue);
	}

	@Override
	public void write(Operation<MapHandler> object, StreamingOutput out)
		throws IOException
	{
		out.writeListStart();
		
		for(Operation<MapHandler> op : CompoundOperation.toList(object))
		{
			if(op instanceof MapSet)
			{
				MapSet set = ((MapSet) op);

				out.writeListStart();

				out.writeString("set");

				out.writeObjectStart();

				out.writeString("key");
				out.writeString(set.getKey());
				
				out.writeString("oldValue");
				out.writeDynamic(set.getOldValue());

				out.writeString("newValue");
				out.writeDynamic(set.getNewValue());

				out.writeObjectEnd();

				out.writeListEnd();
			}
			else
			{
				throw new OperationException("Unsupported operation: " + op);
			}
		}
		
		out.writeListEnd();
	}

}
