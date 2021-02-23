package se.l4.otter.operations;

/**
 * Exception for when a combine between two operations fail.
 *
 * @author Andreas Holstenson
 *
 */
public class ComposeException
	extends OperationException
{

	public ComposeException()
	{
		super();
	}

	public ComposeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ComposeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ComposeException(String message)
	{
		super(message);
	}

	public ComposeException(Throwable cause)
	{
		super(cause);
	}

}
