# PRD: Lets-Code OWASP — Security-Training-Lab (sichere Baseline)

> Status: ready-for-agent
> Scope dieser PRD: **lauffähige, sichere Baseline-Anwendung.** Das absichtliche Einbauen von
> Sicherheitslücken ist eine spätere, separate Arbeit (siehe *Out of Scope*).

## Problem Statement

Für eine Security-Schulung (OWASP-Themen) wird eine kleine, realistische Beispielanwendung
benötigt, an der Teilnehmer später Sicherheitslücken finden, ausnutzen und fixen. Bestehende
Lehr-Apps sind entweder zu groß und unübersichtlich oder bilden keine echte OIDC-Anbindung mit
einem Identity Provider ab. Die Schulungsteilnehmer arbeiten auf einem gemischten Betriebssystem-
Park (macOS, Windows, Linux) und teils mit Podman statt Docker Desktop. Es braucht eine App, die
**bei jedem mit einem einzigen Befehl** end-to-end startet, ohne Host-Datei-Anpassungen oder
plattformspezifisches Gefrickel.

## Solution

Eine kleine Todo-Listen-Anwendung mit Benutzer- und Admin-Rolle, deren Authentifizierung über
Keycloak und OpenID Connect läuft. Ein React-Single-Page-Application-Frontend meldet den Benutzer
per Authorization-Code-Flow mit PKCE direkt bei Keycloak an und ruft eine Spring-Boot-API als
OAuth2 Resource Server mit dem Access-Token (Bearer) auf. Die gesamte Umgebung — Keycloak,
Backend, Frontend — startet über eine einzige `docker compose`-Datei. Keycloak wird beim Start mit
einem vorkonfigurierten Realm (Client, Rollen, Test-Benutzer) provisioniert, sodass nach
`docker compose up` sofort eingeloggt und gearbeitet werden kann.

Diese Baseline ist bewusst **funktional korrekt und sicher** umgesetzt, damit sie als saubere
Referenz dient. In Folge-Arbeiten wird sie gezielt verwundbar gemacht; der `solution`-Branch hält
die korrigierte Fassung.

## User Stories

1. Als Schulungsteilnehmer möchte ich die gesamte Umgebung mit einem einzigen `docker compose up`-Befehl starten, damit ich ohne Setup-Aufwand mit der Schulung beginnen kann.
2. Als Schulungsteilnehmer auf macOS möchte ich die App ohne Anpassung meiner `/etc/hosts`-Datei starten können, damit ich keine Adminrechte oder Host-Konfiguration brauche.
3. Als Schulungsteilnehmer auf Windows möchte ich dieselbe Umgebung identisch starten können, damit das Verhalten unabhängig vom Betriebssystem gleich ist.
4. Als Schulungsteilnehmer mit Podman statt Docker Desktop möchte ich die Compose-Datei unverändert nutzen können, damit ich keine container-engine-spezifischen Workarounds brauche.
5. Als Schulungsteilnehmer möchte ich, dass die gewählten Ports (3010/3011/3012) ungewöhnlich genug sind, damit sie nicht mit anderen lokal laufenden Diensten kollidieren.
6. Als unangemeldeter Benutzer möchte ich beim Aufruf der App auf die Keycloak-Anmeldeseite geleitet werden, damit ich mich authentifizieren kann.
7. Als registrierter Benutzer (`alice`) möchte ich mich über die Keycloak-Login-Maske anmelden, damit ich Zugriff auf meine Todos bekomme.
8. Als angemeldeter Benutzer möchte ich nach erfolgreichem Login zurück in die Anwendung geleitet werden, damit ich nahtlos weiterarbeiten kann.
9. Als angemeldeter Benutzer möchte ich mich abmelden können, damit meine Sitzung beendet wird.
10. Als angemeldeter Benutzer möchte ich, dass das Frontend mein Access-Token bei jeder API-Anfrage als Bearer-Token mitsendet, damit die API mich autorisieren kann.
11. Als Benutzer mit Rolle `user` möchte ich eine neue Todo mit Titel anlegen, damit ich meine Aufgaben festhalten kann.
12. Als Benutzer mit Rolle `user` möchte ich meine eigenen Todos auflisten, damit ich einen Überblick über meine Aufgaben habe.
13. Als Benutzer mit Rolle `user` möchte ich eine eigene Todo als erledigt markieren bzw. den Status umschalten, damit ich meinen Fortschritt abbilden kann.
14. Als Benutzer mit Rolle `user` möchte ich den Titel einer eigenen Todo ändern, damit ich Tippfehler korrigieren kann.
15. Als Benutzer mit Rolle `user` möchte ich eine eigene Todo löschen, damit ich erledigte oder überflüssige Aufgaben entfernen kann.
16. Als Benutzer mit Rolle `user` möchte ich ausschließlich meine eigenen Todos sehen und bearbeiten können, damit fremde Daten geschützt bleiben.
17. Als Benutzer `alice` möchte ich, dass mir die Todos von `bob` nicht angezeigt werden, damit die Datentrennung gewahrt ist.
18. Als Benutzer mit Rolle `user` möchte ich beim Zugriff auf eine fremde Todo per ID eine Fehlermeldung (kein Zugriff) erhalten, damit die Ownership-Prüfung greift.
19. Als Benutzer mit Rolle `admin` möchte ich alle Todos aller Benutzer einsehen, damit ich administrative Aufgaben wahrnehmen kann.
20. Als Benutzer mit Rolle `admin` möchte ich die administrativen Endpunkte aufrufen können, während Benutzer mit Rolle `user` daran gehindert werden, damit die Rollentrennung durchgesetzt wird.
21. Als Benutzer mit Rolle `user` möchte ich beim Aufruf eines Admin-Endpunkts abgewiesen werden (403), damit die Funktionsebenen-Autorisierung greift.
22. Als Trainer möchte ich vorkonfigurierte Test-Benutzer (`alice`, `bob`, `admin`) mit bekannten Passwörtern haben, damit ich Szenarien ohne manuelle Benutzerverwaltung vorführen kann.
23. Als Trainer möchte ich zwei normale Benutzer (`alice`, `bob`) haben, damit ich Datentrennungs- und spätere IDOR-Szenarien demonstrieren kann.
24. Als Trainer möchte ich, dass der Keycloak-Realm versioniert im Repo liegt und automatisch importiert wird, damit jeder Teilnehmer denselben reproduzierbaren Stand hat.
25. Als Teilnehmer möchte ich, dass nach einem Neustart der Umgebung ein sauberer Ausgangszustand (frische In-Memory-Daten, re-importierter Realm) vorliegt, damit jede Übung von vorn beginnen kann.
26. Als Teilnehmer möchte ich Änderungen am Frontend-Code live sehen (Hot-Reload), damit ich im Lab schnell iterieren kann.
27. Als Teilnehmer möchte ich, dass das Backend nach einer Code-Änderung automatisch neu startet, damit ich beim Einbauen von Code keine manuellen Rebuilds anstoßen muss.
28. Als Entwickler/Trainer möchte ich, dass das Backend gültige Tokens akzeptiert und ungültige/abgelaufene/falsch signierte Tokens ablehnt, damit die Resource-Server-Validierung verlässlich ist.
29. Als Entwickler möchte ich, dass das Backend die Token-Rollen korrekt aus `realm_access.roles` in Spring-Authorities übersetzt, damit Autorisierungsregeln greifen.
30. Als Entwickler möchte ich, dass die `iss`-Validierung gegen die browser-sichtbare Keycloak-URL erfolgt, während die Schlüssel (JWKS) intern über den Container-Servicenamen geladen werden, damit das Docker-Hostname-Problem ohne Host-Datei-Eingriff gelöst ist.
31. Als neuer Teilnehmer möchte ich eine README mit Start-Anleitung, URLs, Ports und Test-Zugangsdaten haben, damit ich ohne Rückfragen loslegen kann.

## Implementation Decisions

### Gesamtarchitektur
- **SPA als Public OIDC-Client** mit Authorization-Code-Flow + PKCE. Bewusst **keine** Backend-for-Frontend-Variante. Access-Token lebt im Browser. Diese (für Produktion unsichere) Wahl ist didaktisch gewollt und ermöglicht spätere Token-Diebstahl-Szenarien.
- Das Backend ist ein **reiner OAuth2 Resource Server** und validiert eingehende JWTs; es führt selbst keinen Login-Flow.

### Frontend-Modul
- Vite + React 19 + TypeScript, bewusst **plain CSS** (kein UI-Framework), um die Aufmerksamkeit auf Auth-/Autorisierungslogik zu lenken.
- OIDC-Anbindung über `react-oidc-context` (basierend auf `oidc-client-ts`).
- **Token-Persistenz in `localStorage`** (über den `WebStorageStateStore`). Bewusste, für spätere XSS-Übungen relevante Entscheidung.
- Ein zentraler `fetch`-Wrapper hängt das Access-Token als `Authorization: Bearer …` an alle API-Aufrufe an.
- UI-Umfang: Login/Logout, Todo-Liste der eigenen Einträge, Anlegen/Umschalten/Bearbeiten/Löschen, sowie eine nur für die Admin-Rolle sichtbare Admin-Ansicht (alle Todos).

### Backend-Modul
- Spring Boot 4.1.0, Java 21, Gradle (Kotlin-DSL). Starter: `oauth2-resource-server`, `web`, H2 (in-memory).
- **Custom `JwtDecoder`-Bean** zur Entkopplung von JWKS-Quelle und Issuer-Validierung. Per Konfiguration allein nicht abbildbar (entweder `issuer-uri` *oder* `jwk-set-uri`). Die Entscheidung in Kürze:

  ```
  JWKS laden:        http://keycloak:3010/realms/lab/protocol/openid-connect/certs   (intern, Servicename)
  iss validieren:    http://localhost:3010/realms/lab                                 (browser-sichtbare URL)
  ```

  Dadurch entfällt jeder `/etc/hosts`-Eingriff, und Browser wie Backend sehen ein konsistentes Token.
- **`JwtAuthenticationConverter`** übersetzt `realm_access.roles` in Spring-Authorities (`ROLE_user`, `ROLE_admin`).
- **Zweistufiger Schutz:** grobe Pfadregeln in `HttpSecurity` (`/api/admin/**` → Rolle `admin`, übrige `/api/**` → authentifiziert) **und** Ownership-/Rollenprüfungen auf Service-/Methodenebene (`@PreAuthorize` bzw. expliziter Besitzabgleich). Diese beiden Stellen sind die späteren Vuln-Ansatzpunkte (Broken Function Level Authorization zentral, IDOR im Service).
- **Domänenmodell:** Entität `Todo` mit `id`, `owner` (Keycloak-Subject/Username), `title`, `done`. Persistenz in H2 in-memory; Seed-Daten optional pro Test-Benutzer.
- **API-Vertrag:**
  - `GET /api/todos` — Todos des aktuellen Benutzers
  - `POST /api/todos` — neue Todo des aktuellen Benutzers (Server setzt `owner` aus dem Token, nicht aus dem Request-Body)
  - `GET /api/todos/{id}` — einzelne Todo, nur bei Besitz
  - `PUT /api/todos/{id}` — Aktualisieren (Titel/Status), nur bei Besitz
  - `DELETE /api/todos/{id}` — Löschen, nur bei Besitz
  - `GET /api/admin/todos` — alle Todos, nur Rolle `admin`
- CORS ist für den Frontend-Origin (`http://localhost:3012`) freigegeben.

### Keycloak-Provisionierung
- Keycloak 26.x im `start-dev`-Modus mit `--import-realm`, eingebettete H2.
- Versionierte `keycloak/realm-export.json` definiert: Realm `lab`, einen Public-Client mit PKCE (S256), gültige Redirect-URIs und Web-Origins auf `http://localhost:3012`, Realm-Rollen `user` und `admin`, sowie die Test-Benutzer:
  - `alice` / `alice` → Rolle `user`
  - `bob` / `bob` → Rolle `user`
  - `admin` / `admin` → Rolle `admin`
- Klartext-Passwörter sind als reines Lab bewusst gewählt.

### Infrastruktur / docker compose
- **Bridge-Netzwerk** (Compose-Default) statt `network_mode: host` — verhält sich auf macOS, Windows, Linux und Podman identisch.
- Drei Services: `keycloak` (Port 3010), `backend` (3011), `frontend` (3012).
- **Frontend:** Quellverzeichnis als Bind-Mount, `vite dev --host` → Hot-Reload.
- **Backend:** Ausführung über `gradle bootRun` mit Spring DevTools und Source-Mount → automatischer Restart bei Code-Änderung.
- Ziel: `docker compose up` liefert eine end-to-end lauffähige Umgebung ohne weitere Schritte.

### Repository-Layout
```
docker-compose.yml
README.md
backend/                Spring Boot (Gradle, build.gradle.kts, Dockerfile)
frontend/               Vite React TS (Dockerfile)
keycloak/realm-export.json
docs/PRD.md
```

### Branch-Strategie (Vuln-Modell B, Juice-Shop-Stil)
- Heute entsteht die **sichere Baseline**.
- Später wird `main` gezielt verwundbar gemacht; der `solution`-Branch hält die gefixte Referenz.

## Testing Decisions

- **Was einen guten Test ausmacht:** Es wird ausschließlich **externes Verhalten** an der HTTP-Grenze geprüft (Statuscode, Antwort-Body, Sichtbarkeit von Daten), nicht die interne Implementierung. Keine Tests gegen private Methoden, Repository-Interna oder konkrete SQL-Abfragen.
- **Einzige, höchste Seam:** das **Backend-HTTP-API**, getestet mit Spring `MockMvc` und `spring-security-test`. JWTs werden über den `jwt()`-Request-Post-Processor simuliert (inklusive `realm_access.roles`), sodass Authentifizierung und Autorisierung ohne laufenden Keycloak und ohne Netzwerk geprüft werden. Diese Seam liegt genau dort, wo die späteren Sicherheitslücken sitzen.
- **Abzudeckendes Verhalten (Beispiele):**
  - Anonyme Anfrage an `/api/todos` → 401.
  - `user`-Token sieht nur eigene Todos; Zugriff auf fremde Todo-ID → 403/404.
  - `POST /api/todos` setzt `owner` aus dem Token, ignoriert einen abweichenden `owner` im Body.
  - `user`-Token an `/api/admin/todos` → 403; `admin`-Token → 200 mit allen Todos.
  - Ungültig signiertes / abgelaufenes / falscher-Issuer-Token → 401.
- **Prior Art:** Im Greenfield-Repo existiert noch keine. Die Tests etablieren das Muster „Spring-Security-Resource-Server-Integrationstest mit simuliertem JWT" als Referenz für künftige Tests.
- **Frontend-Tests** sind für die Baseline nicht vorgesehen (manuelle Verifikation im Lab).

## Out of Scope

- Das absichtliche Einbauen konkreter Sicherheitslücken (IDOR/BOLA, Broken Function Level Authorization, Stored XSS mit Token-Diebstahl, Mass Assignment, deaktivierte `iss`-Validierung u. a.) — separate Folge-Arbeit, getrieben durch eine eigene Vuln-Liste.
- Backend-for-Frontend-Architektur, In-Memory-Token-Haltung, Silent-Token-Renewal — bewusst nicht, da der unsichere SPA-Ansatz Lernziel ist.
- Persistente Datenbanken / Volumes (H2 in-memory ist gewollt, Auto-Reset ist ein Feature).
- TLS/HTTPS, produktionsnahe Keycloak-Konfiguration, externe IdPs.
- Automatisierte Frontend-Tests und End-to-End-Browser-Tests.
- Deployment außerhalb der lokalen `docker compose`-Umgebung.

## Further Notes

- Zielplattformen sind ausdrücklich gemischt: macOS, Windows, Linux, Docker Desktop **und** Podman. Jede Entscheidung gegen `network_mode: host` und gegen `/etc/hosts`-Eingriffe folgt diesem Portabilitätsziel.
- Die ungewöhnlichen Ports (3010/3011/3012) dienen der Kollisionsvermeidung in Teilnehmer-Umgebungen.
- Der custom `JwtDecoder` (interne JWKS-URL, externe Issuer-Validierung) ist nicht nur eine technische Notwendigkeit, sondern später selbst Schulungsstoff (Was passiert, wenn man die `iss`-Validierung abschaltet?).
- Mehrere bewusst „unsichere" Baseline-Entscheidungen (Token in `localStorage`, Public Client, Klartext-Lab-Passwörter) sind als solche dokumentiert, damit sie nicht versehentlich als Bug „korrigiert" werden.
