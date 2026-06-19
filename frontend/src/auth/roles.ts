import type { User } from "oidc-client-ts";

/**
 * Liest die Keycloak-Realm-Rollen aus dem Access-Token (`realm_access.roles`).
 *
 * <p>Keycloak legt die Realm-Rollen im Access-Token ab, nicht zwingend im ID-Token-Profil.
 * Daher dekodieren wir hier den (nicht signaturgeprueften) Payload des Access-Tokens rein zur
 * UI-Steuerung. Die eigentliche, sicherheitsrelevante Rollenpruefung passiert ausschliesslich
 * serverseitig — diese Frontend-Logik blendet die Admin-Ansicht lediglich ein oder aus.
 */
export function realmRolesFromUser(user: User | null | undefined): string[] {
  const token = user?.access_token;
  if (!token) {
    return [];
  }
  const payload = decodeJwtPayload(token);
  if (!payload) {
    return [];
  }
  const realmAccess = payload["realm_access"];
  if (
    realmAccess &&
    typeof realmAccess === "object" &&
    Array.isArray((realmAccess as { roles?: unknown }).roles)
  ) {
    return ((realmAccess as { roles: unknown[] }).roles).map((r) => String(r));
  }
  return [];
}

export function hasRole(user: User | null | undefined, role: string): boolean {
  return realmRolesFromUser(user).includes(role);
}

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  const parts = token.split(".");
  if (parts.length < 2) {
    return null;
  }
  try {
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    );
    return JSON.parse(json) as Record<string, unknown>;
  } catch {
    return null;
  }
}
