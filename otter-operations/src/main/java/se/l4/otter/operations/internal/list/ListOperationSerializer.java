package se.l4.otter.operations.internal.list;

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
import se.l4.otter.operations.list.ListHandler;

public class ListOperationSerializer
	implements Serializer<Operation<ListHandler>>
{
	public static final ListOperationSerializer INSTANCE = new ListOperationSerializer();

	@Override
	public Operation<ListHandler> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);

		List<Operation<ListHandler>> ops = new ArrayList<>();

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
							case "retain":
								in.next(Token.VALUE);
								ops.add(new ListRetain(in.getInt()));
								break;
							case "insert":
								ops.add(new ListInsert(readObjects(in)));
								break;
							case "delete":
								ops.add(new ListDelete(readObjects(in)));
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

	private Object[] readObjects(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);
		List<Object> result = new ArrayList<>();
		while(in.peek() != Token.LIST_END)
		{
			result.add(DataSerializer.INSTANCE.read(in));
		}
		in.next(Token.LIST_END);

		return result.toArray();
	}

	@Override
	public void write(Operation<ListHandler> object, String name, StreamingOutput out)
		throws IOException
	{
		out.writeListStart(name);
		for(Operation<ListHandler> op : CompoundOperation.toList(object))
		{
			out.writeListStart("op");
			if(op instanceof ListRetain)
			{
				out.write("type", "retain");
				out.write("length", ((ListRetain) op).getLength());
			}
			else if(op instanceof ListInsert)
			{
				out.write("type", "insert");
				out.writeListStart("items");

				for(Object o : ((ListInsert) op).getItems())
				{
					DataSerializer.INSTANCE.write(o, "entry", out);
				}

				out.writeListEnd("items");
			}
			else if(op instanceof ListDelete)
			{
				out.write("type", "delete");
				out.writeListStart("items");

				for(Object o : ((ListDelete) op).getItems())
				{
					DataSerializer.INSTANCE.write(o, "entry", out);
				}

				out.writeListEnd("items");
			}
			else
			{
				throw new OperationException("Unsupported operation: " + op);
			}
			out.writeListEnd("op");
		}
		out.writeListEnd(name);
	}

}
