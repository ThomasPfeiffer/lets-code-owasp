# 01 — Walking Skeleton: Login + eigene Todos auflisten (end-to-end)

## What to build

Die dünnste vollständige Bahn durch alle Schichten der App. Ein einziger `docker compose up`
bringt Keycloak, Backend und Frontend hoch; ein Teilnehmer ruft das Frontend auf, wird zur
Keycloak-Anmeldemaske geleitet, meldet sich als `alice` an, wird in die App zurückgeleitet und
sieht seine eigene (geseedete) Todo-Liste. Logout beendet die Sitzung.

Diese Slice etabliert die Gesamtarchitektur und das Test-Pattern, auf das alle folgenden Slices
aufbauen.

Umfang:

- **Infrastruktur:** `docker-compose.yml` mit drei Services auf dem Compose-Default-Bridge-Netzwerk
  (kein `network_mode: host`, keine `/etc/hosts`-Eingriffe). Ports: Keycloak `3010`, Backend `3011`,
  Frontend `3012`. Verhalten identisch auf macOS, Windows, Linux, Docker Desktop und Podman.
- **Keycloak-Provisionierung:** Keycloak 26.x im `start-dev`-Modus mit `--import-realm` und
  eingebetteter H2. Versionierte `keycloak/realm-export.json` definiert Realm `lab`, einen
  Public-Client mit PKCE (S256), gültige Redirect-URIs / Web-Origins auf `http://localhost:3012`,
  Realm-Rollen `user` und `admin` sowie die Test-Benutzer `alice`/`alice` (user), `bob`/`bob` (user),
  `admin`/`admin` (admin). Klartext-Passwörter sind als Lab bewusst gewählt.
- **Frontend:** Vite + React 19 + TypeScript, plain CSS. OIDC-Anbindung über `react-oidc-context`
  (Authorization-Code-Flow + PKCE), Token-Persistenz in `localStorage` (`WebStorageStateStore`).
  Zentraler `fetch`-Wrapper hängt das Access-Token als `Authorization: Bearer …` an alle API-Aufrufe.
  UI: Login, Logout, Anzeige der eigenen Todo-Liste.
- **Backend:** Spring Boot 4.1.0, Java 21, Gradle (Kotlin-DSL), Starter `oauth2-resource-server`,
  `web`, H2 (in-memory). Reiner OAuth2 Resource Server (kein eigener Login-Flow). Custom
  `JwtDecoder`-Bean lädt JWKS intern über den Servicenamen
  (`http://keycloak:3010/realms/lab/protocol/openid-connect/certs`) und validiert `iss` gegen die
  browser-sichtbare URL (`http://localhost:3010/realms/lab`). `JwtAuthenticationConverter` übersetzt
  `realm_access.roles` in Spring-Authorities (`ROLE_user`, `ROLE_admin`). `HttpSecurity`: `/api/**`
  erfordert Authentifizierung. Domänenentität `Todo` (`id`, `owner`, `title`, `done`) in H2
  in-memory mit Seed-Daten pro Test-Benutzer. Endpunkt `GET /api/todos` liefert nur die Todos des
  aktuellen Benutzers (`owner` aus dem Token-Subject/Username). CORS für Origin
  `http://localhost:3012` freigegeben.
- **Auto-Reset:** Durch H2 in-memory + `--import-realm` ergibt jeder Neustart automatisch einen
  frischen Ausgangszustand.
- **Test-Pattern (etablierend):** Backend-Integrationstests an der HTTP-Grenze mit Spring `MockMvc`
  und `spring-security-test`; JWTs werden über den `jwt()`-Request-Post-Processor simuliert
  (inkl. `realm_access.roles`), ohne laufenden Keycloak und ohne Netzwerk. Es wird ausschließlich
  externes Verhalten geprüft (Statuscode, Body, Sichtbarkeit von Daten).

## Acceptance criteria

- [ ] `docker compose up` startet Keycloak, Backend und Frontend end-to-end ohne weitere Schritte und ohne Host-Datei-Anpassungen.
- [ ] Ports 3010 (Keycloak), 3011 (Backend), 3012 (Frontend) sind erreichbar; Verhalten ist auf Docker Desktop und Podman identisch.
- [ ] Der Realm `lab` wird beim Start automatisch aus `keycloak/realm-export.json` importiert (Public-Client mit PKCE, Rollen `user`/`admin`, Benutzer `alice`/`bob`/`admin`).
- [ ] Aufruf des Frontends als nicht angemeldeter Benutzer leitet zur Keycloak-Anmeldemaske.
- [ ] Login als `alice` leitet nach Erfolg in die App zurück; das Access-Token liegt im `localStorage`.
- [ ] Logout beendet die Sitzung.
- [ ] Das Frontend sendet das Access-Token bei jeder API-Anfrage als `Authorization: Bearer …`.
- [ ] `GET /api/todos` liefert ausschließlich die Todos des aktuellen Benutzers; `alice` sieht nicht die Todos von `bob`.
- [ ] Das Backend lädt JWKS intern über den Servicenamen, validiert `iss` aber gegen die browser-sichtbare URL; ein Login funktioniert ohne `/etc/hosts`-Eingriff.
- [ ] `realm_access.roles` werden korrekt in `ROLE_user` / `ROLE_admin` übersetzt.
- [ ] Anonyme Anfrage an `/api/todos` → 401.
- [ ] Ungültig signiertes, abgelaufenes oder Token mit falschem Issuer → 401.
- [ ] Ein Neustart der Umgebung ergibt einen frischen Ausgangszustand (frische In-Memory-Daten, re-importierter Realm).
- [ ] Backend-Integrationstests mit `MockMvc` + simuliertem `jwt()` decken die obigen Verhaltensweisen ab und etablieren das Test-Muster.

## Blocked by

- None - can start immediately
