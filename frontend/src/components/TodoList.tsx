import { useEffect, useState } from "react";
import {
  createTodo,
  deleteTodo,
  fetchOwnTodos,
  updateTodo,
  type Todo,
} from "../api/client";

export function TodoList() {
  const [todos, setTodos] = useState<Todo[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [newTitle, setNewTitle] = useState("");
  const [busy, setBusy] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editTitle, setEditTitle] = useState("");

  useEffect(() => {
    let cancelled = false;
    fetchOwnTodos()
      .then((data) => {
        if (!cancelled) {
          setTodos(data);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(toMessage(e));
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function run(action: () => Promise<void>) {
    setBusy(true);
    setError(null);
    try {
      await action();
    } catch (e: unknown) {
      setError(toMessage(e));
    } finally {
      setBusy(false);
    }
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    const title = newTitle.trim();
    if (!title) {
      return;
    }
    await run(async () => {
      const created = await createTodo({ title });
      setTodos((prev) => [...(prev ?? []), created]);
      setNewTitle("");
    });
  }

  async function handleToggle(todo: Todo) {
    await run(async () => {
      const updated = await updateTodo(todo.id, { title: todo.title, done: !todo.done });
      setTodos((prev) => (prev ?? []).map((t) => (t.id === updated.id ? updated : t)));
    });
  }

  function startEdit(todo: Todo) {
    setEditingId(todo.id);
    setEditTitle(todo.title);
  }

  function cancelEdit() {
    setEditingId(null);
    setEditTitle("");
  }

  async function handleSaveEdit(todo: Todo) {
    const title = editTitle.trim();
    if (!title) {
      return;
    }
    await run(async () => {
      const updated = await updateTodo(todo.id, { title, done: todo.done });
      setTodos((prev) => (prev ?? []).map((t) => (t.id === updated.id ? updated : t)));
      cancelEdit();
    });
  }

  async function handleDelete(todo: Todo) {
    await run(async () => {
      await deleteTodo(todo.id);
      setTodos((prev) => (prev ?? []).filter((t) => t.id !== todo.id));
      if (editingId === todo.id) {
        cancelEdit();
      }
    });
  }

  return (
    <div className="todos">
      <form className="new-todo" onSubmit={handleCreate}>
        <input
          type="text"
          placeholder="Neue Todo …"
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
          disabled={busy}
          aria-label="Titel der neuen Todo"
        />
        <button className="primary" type="submit" disabled={busy || newTitle.trim() === ""}>
          Anlegen
        </button>
      </form>

      {error && <p className="error">Fehler: {error}</p>}

      {todos === null ? (
        <p>Lade Todos …</p>
      ) : todos.length === 0 ? (
        <p className="muted">Noch keine Todos vorhanden.</p>
      ) : (
        <ul className="todo-list">
          {todos.map((todo) => (
            <li key={todo.id} className={todo.done ? "done" : ""}>
              <button
                className="status-btn"
                type="button"
                onClick={() => void handleToggle(todo)}
                disabled={busy}
                aria-label={todo.done ? "Als offen markieren" : "Als erledigt markieren"}
                title={todo.done ? "Als offen markieren" : "Als erledigt markieren"}
              >
                {todo.done ? "✓" : "○"}
              </button>

              {editingId === todo.id ? (
                <>
                  <input
                    className="edit-input"
                    type="text"
                    value={editTitle}
                    onChange={(e) => setEditTitle(e.target.value)}
                    disabled={busy}
                    aria-label="Titel bearbeiten"
                    autoFocus
                  />
                  <div className="actions">
                    <button
                      type="button"
                      onClick={() => void handleSaveEdit(todo)}
                      disabled={busy || editTitle.trim() === ""}
                    >
                      Speichern
                    </button>
                    <button type="button" onClick={cancelEdit} disabled={busy}>
                      Abbrechen
                    </button>
                  </div>
                </>
              ) : (
                <>
                  <span className="title">{todo.title}</span>
                  <div className="actions">
                    <button type="button" onClick={() => startEdit(todo)} disabled={busy}>
                      Bearbeiten
                    </button>
                    <button
                      type="button"
                      className="danger"
                      onClick={() => void handleDelete(todo)}
                      disabled={busy}
                    >
                      Löschen
                    </button>
                  </div>
                </>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function toMessage(e: unknown): string {
  return e instanceof Error ? e.message : String(e);
}
