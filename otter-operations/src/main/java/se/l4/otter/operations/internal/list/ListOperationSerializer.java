package se.l4.otter.operations.internal.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import se.l4.exobytes.Serializer;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;
import se.l4.otter.operations.CompoundOperation;
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

		MutableList<Operation<ListHandler>> ops = Lists.mutable.ofInitialCapacity(
			in.getLength().orElse(5)
		);

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
							case "retain":
								in.next(Token.VALUE);
								ops.add(new ListRetain(in.readInt()));
								break;
							case "insert":
								ops.add(new ListInsert(readObjects(in)));
								break;
							case "delete":
								ops.add(new ListDelete(readObjects(in)));
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
		return CompoundOperation.create(ops);
	}

	private Object[] readObjects(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);
		List<Object> result = new ArrayList<>();
		while(in.peek() != Token.LIST_END)
		{
			in.next();
			result.add(in.readDynamic());
		}
		in.next(Token.LIST_END);

		return result.toArray();
	}

	@Override
	public void write(Operation<ListHandler> object, StreamingOutput out)
		throws IOException
	{
		out.writeListStart();
		for(Operation<ListHandler> op : CompoundOperation.toList(object))
		{
			out.writeListStart();
			if(op instanceof ListRetain)
			{
				out.writeString("retain");
				out.writeInt(((ListRetain) op).getLength());
			}
			else if(op instanceof ListInsert)
			{
				out.writeString("insert");
				out.writeListStart();

				for(Object o : ((ListInsert) op).getItems())
				{
					out.writeDynamic(o);
				}

				out.writeListEnd();
			}
			else if(op instanceof ListDelete)
			{
				out.writeString("delete");
				out.writeListStart();

				for(Object o : ((ListDelete) op).getItems())
				{
					out.writeDynamic(o);
				}

				out.writeListEnd();
			}
			else
			{
				throw new OperationException("Unsupported operation: " + op);
			}
			out.writeListEnd();
		}
		out.writeListEnd();
	}

}
