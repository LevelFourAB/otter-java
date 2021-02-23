package se.l4.otter.model.spi;

import java.util.ArrayList;
import java.util.List;

import se.l4.otter.model.SharedObject;
import se.l4.otter.operations.OperationException;

/**
 * Utility for converting between values suitable for use with operations
 * and values in the model.
 *
 * @author Andreas Holstenson
 *
 */
public class DataValues
{
	public static Object toData(Object value)
	{
		List<Object> result = new ArrayList<>();
		if(value instanceof SharedObject)
		{
			result.add("ref");
			result.add(((SharedObject) value).getObjectId());
			result.add(((SharedObject) value).getObjectType());
		}
		else
		{
			// TODO: Better value validation

			result.add("value");
			result.add(value);
		}

		return result;
	}

	public static Object fromData(SharedObjectEditor<?> editor, Object value)
	{
		List<Object> array = (List<Object>) value;
		String type = (String) array.get(0);

		switch(type)
		{
			case "ref":
				return editor.getObject(
					(String) array.get(1),
					(String) array.get(2)
				);
			case "value":
				return array.get(1);
			default:
				throw new OperationException("Value of shared map " + editor.getId() + " has unknown type of value");
		}
	}
}
