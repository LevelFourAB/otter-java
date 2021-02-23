package se.l4.otter.engine;

import se.l4.otter.operations.Operation;

public class TaggedOperation<Op extends Operation<?>>
{
	private final long historyId;
	private final String token;
	private final Op operation;

	public TaggedOperation(long historyId, String token, Op operation)
	{
		this.historyId = historyId;
		this.token = token;
		this.operation = operation;
	}

	/**
	 * Get the identifier this data has in the history.
	 *
	 * @return
	 */
	public long getHistoryId()
	{
		return historyId;
	}

	/**
	 * Get the token provided by the client.
	 *
	 * @return
	 */
	public String getToken()
	{
		return token;
	}

	/**
	 * Get the operation.
	 *
	 * @return
	 */
	public Op getOperation()
	{
		return operation;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[historyId=" + historyId + ", token=" + token + ", operation=" + operation + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (historyId ^ (historyId >>> 32));
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		TaggedOperation other = (TaggedOperation) obj;
		if(historyId != other.historyId)
			return false;
		if(operation == null)
		{
			if(other.operation != null)
				return false;
		}
		else if(!operation.equals(other.operation))
			return false;
		if(token == null)
		{
			if(other.token != null)
				return false;
		}
		else if(!token.equals(other.token))
			return false;
		return true;
	}
}
