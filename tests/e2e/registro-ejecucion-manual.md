# Registro de ejecución manual — bitácora E2E (PRD §16.2)

Plantilla de resultados para la colección **PulsePass — E2E Manual**. Formato de
cada fila según §16.2 (Caso, Actor, Ruta, HTTP esperado, HTTP observado,
JSON/estado observado, Resultado, Evidencia/observación) más la fecha/hora que
exige FR-E2E-012 y §13.4.

**Reglas (§13.5, §16.3, FR-E2E-013):**

- `PASS`: HTTP, JSON y condición de negocio/seguridad observados coinciden con lo esperado.
- `FAIL`: cualquiera de las comprobaciones no coincide, o existe un 500 no esperado.
- `BLOCKED`: no se puede ejecutar por falta de fixture, actor, token o respuesta previa.
- `NOT RUN`: caso aún no ejecutado (estado inicial de TODA esta bitácora).
- No incluir JWT, contraseñas ni información privada en esta bitácora ni en capturas.
- Un caso dependiente de otro `BLOCKED` jamás se cuenta como aprobado (§17).

**Estado al crear el entregable: ninguna ejecución realizada.** Todos los casos
están en `NOT RUN` con `pendiente` en observado; se rellena durante la sesión
manual descrita en `checklist-e2e-manual.md`.

---

## 1. Cobertura de rutas — §7 (22/22 casos base)

Un caso nominal por cada una de las 22 rutas base (20 de negocio + login + me).
Las políticas negativas adicionales de cada ruta viven en las familias §8.

| Caso | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| E2E-VEN-01 | ANONYMOUS | GET /api/venues/{code} | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-VEN-01`, `E2E-VEN-01N` (404) |
| E2E-VEN-02 | ANONYMOUS | GET /api/venues/active | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `PRE-00`, `E2E-VEN-02` |
| E2E-ART-01 | ANONYMOUS | GET /api/artists/{id} | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-ART-01`, `E2E-ART-01N` (404) |
| E2E-ART-02 | ANONYMOUS | GET /api/artists/by-stage-name?stageName=... | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-ART-02`, `E2E-ART-02N` (404) |
| E2E-ART-03 | ANONYMOUS | GET /api/artists/active | — | 200 | pendiente | pendiente | NOT RUN | Petición `E2E-ART-03` |
| E2E-USR-01 | ANONYMOUS | POST /api/users | — | 201 | pendiente | pendiente | NOT RUN | Peticiones `TC-AUTH-001` y `TC-BIZ-006A` (nominales); `USR-01-409`, `VAL-400-01`, `VAL-400-02` (negativos) |
| E2E-USR-02 | USER_A (OWN) | GET /api/users/by-email?email=... | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-USR-02`, `USR-02-ADMIN`, `TC-SEC-006`, `USR-02-401`, `USR-02-404` |
| E2E-USR-03 | USER_A (OWN) | GET /api/users/by-username?username=... | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-USR-03`, `USR-03-ADMIN`, `TC-SEC-007` |
| E2E-EVT-01 | ADMIN | POST /api/events | — | 201 | pendiente | pendiente | NOT RUN | Petición `E2E-EVT-01` (guarda `eventToPublishCode`) |
| E2E-EVT-02 | ANONYMOUS (evento publicado) | GET /api/events/{eventCode} | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-EVT-02`, `TC-SEC-004`, `TC-SEC-015`, `EVT-02-STAFF`, `BIZ-V0A`, `BIZ-V0`, `BIZ-V1` |
| E2E-EVT-03 | ANONYMOUS | GET /api/events/published | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-EVT-03`, `BIZ-V2` |
| E2E-EVT-04 | ADMIN | PATCH /api/events/{eventCode}/publish | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-EVT-04`, `EVT-04-409`, `EVT-04-404` |
| E2E-EVT-05 | ADMIN | POST /api/events/{eventCode}/artists/{artistId} | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-EVT-05`, `EVT-05-409`, `EVT-05-404` |
| E2E-EVT-06 | ANONYMOUS | GET /api/events/by-artist?stageName=... | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-EVT-06`, `TC-SEC-005`, `EVT-06-OPS`, `BIZ-V3` |
| E2E-TKT-01 | USER_A | POST /api/tickets | — | 201 | pendiente | pendiente | NOT RUN | Peticiones `E2E-TKT-01`, `E2E-TKT-01B`, `TKT-01-401`, `TKT-01-404` |
| E2E-TKT-02 | USER_A (OWN) | GET /api/tickets/{ticketCode} | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-TKT-02`, `TKT-02-STAFF`, `TKT-02-404`, `TC-SEC-009`, `BIZ-V4`, `BIZ-V5` |
| E2E-TKT-03 | USER_A (OWN) | GET /api/tickets/by-user?email=... | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-TKT-03`, `TKT-03-ADMIN`, `TKT-03-401`, `TC-SEC-010` |
| E2E-TKT-04 | STAFF (OPS) | GET /api/events/{eventCode}/tickets/paid | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-TKT-04`, `TC-SEC-013`, `TKT-04-401`, `TC-SEC-017` |
| E2E-TKT-05 | USER_A (OWN) | PATCH /api/tickets/{ticketCode}/cancel | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-TKT-05`, `TC-SEC-011`, `TKT-05-409`, `TKT-05-STAFF` |
| E2E-TKT-06 | STAFF (OPS) | PATCH /api/tickets/{ticketCode}/use | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `E2E-TKT-06`, `TC-SEC-012`, `TKT-06-OWNER`, `TKT-06-409`, `TKT-06-401` |
| E2E-AUTH-01 | USER_A | POST /api/auth/login | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `TC-AUTH-002` (username) y `TC-AUTH-003` (email); `TC-AUTH-004/005` (401) |
| E2E-AUTH-02 | USER_A | GET /api/auth/me | — | 200 | pendiente | pendiente | NOT RUN | Peticiones `TC-AUTH-006`, `TC-AUTH-006B`, `TC-AUTH-006S`, `TC-AUTH-006A` |

---

## 2. Casos de autenticación / JWT — §8.2 (TC-AUTH, 15)

| Caso | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| TC-AUTH-001 | ANONYMOUS | POST /api/users | — | 201 | pendiente | pendiente | NOT RUN | `TC-AUTH-001`: sin JWT implícito, rol inicial USER, sin password/hash |
| TC-AUTH-002 | USER_A | POST /api/auth/login | — | 200 | pendiente | pendiente | NOT RUN | `TC-AUTH-002`: accessToken no vacío, tokenType=Bearer, expiresIn=900 |
| TC-AUTH-003 | USER_A | POST /api/auth/login (email) | — | 200 | pendiente | pendiente | NOT RUN | `TC-AUTH-003`: mismo usuario identificado por email |
| TC-AUTH-004 | ANONYMOUS | POST /api/auth/login (password erróneo) | — | 401 | pendiente | pendiente | NOT RUN | `TC-AUTH-004`: 401 genérico, sin enumeración |
| TC-AUTH-005 | ANONYMOUS | POST /api/auth/login (identificador inexistente) | — | 401 | pendiente | pendiente | NOT RUN | `TC-AUTH-005`: idéntico en forma a TC-AUTH-004 |
| TC-AUTH-006 | USER_A | GET /api/auth/me | — | 200 | pendiente | pendiente | NOT RUN | `TC-AUTH-006` (+`006B`, `006S`, `006A`): identidad y roles sin credenciales |
| TC-AUTH-007 | ANONYMOUS | GET /api/auth/me sin header | — | 401 | pendiente | pendiente | NOT RUN | `TC-AUTH-007`: 401 + ErrorResponse |
| TC-AUTH-008 | ANONYMOUS | GET /api/auth/me con Bearer inventado | — | 401 | pendiente | pendiente | NOT RUN | `TC-AUTH-008`: 401 + ErrorResponse |
| TC-AUTH-009 | ANONYMOUS | GET /api/auth/me con JWT alterado | — | 401 | pendiente | pendiente | NOT RUN | `TC-AUTH-009`: nunca ingresa al controller |
| TC-AUTH-010 | INACTIVE_USER (token fixture) | GET /api/auth/me con JWT expirado | — | 401 | pendiente | pendiente | NOT RUN | `TC-AUTH-010` con `expiredToken` (generada offline, §6 de `fixtures/README.md`) |
| TC-AUTH-011 | ANONYMOUS | GET /api/auth/me con JWT de otra clave/issuer | — | 401 | pendiente | pendiente | NOT RUN | `TC-AUTH-011` con `foreignToken` (clave exclusiva de test, nunca la real) |
| TC-AUTH-012 | ANONYMOUS | GET /api/auth/me?token=... (query string) | — | 401 | pendiente | pendiente | NOT RUN | `TC-AUTH-012`: el token por query no autentica |
| TC-AUTH-013 | INACTIVE_USER | GET /api/auth/me con JWT previo | — | 401 | pendiente | pendiente | NOT RUN | `TC-AUTH-013A/013B` + desactivación SQL + `TC-AUTH-013` con `inactiveToken` |
| TC-AUTH-014 | USER_A | POST /api/events (endpoint ADMIN) | — | 403 | pendiente | pendiente | NOT RUN | `TC-SEC-001B / TC-AUTH-014` (misma petición, doble caso) |
| TC-AUTH-015 | ANONYMOUS | GET /api/venues/active + GET /api/auth/me (mismo Bearer inválido) | — | 200 público / 401 en /me | pendiente | pendiente | NOT RUN | `TC-AUTH-015A` y `TC-AUTH-015B`: política para token inválido presente en endpoint público |

---

## 3. Casos de autorización / IDOR — §8.3 (TC-SEC, 17)

Política aplicada (decisión D2 documentada en `README.md`): recurso ajeno
**existente** → `403`; borrador o recurso inexistente → `404`. Todo `403` de
mutación exige relectura de integridad posterior (§8.3, NFR-E2E-006).

| Caso | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| TC-SEC-001 | ANONYMOUS / USER_A | POST /api/events | — | 401 / 403 | pendiente | pendiente | NOT RUN | `TC-SEC-001A` (401) y `TC-SEC-001B` (403); relectura: ningún evento persistido |
| TC-SEC-002 | USER_A / STAFF | PATCH /api/events/{eventCode}/publish | — | 403 / 403 | pendiente | pendiente | NOT RUN | `TC-SEC-002` y `TC-SEC-002B`; relectura con `TC-SEC-015`: estado sin cambios |
| TC-SEC-003 | USER_A | POST /api/events/{eventCode}/artists/{artistId} | — | 403 | pendiente | pendiente | NOT RUN | `TC-SEC-003`; relectura: `artists[]` sin cambios |
| TC-SEC-004 | ANONYMOUS | GET /api/events/{eventCode} (borrador) | — | 404 (no expone datos; política D2) | pendiente | pendiente | NOT RUN | `TC-SEC-004`: forma idéntica a `EVT-02-404`, sin datos del draft |
| TC-SEC-005 | ANONYMOUS | GET /api/events/by-artist?stageName=... | — | 200 sin el borrador | pendiente | pendiente | NOT RUN | `TC-SEC-005`: draft no incluido; contraste con `EVT-06-OPS` |
| TC-SEC-006 | USER_B | GET /api/users/by-email (email de A) | — | 403 (D2) | pendiente | pendiente | NOT RUN | `TC-SEC-006`: sin información personal de A |
| TC-SEC-007 | USER_B | GET /api/users/by-username (username de A) | — | 403 (D2) | pendiente | pendiente | NOT RUN | `TC-SEC-007`: sin información personal de A |
| TC-SEC-008 | USER_A con userEmail de B | POST /api/tickets | — | 403 | pendiente | pendiente | NOT RUN | `TC-SEC-008`: denegado, sin ticket nuevo (relectura de listado de B) |
| TC-SEC-009 | USER_B | GET /api/tickets/{ticketCode} (ticket de A) | — | 403 (D2) | pendiente | pendiente | NOT RUN | `TC-SEC-009`; relectura del dueño: sin datos alterados |
| TC-SEC-010 | USER_B | GET /api/tickets/by-user (email de A) | — | 403 (D2) | pendiente | pendiente | NOT RUN | `TC-SEC-010`: sin información de A |
| TC-SEC-011 | USER_B | PATCH /api/tickets/{ticketCode}/cancel (ticket de A) | — | 403 (D2) | pendiente | pendiente | NOT RUN | `TC-SEC-011` (antes de cancelar T1); relectura: T1 sigue PAID |
| TC-SEC-012 | USER_B | PATCH /api/tickets/{ticketCode}/use (ticket de A) | — | 403 | pendiente | pendiente | NOT RUN | `TC-SEC-012` (tras el uso de `E2E-TKT-06`, §9.4); relectura: T2 sigue USED |
| TC-SEC-013 | USER_A | GET /api/events/{eventCode}/tickets/paid | — | 403 | pendiente | pendiente | NOT RUN | `TC-SEC-013`: rol insuficiente (taquilla) |
| TC-SEC-014 | STAFF | PATCH /api/tickets/{ticketCode}/use | — | 200 | pendiente | pendiente | NOT RUN | `E2E-TKT-06` (misma petición, doble caso): transición PAID → USED válida |
| TC-SEC-015 | ADMIN / STAFF | GET /api/events/{eventCode} (borrador) | — | 200 | pendiente | pendiente | NOT RUN | `TC-SEC-015` (ADMIN) y `EVT-02-STAFF` (STAFF), conforme a política Fase 4 |
| TC-SEC-016 | ANONYMOUS (role=ADMIN inyectado) | POST /api/users → login → /me | — | 201 sin escalación | pendiente | pendiente | NOT RUN | `TC-SEC-016`, `TC-SEC-016B`, `TC-SEC-016C`: roles=[USER] efectivos, nunca ADMIN |
| TC-SEC-017 | STAFF con JWT antiguo (rol revocado por SQL) | GET /api/events/{eventCode}/tickets/paid | — | 403 | pendiente | pendiente | NOT RUN | `TC-SEC-017` + **restauración SQL** obligatoria (§3 de `fixtures/README.md`) |

---

## 4. Casos de negocio y transiciones — §8.4 (TC-BIZ, 15)

| Caso | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| TC-BIZ-001 | ANONYMOUS | GET /api/venues/active y GET /api/venues/{code} | — | 200 / 200 | pendiente | pendiente | NOT RUN | `E2E-VEN-02` y `E2E-VEN-01`: identidad y estado coinciden con fixture |
| TC-BIZ-002 | ANONYMOUS | GET /api/artists/active, /{id}, /by-stage-name | — | 200 ×3 | pendiente | pendiente | NOT RUN | `E2E-ART-03`, `E2E-ART-01`, `E2E-ART-02`: registros esperados |
| TC-BIZ-003 | ADMIN | POST /api/events | — | 201 | pendiente | pendiente | NOT RUN | `E2E-EVT-01`: eventCode disponible para GET posterior (`BIZ-V0A`) |
| TC-BIZ-004 | ADMIN | PATCH /api/events/{eventCode}/publish | — | 200 | pendiente | pendiente | NOT RUN | `E2E-EVT-04`: estado PUBLISHED y aparece en `/published` (`BIZ-V1`, `BIZ-V2`) |
| TC-BIZ-005 | ADMIN | POST /api/events/{eventCode}/artists/{artistId} | — | 200 | pendiente | pendiente | NOT RUN | `E2E-EVT-05`: la búsqueda por artista devuelve el evento (`BIZ-V3`) |
| TC-BIZ-006 | ANONYMOUS | POST /api/users (dos usuarios) | — | 201 + 201; duplicado 409 | pendiente | pendiente | NOT RUN | `TC-AUTH-001`, `TC-BIZ-006A`, `USR-01-409`: unicidad verificable |
| TC-BIZ-007 | USER_A | POST /api/tickets | — | 201 | pendiente | pendiente | NOT RUN | `E2E-TKT-01`: ticket vinculado al usuario y evento correctos (PAID, TCK-) |
| TC-BIZ-008 | USER_A | GET /api/tickets/{ticketCode} y /by-user | — | 200 / 200 | pendiente | pendiente | NOT RUN | `E2E-TKT-02`, `E2E-TKT-03`: estado y propietario correctos |
| TC-BIZ-009 | STAFF | GET /api/events/{eventCode}/tickets/paid | — | 200 | pendiente | pendiente | NOT RUN | `E2E-TKT-04`: incluye el ticket elegible; no otros estados |
| TC-BIZ-010 | USER_A (propietario) | PATCH /api/tickets/{ticketCode}/cancel | — | 200 | pendiente | pendiente | NOT RUN | `E2E-TKT-05` + `BIZ-V4`: nueva lectura refleja CANCELLED |
| TC-BIZ-011 | USER_A | PATCH /api/tickets/{ticketCode}/cancel (2.ª vez) | — | 409 | pendiente | pendiente | NOT RUN | `TKT-05-409`: sin mutación; estado sigue CANCELLED |
| TC-BIZ-012 | STAFF | PATCH /api/tickets/{ticketCode}/use (2.º ticket) | — | 200 | pendiente | pendiente | NOT RUN | `E2E-TKT-06` + `BIZ-V5`: estado USED verificable |
| TC-BIZ-013 | STAFF | PATCH /api/tickets/{ticketCode}/use (2.ª vez) | — | 409 | pendiente | pendiente | NOT RUN | `TKT-06-409`: no doble transición; estado sigue USED |
| TC-BIZ-014 | USER_A (y ADMIN para preparar) | POST /api/tickets (evento DRAFT / edad / aforo) | — | 409 ×3 | pendiente | pendiente | NOT RUN | `TC-BIZ-014A` (DRAFT), `TC-BIZ-014B` (edad mínima 40), `TC-BIZ-014C` (aforo agotado); sin ticket persistido |
| TC-BIZ-015 | ANONYMOUS / USER_A / ADMIN | POST /api/events, GET y PATCH de códigos inexistentes | — | 400 / 404 / 409 | pendiente | pendiente | NOT RUN | `EVT-01-400`, `EVT-04-404`, `EVT-05-404`, `TKT-01-404`, `TKT-02-404`, `VAL-400-03`, `VAL-409-01` |

---

## 5. Avance de flujos obligatorios — §9 (guía, no cuenta en los 69)

Progreso de los flujos A–E en el orden del `checklist-e2e-manual.md`. Cada
paso ejecutado debe tener su caso en las tablas anteriores con `PASS`, `FAIL`
o `BLOCKED`; los flujos se dan por completos solo si sus pasos lo están.

| Flujo | Alcance (PRD) | Pasos del checklist | Estado |
|---|---|---|---|
| A — Descubrimiento público | §9.1 | A1–A10 | NOT RUN |
| B — Registro, JWT y propiedad | §9.2 | B1–B14 | NOT RUN |
| C — Catálogo y publicación (ADMIN) | §9.3 | C1–C13 | NOT RUN |
| D — Compra, propiedad, cancelación y uso | §9.4 | D1–D16 | NOT RUN |
| E — Regresión de errores y cierre | §9.5 | E1–E7 | NOT RUN |

---

## 6. Consolidación — §16.3

Conteo por módulo al cierre de la sesión. **Estado inicial (sin ejecutar):**

| Módulo | PASS | FAIL | BLOCKED | NOT RUN | Total |
|---|---|---|---|---|---|
| §7 Cobertura de rutas | 0 | 0 | 0 | 22 | 22 |
| §8.2 Autenticación (TC-AUTH) | 0 | 0 | 0 | 15 | 15 |
| §8.3 Autorización/IDOR (TC-SEC) | 0 | 0 | 0 | 17 | 17 |
| §8.4 Negocio (TC-BIZ) | 0 | 0 | 0 | 15 | 15 |
| **Total** | **0** | **0** | **0** | **69** | **69** |

- Rutas con al menos un caso nominal ejecutado: **0/22** (meta §16.3 y §17: 22/22).
- Validaciones de seguridad (§8.3) contabilizan aparte: **0** ejecutadas.
- Criterio de cierre: ningún caso omitido puede quedar sin resultado explícito;
  `BLOCKED` y `NOT RUN` nunca se cuentan como aprobados (DoD §24, §17).

### Defectos y bloqueos hallados (§16.3, FR-E2E-014/015)

| # | Caso | Precondiciones | Pasos | HTTP esperado/observado | Reproducción manual | Estado |
|---|---|---|---|---|---|---|
| — | — | — | — | — | — | Ninguno registrado (sin ejecución) |

---

## 7. Criterios de resultado — §13.5

- **PASS**: HTTP, JSON y condición de negocio/seguridad observados coinciden con lo esperado.
- **FAIL**: cualquiera de las comprobaciones no coincide, o existe error 500 no esperado.
- **BLOCKED**: no se puede ejecutar por falta de fixture, actor, token o respuesta previa indispensable.

Restricciones (§16.4): nada de Newman, Postman CLI, scripts de Postman,
pipelines ni reportes de runners; cada request se envía con **Send** y se
revisa visualmente, y todo resultado se anota aquí (DoD §24, item 12: no
declarar aprobado lo no ejecutado).
