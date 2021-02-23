package se.l4.otter.operations.internal.string;

import java.io.IOException;

import org.eclipse.collections.api.list.ListIterable;

import se.l4.exobytes.SerializationException;
import se.l4.exobytes.Serializer;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;
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
			String type = in.readString();

			switch(type)
			{
				case "retain":
					in.next(Token.VALUE);
					delta.retain(in.readInt());
					break;
				case "insert":
					in.next(Token.VALUE);
					delta.insert(in.readString());
					break;
				case "delete":
					in.next(Token.VALUE);
					delta.delete(in.readString());
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
			in.next(Token.VALUE);
			String key = in.readString();

			in.next(Token.OBJECT_START);

			Object oldValue = null;
			Object newValue = null;

			while(in.peek() != Token.OBJECT_END)
			{
				in.next(Token.VALUE);
				switch(in.readString())
				{
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
	public void write(Operation<StringHandler> object, StreamingOutput out)
		throws IOException
	{
		ListIterable<Operation<StringHandler>> list = CompoundOperation.toList(object);
		out.writeListStart(list.size());

		for(Operation<StringHandler> op : list)
		{
			out.writeListStart();
			if(op instanceof StringRetain)
			{
				out.writeString("retain");
				out.writeInt(((StringRetain) op).getLength());
			}
			else if(op instanceof StringInsert)
			{
				out.writeString("insert");
				out.writeString(((StringInsert) op).getValue());
			}
			else if(op instanceof StringDelete)
			{
				out.writeString("delete");
				out.writeString(((StringDelete) op).getValue());
			}
			else if(op instanceof StringAnnotationChange)
			{
				out.writeString("annotations");
				writeAnnotationChange((StringAnnotationChange) op, out);
			}
			else
			{
				throw new SerializationException("Unable to serialize operation; Received unsupported operation: " + op);
			}
			out.writeListEnd();
		}

		out.writeListEnd();
	}

	private void writeAnnotationChange(StringAnnotationChange op, StreamingOutput out)
		throws IOException
	{
		out.writeObjectStart();
		AnnotationChange change = op.getChange();
		for(String key : change.keys())
		{
			out.writeString(key);

			ValueChange keyChange = change.getChange(key);

			out.writeObjectStart();

			out.writeString("oldValue");
			out.writeDynamic(keyChange.getOldValue());

			out.writeString("newValue");
			out.writeDynamic(keyChange.getNewValue());

			out.writeObjectEnd();
		}
		out.writeObjectEnd();
	}

}
