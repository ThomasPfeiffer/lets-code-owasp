# 03 — Admin-Ansicht + Function-Level Authorization

## What to build

Ein Benutzer mit Rolle `admin` kann alle Todos aller Benutzer einsehen; Benutzer mit Rolle `user`
werden daran gehindert. Die Rollentrennung wird zweistufig durchgesetzt: grobe Pfadregel in
`HttpSecurity` plus rollenbasierter Schutz auf dem Admin-Endpunkt. Dies ist bewusst der spätere
Ansatzpunkt für Broken Function Level Authorization.

Umfang end-to-end:

- **Backend:** `GET /api/admin/todos` liefert alle Todos aller Benutzer, nur für Rolle `admin`.
  `HttpSecurity`-Pfadregel `/api/admin/**` → Rolle `admin`. Ein `user`-Token an einem
  Admin-Endpunkt wird mit 403 abgewiesen.
- **Frontend:** Eine nur für die Admin-Rolle sichtbare Admin-Ansicht, die alle Todos auflistet.
  Für `user`-Rollen ist die Ansicht nicht sichtbar/erreichbar.
- **Tests:** `MockMvc`-Integrationstests mit simuliertem `jwt()` — `user`-Token an
  `/api/admin/todos` → 403; `admin`-Token → 200 mit allen Todos.

## Acceptance criteria

- [ ] `GET /api/admin/todos` liefert mit `admin`-Token 200 und alle Todos aller Benutzer.
- [ ] `GET /api/admin/todos` mit `user`-Token → 403.
- [ ] `HttpSecurity` schützt `/api/admin/**` auf Rolle `admin`.
- [ ] Die Admin-Ansicht im Frontend ist nur für die Admin-Rolle sichtbar und zeigt alle Todos.
- [ ] `MockMvc`-Tests decken `user`→403 und `admin`→200-mit-allen-Todos ab.

## Blocked by

- 01 — Walking Skeleton: Login + eigene Todos auflisten
