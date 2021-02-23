package se.l4.otter.operations;

/**
 * Exception for when a combine between two operations fail.
 *
 * @author Andreas Holstenson
 *
 */
public class TransformException
	extends OperationException
{

	public TransformException()
	{
		super();
	}

	public TransformException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TransformException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public TransformException(String message)
	{
		super(message);
	}

	public TransformException(Throwable cause)
	{
		super(cause);
	}

}
