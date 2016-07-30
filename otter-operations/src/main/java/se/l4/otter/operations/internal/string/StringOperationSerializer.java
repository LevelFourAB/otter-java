package se.l4.otter.operations.internal.string;

import java.io.IOException;

import se.l4.commons.serialization.SerializationException;
import se.l4.commons.serialization.Serializer;
import se.l4.commons.serialization.format.StreamingInput;
import se.l4.commons.serialization.format.StreamingOutput;
import se.l4.commons.serialization.format.Token;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringOperationHandler;
import se.l4.otter.operations.string.StringType;

/**
 * {@link Serializer} used for {@link StringType}. Uses the same format
 * that Google Wave once used as it is pretty compact.
 * 
 * @author Andreas Holstenson
 *
 */
public class StringOperationSerializer
	implements Serializer<Operation<StringOperationHandler>>
{
	@Override
	public Operation<StringOperationHandler> read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);
		return new StringOperationParser(in.getString()).parse();
	}
	
	@Override
	public void write(Operation<StringOperationHandler> object, String name, StreamingOutput stream)
		throws IOException
	{
		StringBuilder builder = new StringBuilder();
		for(Operation<StringOperationHandler> op : CompoundOperation.toList(object))
		{
			if(op instanceof StringRetain)
			{
				builder.append("__" + ((StringRetain) op).getLength());
			}
			else if(op instanceof StringInsert)
			{
				builder.append("++");
				quote(builder, ((StringInsert) op).getValue());
			}
			else if(op instanceof StringDelete)
			{
				builder.append("--");
				quote(builder, ((StringDelete) op).getValue());
			}
			else
			{
				throw new SerializationException("Unable to serialize operation; Received unsupported operation: " + op);
			}
			builder.append(';');
		}
		
		stream.write(name, builder.toString());
	}
	
	private void quote(StringBuilder builder, String s)
	{
		if(s == null)
		{
			builder.append("null");
			return;
		}
		
		builder.append("'");
		for(int i=0, n=s.length(); i<n; i++)
		{
			char c = s.charAt(i);
			if(c == '\'')
			{
				builder.append("\\'");
			}
			else
			{
				builder.append(c);
			}
		}
		builder.append("'");
	}
	
}
