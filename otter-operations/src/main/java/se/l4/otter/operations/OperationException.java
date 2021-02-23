package se.l4.otter.operations;

/**
 * Base exception for problems that occur during work with {@link Operation}s.
 *
 * @author Andreas Holstenson
 *
 */
public class OperationException
	extends RuntimeException
{

	public OperationException()
	{
		super();
	}

	public OperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public OperationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public OperationException(String message)
	{
		super(message);
	}

	public OperationException(Throwable cause)
	{
		super(cause);
	}

}
