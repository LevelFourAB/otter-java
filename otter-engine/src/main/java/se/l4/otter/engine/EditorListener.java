package se.l4.otter.engine;

import se.l4.otter.engine.events.ChangeEvent;
import se.l4.otter.operations.Operation;

/**
 * Listener used by {@link Editor}.
 *
 * @author Andreas Holstenson
 *
 */
public interface EditorListener<Op extends Operation<?>>
{
	/**
	 * The editor has applied either a remote or local event.
	 *
	 * @param event
	 */
	void editorChanged(ChangeEvent<Op> event);
}
