package se.l4.otter.operations.internal.combined;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.map.ImmutableMap;

import se.l4.exobytes.Serializer;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.combined.CombinedHandler;

public class CombinedOperationSerializer
	implements Serializer<Operation<CombinedHandler>>
{
	private final ImmutableMap<String, OTType<Operation<?>>> types;

	public CombinedOperationSerializer(ImmutableMap<String, OTType<Operation<?>>> types)
	{
		this.types = types;
	}

	@Override
	public Operation<CombinedHandler> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);

		List<Operation<CombinedHandler>> ops = new ArrayList<>();

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
						if("update".equals(type))
						{
							ops.add(readUpdate(in));
							break;
						}
						else
						{
							in.skipNext();
						}
					default:
						in.skipNext();
						break;
				}
				idx++;
			}
			in.next(Token.LIST_END);
		}

		in.next(Token.LIST_END);
		ops.sort(IdComparator.INSTANCE);
		return CompoundOperation.create(ops);
	}

	private Operation<CombinedHandler> readUpdate(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		String id = in.readString();

		in.next(Token.VALUE);
		String type = in.readString();

		Operation<?> op = serializer(type)
			.read(in);

		return new Update(id, type, op);
	}

	@Override
	public void write(Operation<CombinedHandler> object, StreamingOutput out)
		throws IOException
	{
		out.writeListStart();
		for(Operation<CombinedHandler> op : CompoundOperation.toList(object))
		{
			if(op instanceof Update)
			{
				Update update = ((Update) op);

				out.writeListStart();

				out.writeString("update");

				out.writeString(update.getId());
				out.writeString(update.getType());

				serializer(update.getType())
					.write(update.getOperation(), out);

				out.writeListEnd();
			}
			else
			{
				throw new OperationException("Unsupported operation: " + op);
			}
		}
		out.writeListEnd();
	}

	private Serializer<Operation<?>> serializer(String type)
	{
		return types.get(type).getSerializer();
	}

}
