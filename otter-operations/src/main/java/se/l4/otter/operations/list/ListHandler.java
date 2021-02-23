package se.l4.otter.operations.list;

public interface ListHandler
{
	void retain(int length);

	void insert(Object item);

	void delete(Object item);
}
