import { useState } from "react";
import { useAuth } from "react-oidc-context";
import { TodoList } from "./components/TodoList";
import { AdminTodos } from "./components/AdminTodos";
import { hasRole } from "./auth/roles";

type View = "own" | "admin";

const OIDC_AUTHORITY = import.meta.env.VITE_OIDC_AUTHORITY;
const REDIRECT_URI = import.meta.env.VITE_OIDC_REDIRECT_URI;

export function App() {
  const auth = useAuth();
  const [view, setView] = useState<View>("own");

  if (auth.isLoading) {
    return (
      <main className="container">
        <p>Lade Sitzung …</p>
      </main>
    );
  }

  if (auth.error) {
    return (
      <main className="container">
        <h1>Anmeldefehler</h1>
        <p className="error">{auth.error.message}</p>
        <button onClick={() => void auth.signinRedirect()}>Erneut anmelden</button>
      </main>
    );
  }

  // Nicht angemeldet -> direkt zur Keycloak-Anmeldemaske leiten.
  if (!auth.isAuthenticated) {
    return (
      <main className="container">
        <h1>Lets-Code OWASP — Todo Lab</h1>
        <p>Bitte melde dich an, um deine Todos zu sehen.</p>
        <button className="primary" onClick={() => void auth.signinRedirect()}>
          Anmelden
        </button>
      </main>
    );
  }

  const username =
    (auth.user?.profile.preferred_username as string | undefined) ??
    auth.user?.profile.sub ??
    "unbekannt";

  // Admin-Rolle aus dem Access-Token (realm_access.roles). Steuert nur die UI-Sichtbarkeit;
  // die verbindliche Pruefung erfolgt serverseitig am Endpunkt /api/admin/todos.
  const isAdmin = hasRole(auth.user, "admin");
  const activeView: View = isAdmin ? view : "own";

  // End-Session beim IdP, danach zurueck zur App. Loescht ausserdem den lokalen User.
  const logout = () => {
    void auth.signoutRedirect({
      post_logout_redirect_uri: REDIRECT_URI,
      // Falls der Realm keine End-Session-Bestaetigung verlangt, reicht der lokale Cleanup.
    });
  };

  return (
    <main className="container">
      <header className="topbar">
        <h1>{activeView === "admin" ? "Alle Todos (Admin)" : "Meine Todos"}</h1>
        <div className="user">
          <span>
            Angemeldet als <strong>{username}</strong>
          </span>
          <button onClick={logout}>Abmelden</button>
        </div>
      </header>
      <p className="muted">
        Identity Provider: <code>{OIDC_AUTHORITY}</code>
      </p>

      {/* Admin-Umschalter: nur fuer Benutzer mit Rolle admin sichtbar. */}
      {isAdmin && (
        <nav className="view-switch" aria-label="Ansicht wechseln">
          <button
            type="button"
            className={activeView === "own" ? "primary" : ""}
            onClick={() => setView("own")}
            aria-pressed={activeView === "own"}
          >
            Meine Todos
          </button>
          <button
            type="button"
            className={activeView === "admin" ? "primary" : ""}
            onClick={() => setView("admin")}
            aria-pressed={activeView === "admin"}
          >
            Admin: Alle Todos
          </button>
        </nav>
      )}

      {activeView === "admin" ? <AdminTodos /> : <TodoList />}
    </main>
  );
}
