package se.l4.otter.operations.internal.string;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.string.StringOperationHandler;
import se.l4.otter.operations.string.StringType;

/**
 * Parser for use with {@link StringType}.
 * 
 * @author Andreas Holstenson
 *
 */
public class StringOperationParser
{
	private final StringOperationBuilder builder;
	private String input;
	private int length;
	private int index;
	private StringBuilder buffer;
	
	public StringOperationParser(String input)
	{
		this.input = input;
		length = input.length();
		builder = new StringOperationBuilder();
		buffer = new StringBuilder();
	}
	
	protected void readWhiteSpace()
	{
		for(; index<length; index++)
		{
			if(! Character.isWhitespace(input.charAt(index)))
			{
				break;
			}
		}
	}
	
	protected boolean current(String key)
	{
		for(int i=0, n=key.length(); i<n; i++)
		{
			if(input.charAt(index+i) != key.charAt(i))
			{
				return false;
			}
		}
		
		return true;
	}
	
	protected void consume(String key)
	{
		for(int i=0, n=key.length(); i<n; i++)
		{
			if(input.charAt(index++) != key.charAt(i))
			{
				throw new RuntimeException("Tried to consume " + key + ", but could not");
			}
		}
	}
	
	protected void consume(char c)
	{
		if(input.charAt(index++) != c)
		{
			throw new RuntimeException("Tried to consume " + c + ", but could not");
		}
	}
	
	protected void consumeOpEnd()
	{
		readWhiteSpace();
		
		if(current() == ';')
		{
			consume(';');
		}
		else if(index+1 < length)
		{
			throw new RuntimeException("Expected end of operation (;), but got " + current());
		}
	}
	
	protected char current()
	{
		return input.charAt(index);
	}
	
	protected String readQuotedString()
	{
		consume('\'');
		for(; index<length; index++)
		{
			char c = input.charAt(index);
			if(c == '\\')
			{
				c = input.charAt(--index);
				if(c == '\'')
				{
					buffer.append("'");
				}
				else
				{
					buffer.append(c);
					index--;
				}
			}
			else if(c == '\'')
			{
				String result = buffer.toString();
				buffer.setLength(0);
				index++; // consume this character
				return result;
			}
			else
			{
				buffer.append(c);
			}
		}
		
		throw new RuntimeException("Could not read end of quoted string");
	}
	
	private String readUntil(char c)
	{
		int start = index;
		for(; index<length; index++)
		{
			if(input.charAt(index) == c)
			{
				return input.substring(start, index);
			}
		}
		
		throw new RuntimeException("Ran out of characters while looking for " + c);
	}
	
	private boolean readOperation()
	{
		readWhiteSpace();
		
		if(index >= length) return false;
		
		switch(current())
		{
			case '_':
				readRetain();
				break;
			case '+':
				readInsert();
				break;
			case '-':
				readDelete();
				break;
			default:
				throw new RuntimeException("Unknown operation");
		}
		
		consumeOpEnd();
		
		return true;
	}
	
	private void readRetain()
	{
		consume("__");
		String value = readUntil(';');
		builder.retain(Integer.parseInt(value));
	}
	
	private void readInsert()
	{
		consume("++");
		String chars = readQuotedString();
		builder.insert(chars);
	}
	
	private void readDelete()
	{
		consume("--");
		String chars = readQuotedString();
		builder.delete(chars);
	}
	
	public Operation<StringOperationHandler> parse()
	{
		while(readOperation())
		{
		}
		
		return builder.build();
	}
}
