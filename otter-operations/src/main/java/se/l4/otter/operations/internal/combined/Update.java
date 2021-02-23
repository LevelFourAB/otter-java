package se.l4.otter.operations.internal.combined;

import se.l4.otter.operations.Operation;
import se.l4.otter.operations.combined.CombinedHandler;

public class Update
	implements CombinedOperation
{
	private final String id;
	private final String type;
	private final Operation<?> operation;

	public Update(String id, String type, Operation<?> operation)
	{
		this.id = id;
		this.type = type;
		this.operation = operation;
	}

	@Override
	public String getId()
	{
		return id;
	}

	public String getType()
	{
		return type;
	}

	public Operation<?> getOperation()
	{
		return operation;
	}

	@Override
	public void apply(CombinedHandler target)
	{
		target.update(id, type, operation);
	}

	@Override
	public Operation<CombinedHandler> invert()
	{
		return new Update(id, type, operation.invert());
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[id=" + id + ", type=" + type + ", operation=" + operation + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Update other = (Update) obj;
		if(id == null)
		{
			if(other.id != null)
				return false;
		}
		else if(!id.equals(other.id))
			return false;
		if(operation == null)
		{
			if(other.operation != null)
				return false;
		}
		else if(!operation.equals(other.operation))
			return false;
		if(type == null)
		{
			if(other.type != null)
				return false;
		}
		else if(!type.equals(other.type))
			return false;
		return true;
	}

}
