package se.l4.otter.operations;

/**
 * A pair of {@link Operation}s.
 * 
 * @author Andreas Holstenson
 *
 * @param <Op>
 */
public class OperationPair<Op>
{
	private final Op first;
	private final Op second;
	
	public OperationPair(Op first, Op second)
	{
		this.first = first;
		this.second = second;
	}
	
	public Op getFirst()
	{
		return first;
	}
	
	public Op getSecond()
	{
		return second;
	}
}
