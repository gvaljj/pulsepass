# Matriz de cobertura E2E — 22 rutas, roles y trazabilidad (PRD §7, §18, §25.7)

Entregable complementario de la Fase 5 (aprobado como archivo extra). Sostiene el
DoD §24 item 13 («Matriz requisito → caso → ruta → resultado disponible para
revisión docente») y el §25.7 («Matriz de cobertura de 22 rutas y roles»).

- **Resultado de la corrida (2026-10-23): 125/125 peticiones PASS; 80/80 casos
  lógicos PASS**, 0 FAIL, 0 BLOCKED, 0 NOT RUN. Las 22 rutas tienen al menos una
  petición ejecutada; los 5 actores se usaron en la suite, pero **no todas las
  combinaciones ruta × actor se ejecutaron**. El detalle está en
  `registro-ejecucion-manual.md`.
- **Método:** colección sin scripts; cada petición tiene su propio `Request ID` y
  su fila de bitácora. No se usó Collection Runner, Newman ni Postman CLI.
- Política anti-enumeración **D2** (decisión documentada en `README.md`):
  recurso ajeno **existente** → `403`; borrador o código **inexistente** → `404`
  (idéntico en cuerpo a cualquier 404, `ensureEventVisible`).

**Leyenda (§7):** `P` público · `A` requiere autenticación · `ADM` ADMIN ·
`OPS` STAFF/ADMIN · `OWN` propietario validado contra id persistente ·
`PUB` solo información publicada · `403/404*` denegación según política
consistente (aquí: D2) · `—` **combinación no ejecutada**: no se infiere del
código ni se cuenta como `PASS`.

---

## Registro de la corrida

| Dato | Valor |
|---|---|
| Fecha/hora | 2026-10-23 11:30:36–11:30:41 |
| `runId` | `e2e01` |
| Base de datos | PostgreSQL 17 aislado en `127.0.0.1:55432`, BD `pulsepass_e2e` |
| Aplicación | Spring Boot 4.1.1 / Java 21 en `http://127.0.0.1:8080` |
| Resultado | **125/125 peticiones PASS; 80/80 casos lógicos PASS**, 0 FAIL, 0 BLOCKED, 0 NOT RUN |
| Evidencia | `registro-ejecucion-manual.md` (125 filas, una por petición) |
| Colección | 125 peticiones en 10 carpetas, 0 scripts |
| Defectos | Ninguno |

**Nota de método:** la colección permanece sin scripts y cada petición tiene su
propia fila.

---

## Tabla 1 — Cobertura HTTP ejecutada por ruta y actor (22 rutas)

Solo se marcan las combinaciones con al menos una petición de la colección
ejecutada. `—` significa **no ejecutado**, no «permitido por el código».

| # | Método y ruta (§7) | ANONYMOUS | USER_A | USER_B | STAFF | ADMIN | Casos que lo verifican (bitácora) | Resultado |
|---|---|---|---|---|---|---|---|---|
| 1 | GET /api/venues/active | 200 | — | — | — | — | `PRE-00`, `E2E-VEN-02`, `TC-AUTH-015A`, `TC-BIZ-001` | PASS |
| 2 | GET /api/venues/{code} | 200 / 404 | — | — | — | — | `E2E-VEN-01`, `E2E-VEN-01N` (404), `TC-BIZ-001` | PASS |
| 3 | GET /api/artists/active | 200 | — | — | — | — | `E2E-ART-03`, `TC-BIZ-002` | PASS |
| 4 | GET /api/artists/{id} | 200 / 404 | — | — | — | — | `E2E-ART-01`, `E2E-ART-01N` (404), `TC-BIZ-002` | PASS |
| 5 | GET /api/artists/by-stage-name | 200 / 404 | — | — | — | — | `E2E-ART-02`, `E2E-ART-02N` (404), `TC-BIZ-002` | PASS |
| 6 | POST /api/users | 201 / 400 / 409 | — | — | — | — | `TC-AUTH-001`, `TC-BIZ-006`, `TC-AUTH-013`, `TC-SEC-016`, `VAL-400-01/02` (400), `VAL-409-01` (409), `E2E-USR-01` | PASS |
| 7 | POST /api/auth/login | 200 / 401 | — | — | — | — | `TC-AUTH-002/003` (200), `TC-AUTH-004/005` (401), `TC-AUTH-013`, `TC-SEC-016B`, `E2E-AUTH-01` | PASS |
| 8 | GET /api/auth/me | 401 | 200 / 401 | 200 | 200 | 200 | `TC-AUTH-006/006B/006S/006A` (200); 401: `TC-AUTH-007`–`013`, `TC-AUTH-010` (expired), `TC-AUTH-015B`; `TC-AUTH-CLAIMS`; `TC-SEC-016C` (candidate); `E2E-AUTH-02` | PASS |
| 9 | GET /api/users/by-email | 401 | 200 / 404 | 403 | — | 200 | `E2E-USR-02`, `TC-SEC-006`, `USR-02-ADMIN`, `USR-02-401`, `USR-02-404` | PASS |
| 10 | GET /api/users/by-username | 401 | 200 | 403 | — | 200 | `E2E-USR-03`, `TC-SEC-007`, `USR-03-ADMIN`, `USR-03-401` | PASS |
| 11 | POST /api/events | 401 | 403 | — | 403 | 201 / 400 / 409 | `TC-SEC-001A` (401), `TC-SEC-001B`/`TC-AUTH-014`/`EVT-01-STAFF` (403), `E2E-EVT-01`, `EVT-01-400`, `EVT-01-409`, `VAL-400-03`, `TC-BIZ-003` | PASS |
| 12 | GET /api/events/{eventCode} | 200 / 404 | — | — | 200 | 200 | `E2E-EVT-02`, `TC-SEC-004`, `TC-SEC-015`, `TC-SEC-015B`, `EVT-02-STAFF`, `BIZ-V0A` (200 DRAFT), `BIZ-V0` (404), `BIZ-V1`, `EVT-02-404`, `ERR-404-ANATOMIA` | PASS |
| 13 | GET /api/events/published | 200 | — | — | — | — | `E2E-EVT-03`, `BIZ-V2` | PASS |
| 14 | PATCH /api/events/{eventCode}/publish | 401 | 403 | — | 403 | 200 / 409 / 404 | `TC-SEC-002`, `TC-SEC-002B` (403), `E2E-EVT-04`, `EVT-04-409`, `EVT-04-404`, `EVT-04-401`, `TC-BIZ-004` | PASS |
| 15 | POST /api/events/{eventCode}/artists/{artistId} | 401 | 403 | — | 403 | 200 / 409 / 404 | `TC-SEC-003`, `EVT-05-STAFF` (403), `E2E-EVT-05`, `EVT-05-409`, `EVT-05-404`, `EVT-05-401`, `TC-BIZ-005` | PASS |
| 16 | GET /api/events/by-artist | 200 | — | — | — | 200 | `E2E-EVT-06`, `TC-SEC-005` (anónimo: solo PUB), `EVT-06-OPS` (ADMIN: PUB+CAT), `BIZ-V3` | PASS |
| 17 | POST /api/tickets | 401 | 201 / 400 / 403 / 404 / 409 | — | — | — | `E2E-TKT-01/01B`, `TC-SEC-008` (USER_A con email de B: 403), `TKT-01-401`, `TKT-01-404`, `TC-BIZ-014A/B/C` (409), `VAL-400-04/05` (400), `TC-BIZ-007` | PASS |
| 18 | GET /api/tickets/{ticketCode} | 401 | 200 / 404 | 403 | 200 | — | `E2E-TKT-02`, `TC-SEC-009` (USER_B: 403), `TKT-02-STAFF`, `TKT-02-404`, `TKT-02-401`, `BIZ-V4` (CANCELLED), `BIZ-V5` (USED) | PASS |
| 19 | GET /api/tickets/by-user | 401 | 200 | 403 | — | 200 | `E2E-TKT-03`, `TC-SEC-010`, `TKT-03-ADMIN`, `TKT-03-401` | PASS |
| 20 | GET /api/events/{eventCode}/tickets/paid | 401 | 403 | — | 200 / 403 | — | `E2E-TKT-04`, `TC-SEC-013` (USER_A: 403), `TKT-04-401`, `TC-SEC-017` (STAFF con rol revocado: 403) | PASS |
| 21 | PATCH /api/tickets/{ticketCode}/cancel | 401 | 200 / 409 | 403 | 403 | — | `E2E-TKT-05`, `TC-SEC-011`, `TKT-05-409`, `TKT-05-STAFF`, `TKT-05-401`, `TC-BIZ-010/011` | PASS |
| 22 | PATCH /api/tickets/{ticketCode}/use | 401 | 403 | 403 | 200 / 409 | — | `E2E-TKT-06`, `TC-SEC-012/014`, `TKT-06-OWNER`, `TKT-06-409`, `TKT-06-401`, `TC-BIZ-012/013` | PASS |

Notas de celdas:

- `—` = no se ejecutó una petición de la colección con ese actor en esa ruta.
  No se deduce del código, de la política `SecurityConfig` ni de una corrida
  anterior; por tanto no cuenta como `PASS`.
- Rutas públicas (1–5, 13): solo se ejercitó `ANONYMOUS`; las demás columnas
  quedan `—` aunque la ruta sea pública.
- Registro y login (6–7): son peticiones anónimas por diseño; las columnas de
  rol quedan `—`. En login, `ANONYMOUS` representa la petición con credenciales
  de A/B/STAFF/ADMIN, del candidato (`TC-SEC-016B`) o credenciales inválidas.
- `USER_B` en `POST /api/tickets` (17): **no ejecutado**. `TC-SEC-008` usa el
  token de `USER_A` con el `userEmail` de B; no es una petición de `USER_B`.
- `STAFF` en `GET /api/events/by-artist` (16): **no ejecutado**; la visibilidad
  de borradores se comprobó con `ADMIN` (`EVT-06-OPS`).
- `ADMIN` en `GET /api/tickets/{code}` (18), `GET .../tickets/paid` (20),
  `PATCH .../cancel` (21) y `PATCH .../use` (22): **no ejecutado**. La matriz
  anterior infería `200` del código; ahora queda `—`.
- `STAFF` en `GET /api/users/by-email` y `by-username` (9–10): **no ejecutado**.
- `USER_A`/`USER_B` en `GET /api/events/{eventCode}` y `GET /api/events/published`
  (12–13): **no ejecutado**; solo hay peticiones anónimas, STAFF y ADMIN.
- `401` solo se marca donde hay una petición dedicada ejecutada:
  `USR-02-401`, `USR-03-401`, `TKT-01-401`, `TKT-02-401`, `TKT-03-401`,
  `TKT-04-401`, `TKT-05-401`, `TKT-06-401`, `EVT-04-401`, `EVT-05-401`,
  `TC-SEC-001A`, `TC-AUTH-007`–`013`, `TC-AUTH-015B`. No se rellena el resto por
  analogía.
- `TC-AUTH-013` (INACTIVE_USER) y `TC-AUTH-010` (USER_A con token expirado)
  producen 401 en `/me`; `TC-SEC-016C` (candidate) produce 200 con
  `roles=[USER]`. No son columnas de la matriz, pero están en la bitácora.
- Cada `403` de mutación (filas 11, 14, 15, 17, 21, 22) tiene relectura de
  integridad en la bitácora (NFR-E2E-006, §8.3).

### Justificación de 403 vs 404 (exigida por §7 y §8, decisión D2)

El PRD prohíbe «aceptar silenciosamente cualquier 404 como sustituto de un 403
sin justificarlo en la matriz». Justificación en esta ejecución:

| Situación | Código | Por qué y dónde se verifica |
|---|---|---|
| Recurso ajeno **existente** (ticket, usuario, listados de otro usuario) | **403** | Anti-IDOR sin oráculo de existencia: `TC-SEC-006`–`TC-SEC-011`, `TC-SEC-008`. Nunca se responde 404 a un recurso existente ajeno. |
| Borrador de evento (existente pero no publicado) | **404** | Anti-enumeración: `ensureEventVisible` responde **idéntico** a inexistente (`TC-SEC-004` vs `EVT-02-404`), y el mismo recurso responde 200 para OPS (`TC-SEC-015`/`EVT-02-STAFF`), probando que el 404 es de visibilidad, no de inexistencia encubridora. |
| Código inexistente | **404** | `EVT-02-404`, `EVT-04-404`, `EVT-05-404`, `TKT-01-404`, `TKT-02-404`, `USR-02-404`, `E2E-*-01N`. |
| Rol insuficiente sobre ruta/recurso (sin propiedad de por medio) | **403** | `TC-SEC-001A/001B`, `TC-SEC-002/002B`, `TC-SEC-003`, `TC-SEC-013`, `EVT-01-STAFF`, `EVT-05-STAFF`, `TKT-05-STAFF`. |

`expectedForeignResourceStatus=403` en el ambiente documenta esta decisión
(§11: «decisión explícita documentada, no aceptar ambos de forma
indiscriminada»).

---

## Tabla 2 — Trazabilidad requisito → caso → ruta → resultado (§6, §18, §22)

| Requisito | Caso(s) | Ruta(s) | Resultado | Nota / evidencia (§18) |
|---|---|---|---|---|
| FR-E2E-001 Colección PM v2.1 | Inspección de carpetas y peticiones | 22/22 rutas presentes | PASS | 10 carpetas §10.1, 125 peticiones; 0 scripts; estructura validada por inspección del JSON |
| FR-E2E-002 Entorno parametrizable | `PRE-00` + revisión de ambiente | Todas vía `{{baseUrl}}` | PASS | 32 vars; `{{baseUrl}}` y tokens resueltos en el ambiente; sin secretos versionados |
| FR-E2E-003 Autenticación | `TC-AUTH-001`–`005` | `POST /api/users`, `POST /api/auth/login` | PASS | 201→200; token copiado a la variable local tras el login, sin secretos versionados |
| FR-E2E-004 Token por actor | `TC-AUTH-006/006B/006S/006A` | `GET /api/auth/me` | PASS | 4 tokens de actor usados; `/me` 200 con roles USER/STAFF/ADMIN |
| FR-E2E-005 Comprobación | Todas (descripción §10.2 con HTTP esperado) | 22 rutas | PASS | 125 peticiones con ID/objetivo/esperado; resultados registrados en la bitácora |
| FR-E2E-006 Encadenamiento | `E2E-ART-03`→`artistId`; `E2E-EVT-01`→`eventToPublishCode`; `E2E-TKT-01/01B`→códigos de ticket | Filas 5, 11, 17–22 | PASS | IDs y códigos encadenados; filas 5, 11, 17–22 PASS |
| FR-E2E-007 Matriz de autorización | `TC-SEC-001`–`017`, `TC-AUTH-007/014/015` | Tabla 1 completa | PASS | 5 actores usados; 22/22 rutas; celdas `—` no ejecutadas; D2 403/404 |
| FR-E2E-008 Integridad funcional | `BIZ-V0A/V0/V1/V2/V3/V4/V5` + relecturas tras cada 403 | Filas 12, 13, 16, 18, 21, 22 | PASS | Relecturas BIZ-V0A/V0/V1/V2/V3/V4/V5 y tras cada 403 |
| FR-E2E-009 ErrorResponse | `ERR-403-ANATOMIA`, `ERR-404-ANATOMIA`, `VAL-400-*`, `USR-02-404` | Varias | PASS | 5 campos (timestamp/status/error/message/details); 400/401/403/404/409 verificados |
| FR-E2E-010 Datos controlados | Seed `pulsepass_e2e` + `fixtures/README.md` | — | PASS | Seed `pulsepass_e2e`; `runId=e2e01`; restauración documentada |
| FR-E2E-011 Instrucciones paso a paso | `checklist-e2e-manual.md` (Flujos A–E) | Todas | PASS | Checklist A–E ejecutado en orden |
| FR-E2E-012 Bitácora de ejecución | `registro-ejecucion-manual.md` (80 casos lógicos) | 22 rutas + familias §8 | PASS | 125 filas (una por petición); 125/125 peticiones PASS; 80/80 casos lógicos PASS; fecha/hora 2026-10-23 11:30:36–11:30:41 |
| FR-E2E-013 Confidencialidad | Revisión de exportables | — | PASS | Sin JWT/contraseñas en docs; búsqueda local de secretos |
| FR-E2E-014 Regresión | Flujo E (nominales + errores §9.5) | Todas | PASS | Flujo E; 0 FAIL; sin 500 |
| FR-E2E-015 Bloqueos | Regla §13.5 de la bitácora | — | PASS | 0 BLOCKED; casos dependientes resueltos |
| FR-E2E-016 Repetibilidad | `runId` + seed idempotente + `.gitignore` | — | PASS | `runId=e2e01`; seed idempotente; BD aislada |
| FR-E2E-017 Contratos heredados | `TC-HTTP` (§8.1): `VAL-400-01`–`05`, `VAL-409-01`, revisión de DTO | Varias | PASS | VAL-400-01..05 y VAL-409-01 ejecutados; DTOs intactos |
| NFR-E2E-001 Portabilidad | Inspección del JSON | — | PASS | 0 scripts; colección v2.1 |
| NFR-E2E-002 Seguridad de secretos | Búsqueda de secretos en el repo | — | PASS | Sin secretos versionados; scanner local |
| NFR-E2E-003 Determinismo | Seed con `ON CONFLICT` + `runId` | — | PASS | Seed `ON CONFLICT` + `runId` |
| NFR-E2E-004 Legibilidad | Carpetas §10.1 + IDs rastreables | — | PASS | 125 peticiones con IDs rastreables |
| NFR-E2E-005 Diagnóstico | Bitácora con actor y escenario | — | PASS | 125 filas con actor, ruta, status y evidencia |
| NFR-E2E-006 Integridad | Relecturas de `TC-SEC-003/008`–`012` | Filas 15, 17–22 | PASS | Relecturas TC-SEC-003/008–012; sin mutación |
| NFR-E2E-007 Compatibilidad | `git status` de `src/` | — | PASS | `src/` sin cambios (`git diff`) |
| NFR-E2E-008 Eficiencia | `checklist-e2e-manual.md` (Flujos A–E en orden, sin pasos repetidos) | Todas | PASS | Flujos A–E en secuencia única; 125 peticiones |

---

## Consolidación de cobertura

| Corte | Estado |
|---|---|
| Rutas con ≥1 caso nominal ejecutado | **22/22** (125/125 peticiones PASS; 80/80 casos lógicos PASS) |
| Actores usados en la suite | 5: ANONYMOUS, USER_A, USER_B, STAFF, ADMIN; además INACTIVE_USER, CANDIDATE y USER_A con token expirado. **No todas las combinaciones ruta × actor se ejecutaron** (ver `—` en Tabla 1) |
| Casos totales registrados en la bitácora | 80 (22 §7 + 16 §8.2 + 22 §8.3 + 20 §8.4), **80/80 casos PASS**, 0 FAIL, 0 BLOCKED, 0 NOT RUN |
| Filas de bitácora | **125** (una por `Request ID`), **125 PASS**; ninguna fila agrupa varias peticiones |
| Peticiones en la colección | 125 en 10 carpetas, 0 scripts |
| Defectos registrados | Ninguno (0 FAIL, 0 BLOCKED) |
| Corrida | 2026-10-23 11:30:36–11:30:41; `runId=e2e01`; PostgreSQL 17 aislado; app en `:8080` |
