import { User } from "oidc-client-ts";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

/**
 * Liest den aktuellen oidc-Benutzer (inkl. access_token) direkt aus dem Storage,
 * den react-oidc-context/oidc-client-ts unter einem bekannten Schluessel ablegt.
 */
function getAccessToken(): string | null {
  const authority = import.meta.env.VITE_OIDC_AUTHORITY;
  const clientId = import.meta.env.VITE_OIDC_CLIENT_ID;
  const storageKey = `oidc.user:${authority}:${clientId}`;
  const raw = window.localStorage.getItem(storageKey);
  if (!raw) {
    return null;
  }
  const user = User.fromStorageString(raw);
  return user.access_token ?? null;
}

/**
 * Zentraler fetch-Wrapper: haengt das Access-Token als `Authorization: Bearer ...`
 * an JEDE API-Anfrage an (User Story 10).
 */
export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const token = getAccessToken();
  const headers = new Headers(init.headers);
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  return fetch(`${API_BASE_URL}${path}`, { ...init, headers });
}

export interface Todo {
  id: number;
  owner: string;
  title: string;
  done: boolean;
}

export interface TodoInput {
  title: string;
  done?: boolean;
}

export async function fetchOwnTodos(): Promise<Todo[]> {
  const res = await apiFetch("/api/todos");
  if (!res.ok) {
    throw new Error(`GET /api/todos fehlgeschlagen: ${res.status}`);
  }
  return (await res.json()) as Todo[];
}

/**
 * Legt eine neue Todo an. Der Server setzt den `owner` aus dem Token; ein hier mitgesendeter
 * `owner` wuerde serverseitig ignoriert (siehe Backend TodoRequest).
 */
export async function createTodo(input: TodoInput): Promise<Todo> {
  const res = await apiFetch("/api/todos", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    throw new Error(`POST /api/todos fehlgeschlagen: ${res.status}`);
  }
  return (await res.json()) as Todo;
}

/**
 * Admin-Ansicht: ALLE Todos aller Benutzer. Der Endpunkt ist serverseitig auf Rolle `admin`
 * beschraenkt (HttpSecurity-Pfadregel + @PreAuthorize); ein `user`-Token erhaelt hier 403.
 */
export async function fetchAllTodos(): Promise<Todo[]> {
  const res = await apiFetch("/api/admin/todos");
  if (!res.ok) {
    throw new Error(`GET /api/admin/todos fehlgeschlagen: ${res.status}`);
  }
  return (await res.json()) as Todo[];
}

export async function updateTodo(id: number, input: TodoInput): Promise<Todo> {
  const res = await apiFetch(`/api/todos/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    throw new Error(`PUT /api/todos/${id} fehlgeschlagen: ${res.status}`);
  }
  return (await res.json()) as Todo;
}

export async function deleteTodo(id: number): Promise<void> {
  const res = await apiFetch(`/api/todos/${id}`, { method: "DELETE" });
  if (!res.ok) {
    throw new Error(`DELETE /api/todos/${id} fehlgeschlagen: ${res.status}`);
  }
}

export async function deleteAdminTodo(id: number): Promise<void> {
  const res = await apiFetch(`/api/admin/todos/${id}`, { method: "DELETE" });
  if (!res.ok) {
    throw new Error(`DELETE /api/admin/todos/${id} fehlgeschlagen: ${res.status}`);
  }
}
