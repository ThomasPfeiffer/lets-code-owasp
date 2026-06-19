import { useEffect, useState } from "react";
import { fetchAllTodos, type Todo } from "../api/client";

/**
 * Admin-Ansicht: listet ALLE Todos ALLER Benutzer (read-only).
 *
 * <p>Wird ausschliesslich fuer Benutzer mit Rolle `admin` gerendert (siehe App.tsx). Die
 * Sichtbarkeit hier ist nur UI-Komfort; der Endpunkt `/api/admin/todos` ist serverseitig
 * auf die Rolle `admin` beschraenkt und liefert einem `user`-Token 403.
 */
export function AdminTodos() {
  const [todos, setTodos] = useState<Todo[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchAllTodos()
      .then((data) => {
        if (!cancelled) {
          setTodos(data);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : String(e));
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="todos">
      <p className="muted">
        Administrative Gesamtansicht — alle Todos aller Benutzer (nur Rolle <code>admin</code>).
      </p>

      {error && <p className="error">Fehler: {error}</p>}

      {todos === null ? (
        <p>Lade Todos …</p>
      ) : todos.length === 0 ? (
        <p className="muted">Keine Todos vorhanden.</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Besitzer</th>
              <th>Titel</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {todos.map((todo) => (
              <tr key={todo.id} className={todo.done ? "done" : ""}>
                <td>{todo.id}</td>
                <td>
                  <strong>{todo.owner}</strong>
                </td>
                <td>{todo.title}</td>
                <td>{todo.done ? "erledigt" : "offen"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
