package se.l4.otter.operations.internal.string;

import se.l4.otter.operations.string.StringHandler;

/**
 * Normalizer that takes operations that follow each other and combines them.
 * 
 * @author Andreas Holstenson
 *
 */
public class StringNormalizer
	implements StringHandler
{
	private enum State
	{
		EMPTY,
		RETAIN,
		INSERT,
		DELETE
	}
	
	private final StringHandler target;
	
	private final StringBuilder characters;
	private int retainCount;
	private State state;
	
	public StringNormalizer(StringHandler target)
	{
		this.target = target;
	
		characters = new StringBuilder();
		state = State.EMPTY;
	}
	
	public void flush()
	{
		switch(state)
		{
			case RETAIN:
				if(retainCount > 0)
				{
					target.retain(retainCount);
				}
				break;
			case DELETE:
				if(characters.length() > 0)
				{
					target.delete(characters.toString());
				}
				break;
			case INSERT:
				if(characters.length() > 0)
				{
					target.insert(characters.toString());
				}
				break;
		}
		
		characters.setLength(0);
		retainCount = 0;
	}
	
	@Override
	public void insert(String s)
	{
		if(! s.isEmpty())
		{
			if(state != State.INSERT)
			{
				flush();
				state = State.INSERT;
			}
			
			characters.append(s);
		}
	}
	
	@Override
	public void delete(String s)
	{
		if(! s.isEmpty())
		{
			if(state != State.DELETE)
			{
				flush();
				state = State.DELETE;
			}
			
			characters.append(s);
		}
	}
	
	@Override
	public void retain(int itemCount)
	{
		if(itemCount > 0)
		{
			if(state != State.RETAIN)
			{
				flush();
				state = State.RETAIN;
			}
			
			retainCount += itemCount;
		}
	}
}
