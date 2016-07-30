package se.l4.otter.operations.internal.combined;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.l4.commons.serialization.Serializer;
import se.l4.commons.serialization.format.StreamingInput;
import se.l4.commons.serialization.format.StreamingOutput;
import se.l4.commons.serialization.format.Token;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.DefaultCompoundOperation;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.combined.CombinedTarget;

public class CombinedOperationSerializer
	implements Serializer<Operation<CombinedTarget>>
{
	private final Map<String, OTType<Operation<?>>> types;

	public CombinedOperationSerializer(Map<String, OTType<Operation<?>>> types)
	{
		this.types = types;
	}
	
	@Override
	public Operation<CombinedTarget> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.LIST_START);
		
		List<Operation<CombinedTarget>> ops = new ArrayList<>();
		
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
						if("update".equals(type))
						{
							ops.add(readUpdate(in));
							break;
						}
						else
						{
							in.skipValue();
						}
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
	
	private Operation<CombinedTarget> readUpdate(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		String id = in.getString();
		
		in.next(Token.VALUE);
		String type = in.getString();
		
		Operation<?> op = serializer(type)
			.read(in);
		
		return new Update(id, type, op);
	}

	@Override
	public void write(Operation<CombinedTarget> object, String name, StreamingOutput out)
		throws IOException
	{
		out.writeListStart(name);
		for(Operation<CombinedTarget> op : CompoundOperation.toList(object))
		{
			if(op instanceof Update)
			{
				Update update = ((Update) op);
				
				out.writeListStart("entry");
				
				out.write("type", "update");
				
				out.write("id", update.getId());
				out.write("type", update.getType());
				
				serializer(update.getType())
					.write(update.getOperation(), "op", out);
				
				out.writeListEnd("entry");
			}
			else
			{
				throw new OperationException("Unsupported operation: " + op);
			}
		}
		out.writeListEnd(name);
	}

	private Serializer<Operation<?>> serializer(String type)
	{
		return types.get(type).getSerializer();
	}
	
}
