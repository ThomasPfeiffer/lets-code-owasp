# Lets-Code OWASP — Security-Training-Lab (sichere Baseline)

Eine kleine Todo-App mit Keycloak-Login (OIDC, Authorization-Code-Flow + PKCE), einem
Spring-Boot-Backend als OAuth2 Resource Server und einem React-SPA-Frontend. Sie dient als
saubere, **bewusst sichere** Referenz für eine OWASP-Schulung. In Folge-Arbeiten wird sie gezielt
verwundbar gemacht (siehe `docs/PRD.md`).

Funktionsumfang der Baseline:

- Login / Logout über Keycloak (Authorization-Code-Flow + PKCE).
- Eigene Todos auflisten, anlegen, umschalten (erledigt/offen), Titel ändern, löschen.
- Strikte Datentrennung: jeder Benutzer sieht und bearbeitet nur seine eigenen Todos.
- Admin-Ansicht (nur Rolle `admin`): alle Todos aller Benutzer.
- Hot-Reload im Frontend und Auto-Restart im Backend für Live-Coding im Lab.

## Schnellstart

Voraussetzung: **Docker Desktop** *oder* **Podman**.

```bash
docker compose up
```

(Mit Podman analog: `podman compose up`.)

Das ist der **einzige** nötige Befehl — Keycloak, Backend und Frontend starten end-to-end.

Danach erreichbar:

| Dienst    | URL                          | Port |
|-----------|------------------------------|------|
| Frontend  | http://localhost:3012        | 3012 |
| Backend   | http://localhost:3011        | 3011 |
| Keycloak  | http://localhost:3010        | 3010 |

### Test-Zugangsdaten

Vorkonfiguriert im Realm — kein manuelles Anlegen nötig:

| Benutzer | Passwort | Rolle   | Zweck                                          |
|----------|----------|---------|------------------------------------------------|
| `alice`  | `alice`  | `user`  | normaler Benutzer                              |
| `bob`    | `bob`    | `user`  | zweiter Benutzer (Datentrennung / IDOR-Demos)  |
| `admin`  | `admin`  | `admin` | sieht alle Todos, darf Admin-Endpunkte         |

Keycloak-Admin-Konsole: http://localhost:3010 (admin / admin).



## Architektur in Kürze

- **Frontend** (Vite + React 19 + TS, plain CSS): meldet den Benutzer per `react-oidc-context`
  direkt bei Keycloak an. Das Access-Token liegt im `localStorage` (bewusste Lab-Entscheidung).
  Ein zentraler `fetch`-Wrapper hängt es als `Authorization: Bearer …` an jede API-Anfrage.
- **Backend** (Spring Boot 4.1 / Java 21, Gradle Kotlin-DSL): reiner OAuth2 Resource Server.
- **Keycloak 26** im `start-dev`-Modus mit `--import-realm`.


## Auto-Reset

Sowohl die Backend-Daten (H2 in-memory) als auch der Keycloak-Realm (`--import-realm`) werden bei
jedem Neustart frisch aufgesetzt. `docker compose down && docker compose up` ergibt einen sauberen
Ausgangszustand — ideal, um eine Übung von vorn zu beginnen.

## Hot-Reload / Live-Coding

- **Frontend:** `frontend/src` ist als Bind-Mount eingebunden; der Vite-Dev-Server lädt Änderungen
  sofort nach.
- **Backend:** `backend/src` ist gemountet; Gradle baut kontinuierlich und Spring DevTools startet
  die App bei Code-Änderungen automatisch neu.


## API-Endpunkte (Backend)

Alle unter `http://localhost:3011`, geschützt per Bearer-Access-Token:

| Methode  | Pfad                | Wer            | Wirkung                                  |
|----------|---------------------|----------------|------------------------------------------|
| `GET`    | `/api/todos`        | `user`         | eigene Todos auflisten                   |
| `POST`   | `/api/todos`        | `user`         | eigene Todo anlegen (owner aus Token)    |
| `GET`    | `/api/todos/{id}`   | `user`         | eigene Todo lesen (sonst 404)            |
| `PUT`    | `/api/todos/{id}`   | `user`         | eigene Todo ändern (sonst 404)           |
| `DELETE` | `/api/todos/{id}`   | `user`         | eigene Todo löschen (sonst 404)          |
| `GET`    | `/api/admin/todos`  | `admin`        | alle Todos aller Benutzer (sonst 403)    |

Anonyme Anfragen → 401. Ein `user`-Token an `/api/admin/**` → 403.