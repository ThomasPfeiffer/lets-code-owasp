# 02 — Todo-Schreiboperationen + Ownership-Schutz

## What to build

Der Benutzer kann seine Todos vollständig verwalten: anlegen, Status umschalten, Titel bearbeiten,
löschen. Jede Schreib- und Einzelzugriffsoperation ist durch eine Ownership-Prüfung geschützt — ein
Benutzer kann ausschließlich seine eigenen Todos lesen und verändern, und der `owner` wird immer aus
dem Token gesetzt, niemals aus dem Request-Body übernommen.

Umfang end-to-end:

- **Backend-API-Vertrag** (ergänzend zu `GET /api/todos` aus Slice #1):
  - `POST /api/todos` — neue Todo des aktuellen Benutzers; Server setzt `owner` aus dem Token, ein
    abweichender `owner` im Body wird ignoriert.
  - `GET /api/todos/{id}` — einzelne Todo, nur bei Besitz.
  - `PUT /api/todos/{id}` — Aktualisieren von Titel/Status, nur bei Besitz.
  - `DELETE /api/todos/{id}` — Löschen, nur bei Besitz.
- **Ownership-Prüfung** auf Service-/Methodenebene (expliziter Besitzabgleich bzw. `@PreAuthorize`).
  Zugriff auf eine fremde Todo-ID ergibt 403/404. Dies ist bewusst der spätere IDOR-Ansatzpunkt.
- **Frontend:** UI zum Anlegen (Titel-Eingabe), Umschalten des erledigt-Status, Bearbeiten des
  Titels und Löschen einzelner eigener Todos. Nutzt den bestehenden Bearer-`fetch`-Wrapper.
- **Tests:** `MockMvc`-Integrationstests mit simuliertem `jwt()` — u. a. `POST` ignoriert
  abweichenden `owner` im Body; Zugriff/Änderung/Löschen einer fremden Todo-ID → 403/404; eigene
  Operationen → Erfolg.

## Acceptance criteria

- [ ] `POST /api/todos` legt eine Todo mit `owner` aus dem Token an; ein im Body mitgesendeter abweichender `owner` wird ignoriert.
- [ ] `GET /api/todos/{id}` liefert die Todo nur bei Besitz, sonst 403/404.
- [ ] `PUT /api/todos/{id}` ändert Titel und/oder Status nur bei Besitz.
- [ ] `DELETE /api/todos/{id}` löscht nur bei Besitz.
- [ ] Zugriff auf oder Änderung einer fremden Todo-ID ergibt eine Fehlermeldung (403/404).
- [ ] Das Frontend bietet Anlegen, Umschalten, Bearbeiten und Löschen eigener Todos.
- [ ] `MockMvc`-Tests decken Owner-aus-Token, Fremdzugriff-Ablehnung und erfolgreiche Eigenoperationen ab.

## Blocked by

- 01 — Walking Skeleton: Login + eigene Todos auflisten
