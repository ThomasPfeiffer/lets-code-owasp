# 04 — Dev-Experience: Hot-Reload (Frontend) + Auto-Restart (Backend)

## What to build

Im Lab sollen Teilnehmer schnell iterieren können. Änderungen am Frontend-Code werden live sichtbar
(Hot-Reload), und das Backend startet nach einer Code-Änderung automatisch neu — ohne manuelle
Rebuilds. Beides läuft innerhalb der bestehenden `docker compose`-Umgebung.

Umfang:

- **Frontend:** Quellverzeichnis als Bind-Mount in den Container, Ausführung über `vite dev --host`,
  sodass Änderungen am Frontend-Code per HMR live im Browser erscheinen.
- **Backend:** Ausführung über `gradle bootRun` mit Spring DevTools und Source-Mount, sodass eine
  Code-Änderung einen automatischen Restart des Backends auslöst.

## Acceptance criteria

- [ ] Eine Änderung an einer Frontend-Quelldatei wird ohne manuellen Rebuild live im Browser sichtbar.
- [ ] Eine Änderung an einer Backend-Quelldatei löst einen automatischen Restart des Backends aus (Spring DevTools).
- [ ] Beides funktioniert mit der unveränderten `docker compose up`-Umgebung.

## Blocked by

- 01 — Walking Skeleton: Login + eigene Todos auflisten
