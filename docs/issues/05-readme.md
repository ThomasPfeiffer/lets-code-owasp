# 05 — README mit Start-Anleitung, URLs, Ports und Test-Zugangsdaten

## What to build

Ein neuer Teilnehmer soll ohne Rückfragen loslegen können. Eine `README.md` im Repo-Root
dokumentiert den Start, die relevanten URLs/Ports und die Test-Zugangsdaten — und benennt die
bewusst „unsicheren" Baseline-Entscheidungen als gewollt, damit sie nicht versehentlich als Bug
„korrigiert" werden.

Umfang:

- Start-Anleitung: `docker compose up` als einziger Befehl, Hinweis auf Docker Desktop **und** Podman.
- URLs/Ports: Keycloak `http://localhost:3010`, Backend `http://localhost:3011`, Frontend
  `http://localhost:3012`.
- Test-Zugangsdaten: `alice`/`alice` (user), `bob`/`bob` (user), `admin`/`admin` (admin).
- Kurzhinweis auf bewusste Lab-Entscheidungen: Token in `localStorage`, Public Client,
  Klartext-Passwörter, H2 in-memory mit Auto-Reset — als gewollt dokumentiert.

## Acceptance criteria

- [ ] `README.md` beschreibt den Start mit einem einzigen `docker compose up` und nennt Docker Desktop und Podman.
- [ ] URLs und Ports (3010/3011/3012) sind dokumentiert.
- [ ] Test-Zugangsdaten (`alice`, `bob`, `admin`) sind dokumentiert.
- [ ] Die bewusst „unsicheren" Baseline-Entscheidungen sind als gewollt benannt.

## Blocked by

- 01 — Walking Skeleton: Login + eigene Todos auflisten
- 02 — Todo-Schreiboperationen + Ownership-Schutz
- 03 — Admin-Ansicht + Function-Level Authorization
- 04 — Dev-Experience: Hot-Reload + Auto-Restart
