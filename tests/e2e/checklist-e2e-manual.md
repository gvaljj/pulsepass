# Checklist E2E — Flujos A–E (PRD §9)

> **Estado de la corrida (2026-10-23):** los flujos A–E tienen resultado
> explícito. Resultado: **125/125 peticiones PASS; 80/80 casos lógicos PASS**,
> 0 FAIL, 0 BLOCKED, 0 NOT RUN. Detalle en `registro-ejecucion-manual.md` y
> `matriz-cobertura.md`.

Orden de ejecución de la corrida. Cada paso puede contener una o varias
peticiones de la colección **PulsePass — E2E Manual** (nombre exacto entre
paréntesis); la bitácora tiene **una fila por petición**. Registra el resultado
en `registro-ejecucion-manual.md`; no pases de flujo con FAIL sin anotarlo.

**Preparación previa** (§4 de `README.md`): app E2E sobre `pulsepass_e2e`,
seed aplicado (RUNID sustituido), ambiente **PulsePass-local** importado,
`runId` definido, contraseñas locales elegidas, fixtures STAFF/ADMIN registrados
y promovidos por SQL (`fixtures/README.md` §2), y PRE-00 en verde.

---

## Flujo A — Descubrimiento público sin sesión

**Objetivo:** un visitante anónimo solo ve datos públicos coherentes (FR-E2E-003).
**Actores:** ninguno (sin token).

| # | Paso | Esperado |
|---|---|---|
| A1 | `E2E-VEN-02` — lista de venues activos | 200; anota/verifica `venueActiveCode` |
| A2 | `E2E-VEN-01` — venue por código | 200 coherente con A1 |
| A3 | `E2E-VEN-01N` — código inexistente | 404 con ErrorResponse completo |
| A4 | `E2E-ART-03` — artistas activos | 200; **guarda `artistId` y `artistStageName` en el ambiente** |
| A5 | `E2E-ART-01`, `E2E-ART-01N` | 200 / 404 |
| A6 | `E2E-ART-02`, `E2E-ART-02N` | 200 coherente con A5 / 404 |
| A7 | `E2E-EVT-03` — publicados | 200; contiene `publishedEventCode` y **sin** `draftEventCode` |
| A8 | `E2E-EVT-02` — evento publicado | 200 con `status="PUBLISHED"` |
| A9 | `TC-SEC-004` — borrador como anónimo | 404 idéntico en forma a `EVT-02-404` |
| A10 | `E2E-EVT-06` + `TC-SEC-005` — por artista | 200 solo con `PUBLISHED`; el borrador **no** aparece |

## Flujo B — Identidad y propiedad de usuarios

**Objetivo:** registro, login por username/email y propiedad de recursos
propios (FR-E2E-004, E2E-USR-01..03, TC-AUTH-001..013, TC-BIZ-006).
**Actores:** USER_A, USER_B, cuenta temporal, candidato ADMIN (registrado sin
rol), STAFF/ADMIN fixture (logins).

| # | Paso | Esperado |
|---|---|---|
| B1 | `TC-AUTH-001` (USER_A), `TC-BIZ-006A` (USER_B), `TC-AUTH-013A` (temporal), `TC-SEC-016` (role inyectado) | 201 sin password/hash/tokens |
| B2 | `USR-01-409` duplicado | 409 seguro (TC-BIZ-006) |
| B3 | `TC-AUTH-002` (login username) → copia `userAToken` | 200, `expiresIn=900` |
| B4 | `TC-AUTH-003` (login email) | 200 misma identidad |
| B5 | `TC-AUTH-004`, `TC-AUTH-005` | 401 **idénticos** entre sí (sin oráculo) |
| B6 | `AUTH-STAFF`, `AUTH-ADMIN`, `AUTH-B` → copia `staffToken`/`adminToken`/`userBToken` | 200 cada login |
| B7 | `TC-AUTH-006`, `006B`, `006S`, `006A` (`/me` por actor) | 200 con identidad y roles correctos (`USER` / `STAFF` / `ADMIN`) |
| B8 | `TC-AUTH-007`, `008`, `012` | 401 uniforme (sin cabecera / Bearer inventado / token por query) |
| B9 | `TC-AUTH-013B` (login temporal) → `inactiveToken` | 200 |
| B10 | Desactivación SQL de la temporal + `TC-AUTH-013` (`inactiveToken`) | 401 con el token previo |
| B11 | `E2E-USR-02`, `E2E-USR-03` (propios) | 200 |
| B12 | `USR-02-ADMIN`, `USR-03-ADMIN` (ajenos con ADMIN) | 200 |
| B13 | `TC-SEC-006`, `TC-SEC-007` (ajenos con B) | 403 sin datos personales de A |
| B14 | `USR-02-401`, `USR-02-404`, `USR-03-401` | 401 / 404 / 401 (distintos y justificados) |

## Flujo C — Evento y publicación (ADMIN)

**Objetivo:** creación y transición DRAFT → PUBLISHED solo por ADMIN, con
propagación a listados (E2E-EVT-01..06, TC-BIZ-003..005, TC-SEC-001..005/015).
Secuencia según §9.3: crear → leer como ADMIN y verificar ocultación → asociar
artista → publicar → verificar visibilidad pública.
**Actores:** ADMIN, USER_A y STAFF (denegaciones).

| # | Paso | Esperado |
|---|---|---|
| C1 | `E2E-EVT-01` (ADMIN) → verifica/ajusta `eventToPublishCode` | 201 con `status="DRAFT"` |
| C2 | `EVT-01-400`, `EVT-01-409` | 400 con `details` por campo / 409 duplicado |
| C3 | `BIZ-V0A` (ADMIN lee el nuevo evento) | 200 `status="DRAFT"` |
| C4 | `BIZ-V0` (mismo evento como anónimo) + `E2E-EVT-03` | 404 oculto y **ausente** del listado público |
| C5 | `E2E-EVT-05` (asociar artista al evento en DRAFT) | 200 con el artista en `artists[]` |
| C6 | `E2E-EVT-04` (ADMIN publica) | 200 `status="PUBLISHED"` |
| C7 | `BIZ-V1`, `BIZ-V2` | 200 público y presente en el listado de publicados |
| C8 | `BIZ-V3` (por artista como anónimo) | 200 incluyendo el evento publicado |
| C9 | Denegaciones de creación: `TC-SEC-001A` (anónimo), `TC-SEC-001B`/`TC-AUTH-014` (USER), `EVT-01-STAFF` (STAFF) | 401 / 403 / 403 y **ningún evento persistido** |
| C10 | Denegaciones de publicación: `TC-SEC-002` (USER), `TC-SEC-002B` (STAFF) | 403 y el evento **sigue PUBLISHED** tal cual (relectura) |
| C11 | Denegaciones de asociación: `TC-SEC-003` (USER_A), `EVT-05-STAFF` (STAFF) | 403 y `artists[]` sin cambios |
| C12 | `EVT-05-409`, `EVT-05-404` | 409 duplicado / 404 artista inexistente |
| C13 | `TC-SEC-015`/`EVT-02-STAFF` (borrador fixture) + `EVT-06-OPS` | 200 `DRAFT` para ADMIN y STAFF; el anónimo no lo ve (A9/A10) |
| C14 | **401 explícitos**: `EVT-04-401` (publish sin token), `EVT-05-401` (asociar artista sin token) | 401 uniforme en ambos |
| C15 | **Setup TC-BIZ-014B**: `SETUP-EVT-EDAD` → `SETUP-EVT-EDAD-PUB` (crear y publicar evento con minimumAge=40) | 201 DRAFT → 200 PUBLISHED |
| C16 | **Setup TC-BIZ-014C**: `SETUP-EVT-AFORO` → `SETUP-EVT-AFORO-PUB` (crear y publicar evento en venueTinyCode) | 201 DRAFT → 200 PUBLISHED |

## Flujo D — Compras y ciclo de vida de tickets

**Objetivo:** compra y transiciones PAID → CANCELLED / USED con autorización
correcta (E2E-TKT-01..06, TC-BIZ-001/002/007..014, TC-SEC-008..014).
**Actores:** USER_A (dueño), USER_B (ajeno), STAFF (taquilla), ADMIN.
Orden de las denegaciones según §9.4: ownership antes de la transición que
podría volver ambiguo el 403; el de uso después del 200 (el chequeo de rol
precede al de estado).

| # | Paso | Esperado |
|---|---|---|
| D1 | `E2E-TKT-01` → `ticketCancelableCode`; `E2E-TKT-01B` → `ticketUsableCode` | 201 `PAID`; precios 50.00 / 30.00 |
| D2 | `E2E-TKT-02`, `E2E-TKT-03`, `E2E-TKT-04` | 200 (dueño, listado propio, taquilla solo `PAID`) |
| D3 | `TKT-01-401`, `TKT-01-404`, `TC-BIZ-014A` | 401 / 404 / 409 (evento DRAFT) |
| D4 | `TC-SEC-008` (comprar a nombre de B), `TC-SEC-009`, `TC-SEC-010` | 403 y **sin tickets nuevos de B** |
| D5 | `TKT-02-STAFF`, `TKT-02-404`, `TKT-03-ADMIN`, `TKT-03-401` | 200 OPS / 404 / 200 ADMIN / 401 |
| D6 | `TC-SEC-011` (B cancela T1) → releer `E2E-TKT-02` | 403 y T1 **sigue PAID** |
| D7 | `E2E-TKT-05` (A cancela T1) → `BIZ-V4` | 200 y relectura `status="CANCELLED"` |
| D8 | `TKT-05-409`, `TKT-05-STAFF`, `TKT-05-401` | 409 transición / 403 (STAFF excluido) / 401 sin token |
| D9 | `TC-SEC-013`, `TKT-04-401` (taquilla) | 403 USER / 401 sin token |
| D10 | `E2E-TKT-04` de nuevo | la lista `paid` ya **no** contiene T1 |
| D11 | `E2E-TKT-06` (STAFF usa T2) → `BIZ-V5` | 200 y relectura `status="USED"` |
| D12 | `TC-SEC-012` (USER_B intenta usar T2), `TKT-06-OWNER` (dueño A) | 403 / 403 y T2 **sigue USED** |
| D13 | `TKT-06-409`, `TKT-06-401` | 409 doble uso / 401 |
| D14 | **Setup TC-BIZ-014B**: `SETUP-TKT-AFORO-FIRST` (vender el único ticket del evento AFORO) | 201 PAID; evento pasa a SOLD_OUT |
| D15 | `TC-BIZ-014B`: compra USER_A en evento EDAD (`minimumAge=40`) | 409 con mensaje edad mínima (40) |
| D16 | `TC-BIZ-014C`: segunda compra en evento AFORO (ya SOLD_OUT) | 409; el servicio rechaza la compra porque el evento no está PUBLISHED (SOLD_OUT); verificar status=SOLD_OUT y ausencia de ticket nuevo |
| D17 | `E2E-EVT-03`/`E2E-EVT-06` al cierre | el listado público sigue sin borradores |
| D18 | **401 explícitos**: `TKT-02-401` (GET ticket), `TKT-05-401` (cancel sin token) | 401 uniforme en ambos |

## Flujo E — Combinado y cierre de sesión

**Objetivo:** estados coexistentes, escenarios de seguridad de token/rol y
validaciones (FR-E2E-010..016).
**Actores:** todos.

| # | Paso | Esperado |
|---|---|---|
| E1 | `E2E-TKT-01` otra vez (USER_A) → releer `E2E-TKT-03` | 201 y el listado de A contiene ahora tickets `PAID` + `CANCELLED` + `USED` |
| E2 | Carpeta 07 completa: `TC-AUTH-009` (alterado), `TC-AUTH-010` (`expiredToken`), `TC-AUTH-011` (`foreignToken`), `TC-AUTH-013` (`inactiveToken`) | 401 uniforme en los cuatro |
| E3 | `TC-AUTH-015A` + `TC-AUTH-015B` (mismo Bearer inválido) | 200 público / 401 en `/me` |
| E4 | `TC-SEC-016B` (login candidato) + `TC-SEC-016C` (`/me` con token pegado a mano) | `roles=["USER"]`, nunca `ADMIN` |
| E5 | `TC-SEC-017`: SQL de revocación → petición → **restauración SQL** | 403 con el JWT firmado sin rol STAFF |
| E6 | `TC-AUTH-CLAIMS`: inspección de claims JWT (sub, iss, iat, exp, jti) con `expectedIssuer`/`expectedTokenTtlSeconds` | Claims correctos verificados (`alg=HS256`, TTL 900) |
| E7 | Carpeta 08 completa (validaciones y anatomía de errores) | 400 con `details`, 409 de negocio y ErrorResponse de 5 campos |
| E8 | Cierre: completar `registro-ejecucion-manual.md` y `matriz-cobertura.md` | ningún caso en `NOT RUN` sin causa; cobertura declarada |

---

## Reglas de ejecución (§24)

- No ejecutes peticiones fuera de la colección: lo que no esté en la colección
  no cuenta como evidencia.
- Ningún `PASS` sin haber revisado el body (y, si aplica, la relectura de
  integridad). Un `403` sin prueba de "sin mutación" es insuficiente.
- Antes de cada petición protegida, comprueba la cabecera `Authorization`.
- Si un fixture falta (rol no promovido, venue sin sembrar, token vacío), el
  caso va a `BLOCKED` con la causa, nunca a `PASS` ni a `FAIL`.
