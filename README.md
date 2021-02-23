# Otter

Otter is a library to support collaborative realtime editing using
[Operational Transformation](https://en.wikipedia.org/wiki/Operational_transformation).
This repository contains the Java-implementation.

## Using Otter

Otter consists of three parts, the operations library, the editing engine and
a high level model. The high level model is what you usually want to use
unless you are implementing something special.

### Operations

The lowest level of Otter is the operational transformation algorithms. Otter
supports transformations on maps, lists and strings. There is also a combined
type that can be used to combine several other types based on unique
identifiers. All of these transformations are used together to create the
higher level model.

### Engine

The engine contains editing control. It provides support for creating
editors on top of any supported operational transformation.


```java
OperationSync<Operation<StringHandler>> sync = new YourOperationSync(new StringType(), ...);
Editor<Operation<StringHandler>> editor = new DefaultEditor<>(uniqueSessionId, sync);

// Get the initial content and register a listener
try(CloseableLock lock = editor.lock()) {
  editor.getCurrent().apply( ... );
  
  editor.addEditorListener(new EditorEventHandler());
}

// Perform an operation
try(CloseableLock lock = editor.lock()) {
  // Use a lock to safely be able to create a delta
  editor.apply(StringDelta.builder()
    .retain(currentStringLength)
    .insert("abc")
    .done()
  );
}
```

Editors require a synchronization helper for sending and receiving operations
from a server. There is intentionally no default implementation of such a sync
as different applications will have different requirements here.

In the end all operations performed by an editor will end up being handled by
an instance of `EditorControl`.

```java
EditorControl control = new DefaultEditorControl(historyStorage);

// When a new editor connects you can get the latest version:
control.getLatest();

// When an operation is received from a client it needs to be stored and
// the result needs to be sent back to all clients
TaggedOperation op = control.store(taggedOperation);
```

### Model

This is the high level API that makes it easier to work with shared editing.
The model provides shared objects of different types that are synchronized
between all editors of the model.

Here is a tiny example of working with the model:

```java
Editor editor = new DefaultEditor(uniqueSessionId, sync); 
Model model = Model.builder(editor)
  .build();

// Create a new string and store it in the root map
SharedString title = model.newString();
title.set("Cookies are tasty");
model.set("title", title);

// Set a primitive value in the map
model.set("priority", 10);
```
