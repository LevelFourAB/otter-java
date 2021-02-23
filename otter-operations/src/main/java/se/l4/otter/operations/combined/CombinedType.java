package se.l4.otter.operations.combined;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.map.ImmutableMap;

import se.l4.exobytes.Serializer;
import se.l4.otter.operations.ComposeException;
import se.l4.otter.operations.CompoundOperation;
import se.l4.otter.operations.OTType;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationPair;
import se.l4.otter.operations.TransformException;
import se.l4.otter.operations.internal.combined.CombinedOperation;
import se.l4.otter.operations.internal.combined.CombinedOperationSerializer;
import se.l4.otter.operations.internal.combined.IdComparator;
import se.l4.otter.operations.internal.combined.Update;
import se.l4.otter.operations.util.MutableOperationIterator;

/**
 * Type for combining different types of operational transformations. This is
 * based on creating instances of other types dynamically and creating a
 * way to reference these using a simple identifier.
 *
 * @author Andreas Holstenson
 *
 */
public class CombinedType
	implements OTType<Operation<CombinedHandler>>
{
	private final ImmutableMap<String, OTType<Operation<?>>> types;
	private final CombinedOperationSerializer serializer;

	public CombinedType(ImmutableMap<String, OTType<Operation<?>>> types)
	{
		this.types = types;

		serializer = new CombinedOperationSerializer(types);
	}

	public OTType<Operation<?>> getSubType(String type)
	{
		return types.get(type);
	}

	@Override
	public Operation<CombinedHandler> compose(Operation<CombinedHandler> left, Operation<CombinedHandler> right)
	{
		IdComparator comparator = IdComparator.INSTANCE;
		MutableOperationIterator<CombinedHandler> it1 = new MutableOperationIterator<>(CompoundOperation.toList(left));
		MutableOperationIterator<CombinedHandler> it2 = new MutableOperationIterator<>(CompoundOperation.toList(right));

		List<Operation<CombinedHandler>> result = new ArrayList<>();
		while(it1.hasNext())
		{
			CombinedOperation op1 = (CombinedOperation) it1.next();

			boolean handled = false;
			while(it2.hasNext())
			{
				CombinedOperation op2 = (CombinedOperation) it2.next();

				int compared = comparator.compare(op1, op2);

				if(compared > 0)
				{
					/*
					 * Left key is larger than right, so push the right key
					 * onto result as we can't combine it with anything.
					 */
					result.add(op2);
					continue;
				}
				else if(compared < 0)
				{
					/**
					 * Left key is smaller than right, release it back for
					 * handling and try next left operation.
					 */
					it2.back();
				}
				else
				{
					if(op1 instanceof Update && op2 instanceof Update)
					{
						Update o1 = (Update) op1;
						Update o2 = (Update) op2;

						String t1 = o1.getType();
						if(! t1.equals(o2.getType()))
						{
							throw new ComposeException("Can not compose operations with id " + o1.getId() + ", they have different types: " + t1 + " vs " + o2.getType());
						}

						OTType type = types.get(t1);
						if(type == null)
						{
							throw new ComposeException("Unknown type: " + t1);
						}

						Operation composed = type.compose(o1.getOperation(), o2.getOperation());
						it1.replace(new Update(o1.getId(), t1, composed));
					}

					handled = true;
				}

				break;
			}

			if(! handled)
			{
				result.add(op1);
			}
		}

		while(it2.hasNext())
		{
			Operation<CombinedHandler> op = it2.next();
			result.add(op);
		}

		return CompoundOperation.create(result);
	}

	@Override
	public OperationPair<Operation<CombinedHandler>> transform(Operation<CombinedHandler> left, Operation<CombinedHandler> right)
	{
		IdComparator comparator = IdComparator.INSTANCE;
		MutableOperationIterator<CombinedHandler> it1 = new MutableOperationIterator<>(CompoundOperation.toList(left));
		MutableOperationIterator<CombinedHandler> it2 = new MutableOperationIterator<>(CompoundOperation.toList(right));

		List<Operation<CombinedHandler>> deltaLeft = new ArrayList<>();
		List<Operation<CombinedHandler>> deltaRight = new ArrayList<>();

		while(it1.hasNext())
		{
			CombinedOperation op1 = (CombinedOperation) it1.next();

			boolean handled = false;
			while(it2.hasNext())
			{
				CombinedOperation op2 = (CombinedOperation) it2.next();

				int compared = comparator.compare(op1, op2);

				if(compared > 0)
				{
					/**
					 * Left key is more than right, no transformation against
					 * left key to be done. Push right onto delta right.
					 */
					deltaRight.add(op2);
					continue;
				}
				else if(compared < 0)
				{
					/*
					 * Left key is less than right, back up right by one and
					 * let left key be added to delta left.
					 */
					it2.back();
				}
				else
				{
					if(op1 instanceof Update && op2 instanceof Update)
					{
						Update o1 = (Update) op1;
						Update o2 = (Update) op2;

						String t1 = o1.getType();
						if(! t1.equals(o2.getType()))
						{
							throw new TransformException("Can not transform operations with id " + o1.getId() + ", they have different types: " + t1 + " vs " + o2.getType());
						}

						OTType type = types.get(t1);
						if(type == null)
						{
							throw new TransformException("Unknown type: " + t1);
						}

						OperationPair transformed = type.transform(o1.getOperation(), o2.getOperation());
						deltaLeft.add(new Update(o1.getId(), t1, transformed.getLeft()));
						deltaRight.add(new Update(o1.getId(), t1, transformed.getRight()));
					}

					handled = true;
				}

				break;
			}

			if(! handled)
			{
				deltaLeft.add(op1);
			}
		}

		while(it2.hasNext())
		{
			Operation<CombinedHandler> op = it2.next();
			deltaRight.add(op);
		}

		return new OperationPair<>(
			CompoundOperation.create(deltaLeft),
			CompoundOperation.create(deltaRight)
		);
	}

	@Override
	public Serializer<Operation<CombinedHandler>> getSerializer()
	{
		return serializer;
	}

	/**
	 * Compose two operations of the given type.
	 *
	 * @param type
	 * @param left
	 * @param right
	 * @return
	 */
	public Operation<?> compose(String type, Operation<?> left, Operation<?> right)
	{
		OTType<Operation<?>> subType = types.get(type);
		if(subType == null)
		{
			throw new ComposeException("Unknown sub type: " + type);
		}

		return subType.compose(left, right);
	}
}
