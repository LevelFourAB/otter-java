package se.l4.otter.model.spi;

import se.l4.otter.operations.Operation;

public interface HasApply<Op extends Operation<?>>
{
	void apply(Op op);
}
