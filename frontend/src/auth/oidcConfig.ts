import { WebStorageStateStore } from "oidc-client-ts";
import type { AuthProviderProps } from "react-oidc-context";

// OIDC-Konfiguration der SPA als PUBLIC Client mit Authorization-Code-Flow + PKCE.
//
// Bewusste Lab-Entscheidung (didaktisch, fuer spaetere XSS-/Token-Diebstahl-Uebungen):
// Token-Persistenz in localStorage statt In-Memory. Siehe PRD.
export const oidcConfig: AuthProviderProps = {
  authority: import.meta.env.VITE_OIDC_AUTHORITY,
  client_id: import.meta.env.VITE_OIDC_CLIENT_ID,
  redirect_uri: import.meta.env.VITE_OIDC_REDIRECT_URI,
  // Authorization-Code-Flow + PKCE (oidc-client-ts macht PKCE bei response_type=code automatisch).
  response_type: "code",
  scope: "openid profile email",
  // Token landen im localStorage.
  userStore: new WebStorageStateStore({ store: window.localStorage }),
  // Nach dem Login die Auth-Parameter (?code=...&state=...) aus der URL entfernen.
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};
