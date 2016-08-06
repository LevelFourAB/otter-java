package se.l4.otter.operations.internal.string;

import java.io.IOException;

import se.l4.commons.serialization.SerializationException;
import se.l4.commons.serialization.Serializer;
import se.l4.commons.serialization.format.StreamingInput;
import se.l4.commons.serialization.format.StreamingOutput;
import se.l4.commons.serialization.format.Token;
import se.l4.otter.data.DataSerializer;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.ValueChange;
import se.l4.otter.operations.string.AnnotationChange;
import se.l4.otter.operations.string.AnnotationChangeBuilder;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringHandler;
import se.l4.otter.operations.string.StringType;

/**
 * {@link Serializer} used for {@link StringType}. Uses the same format
 * that Google Wave once used as it is pretty compact.
 * 
 * @author Andreas Holstenson
 *
 */
public class StringOperationSerializer
	implements Serializer<Operation<StringHandler>>
{
	@Override
	public Operation<StringHandler> read(StreamingInput in)
		throws IOException
	{
		StringDelta<Operation<StringHandler>> delta = StringDelta.builder();
		
		in.next(Token.LIST_START);
		while(in.peek() != Token.LIST_END)
		{
			in.next(Token.LIST_START);
			
			in.next(Token.VALUE);
			String type = in.getString();
			
			switch(type)
			{
				case "retain":
					in.next(Token.VALUE);
					delta.retain(in.getInt());
					break;
				case "insert":
					in.next(Token.VALUE);
					delta.insert(in.getString());
					break;
				case "delete":
					in.next(Token.VALUE);
					delta.delete(in.getString());
					break;
				case "annotations":
					readAnnotation(in, delta);
					break;
			}
			
			while(in.peek() != Token.LIST_END)
			{
				in.next();
				in.skip();
			}
			
			in.next(Token.LIST_END);
		}
		in.next(Token.LIST_END);
		
		return delta.done();
	}
	
	private void readAnnotation(StreamingInput in, StringDelta<Operation<StringHandler>> delta)
		throws IOException
	{
		AnnotationChangeBuilder<StringDelta<Operation<StringHandler>>> annotations = delta.updateAnnotations();
		
		in.next(Token.OBJECT_START);
		while(in.peek() != Token.OBJECT_END)
		{
			in.next(Token.KEY);
			String key = in.getString();
			
			in.next(Token.OBJECT_START);
			
			Object oldValue = null;
			Object newValue = null;
			
			while(in.peek() != Token.OBJECT_END)
			{
				in.next(Token.KEY);
				switch(in.getString())
				{
					case "oldValue":
						oldValue = DataSerializer.INSTANCE.read(in);
						break;
					case "newValue":
						newValue = DataSerializer.INSTANCE.read(in);
						break;
					default:
						in.skipValue();
						break;
				}
			}
			
			if(newValue == null)
			{
				annotations.remove(key, oldValue);
			}
			else
			{
				annotations.set(key, oldValue, newValue);
			}
			
			in.next(Token.OBJECT_END);
		}
		in.next(Token.OBJECT_END);
		
		annotations.done();
	}

	@Override
	public void write(Operation<StringHandler> object, String name, StreamingOutput out)
		throws IOException
	{
		out.writeListStart(name);
		for(Operation<StringHandler> op : CompoundOperation.toList(object))
		{
			out.writeListStart("op");
			if(op instanceof StringRetain)
			{
				out.write("type", "retain");
				out.write("length", ((StringRetain) op).getLength());
			}
			else if(op instanceof StringInsert)
			{
				out.write("type", "insert");
				out.write("value", ((StringInsert) op).getValue());
			}
			else if(op instanceof StringDelete)
			{
				out.write("type", "delete");
				out.write("value", ((StringDelete) op).getValue());
			}
			else if(op instanceof StringAnnotationChange)
			{
				out.write("type", "annotations");
				writeAnnotationChange((StringAnnotationChange) op, out);
			}
			else
			{
				throw new SerializationException("Unable to serialize operation; Received unsupported operation: " + op);
			}
			out.writeListEnd("end");
		}
		
		out.writeListEnd(name);
	}
	
	private void writeAnnotationChange(StringAnnotationChange op, StreamingOutput out)
		throws IOException
	{
		out.writeObjectStart("changes");
		AnnotationChange change = op.getChange();
		for(String key : change.keys())
		{
			ValueChange keyChange = change.getChange(key);
			
			out.writeObjectStart(key);
			
			DataSerializer.INSTANCE.write(keyChange.getOldValue(), "oldValue", out);
			DataSerializer.INSTANCE.write(keyChange.getNewValue(), "newValue", out);
			
			out.writeObjectEnd(key);
		}
		out.writeObjectEnd("changes");
	}
	
}
