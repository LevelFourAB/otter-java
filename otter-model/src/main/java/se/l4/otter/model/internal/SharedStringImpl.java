package se.l4.otter.model.internal;

import java.util.LinkedList;

import com.sksamuel.diffpatch.DiffMatchPatch;
import com.sksamuel.diffpatch.DiffMatchPatch.Diff;

import se.l4.otter.lock.CloseableLock;
import se.l4.otter.model.SharedString;
import se.l4.otter.model.spi.AbstractSharedObject;
import se.l4.otter.model.spi.SharedObjectEditor;
import se.l4.otter.operations.Operation;
import se.l4.otter.operations.OperationException;
import se.l4.otter.operations.string.AnnotationChange;
import se.l4.otter.operations.string.StringDelta;
import se.l4.otter.operations.string.StringHandler;

public class SharedStringImpl
	extends AbstractSharedObject<Operation<StringHandler>>
	implements SharedString
{
	private static final DiffMatchPatch DIFF = new DiffMatchPatch();

	private StringBuilder value;

	public SharedStringImpl(SharedObjectEditor<Operation<StringHandler>> editor)
	{
		super(editor);

		value = new StringBuilder();
		editor.getCurrent().apply(new StringHandler()
		{
			@Override
			public void retain(int count)
			{
				throw new OperationException("Latest value invalid, must only contain inserts.");
			}

			@Override
			public void insert(String s)
			{
				value.append(s);
			}

			@Override
			public void delete(String s)
			{
				throw new OperationException("Latest value invalid, must only contain inserts.");
			}

			@Override
			public void annotationUpdate(AnnotationChange change)
			{
				// Annotations are not currently handled
			}
		});

		editor.setOperationHandler(this::apply);
	}

	private void apply(Operation<StringHandler> op, boolean local)
	{
		op.apply(new StringHandler()
		{
			int index = 0;

			@Override
			public void retain(int count)
			{
				index += count;
			}

			@Override
			public void insert(String s)
			{
				value.insert(index, s);
				index += s.length();
			}

			@Override
			public void delete(String s)
			{
				value.delete(index, index + s.length());
			}

			@Override
			public void annotationUpdate(AnnotationChange change)
			{
				// Annotations are not currently handled
			}
		});
	}

	@Override
	public String get()
	{
		return value.toString();
	}

	@Override
	public void set(String newValue)
	{
		try(CloseableLock lock = editor.lock())
		{
			LinkedList<Diff> diffs = DIFF.diff_main(value.toString(), newValue);
			if(diffs.size() > 2)
			{
				DIFF.diff_cleanupSemantic(diffs);
				DIFF.diff_cleanupEfficiency(diffs);
			}

			StringDelta<Operation<StringHandler>> builder = StringDelta.builder();
			for(Diff d : diffs)
			{
				switch(d.operation)
				{
					case EQUAL:
						builder.retain(d.text.length());
						break;
					case DELETE:
						builder.delete(d.text);
						break;
					case INSERT:
						builder.insert(d.text);
						break;
				}
			}

			editor.apply(builder.done());
		}
	}

	@Override
	public void append(String value)
	{
		try(CloseableLock lock = editor.lock())
		{
			int length = this.value.length();

			editor.apply(StringDelta.builder()
				.retain(length)
				.insert(value)
				.done()
			);
		}
	}

	@Override
	public void insert(int idx, String value)
	{
		try(CloseableLock lock = editor.lock())
		{
			int length = this.value.length();
			this.value.insert(idx, value);

			editor.apply(StringDelta.builder()
				.retain(idx)
				.insert(value)
				.retain(length - idx)
				.done()
			);
		}
	}

	@Override
	public void remove(int fromIndex, int toIndex)
	{
		try(CloseableLock lock = editor.lock())
		{
			int length = this.value.length();
			String deleted = this.value.substring(fromIndex, toIndex);

			editor.apply(StringDelta.builder()
				.retain(fromIndex)
				.delete(deleted)
				.retain(length - toIndex)
				.done()
			);
		}
	}
}
