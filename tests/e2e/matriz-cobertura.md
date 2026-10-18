# Matriz de cobertura E2E — 22 rutas, roles y trazabilidad (PRD §7, §18, §25.7)

Entregable complementario de la Fase 5 (aprobado como archivo extra). Sostiene el
DoD §24 item 13 («Matriz requisito → caso → ruta → resultado disponible para
revisión docente») y el §25.7 («Matriz de cobertura de 22 rutas y roles»).

- **Resultado inicial: `NOT RUN` en todas las filas.** Se actualiza durante la
  sesión manual junto con `registro-ejecucion-manual.md`; nada se declara
  aprobado sin ejecutar (DoD §24 item 12).
- Política anti-enumeración **D2** (decisión documentada en `README.md`):
  recurso ajeno **existente** → `403`; borrador o código **inexistente** → `404`
  (idéntico en cuerpo a cualquier 404, `ensureEventVisible`).

**Leyenda (§7):** `P` público · `A` requiere autenticación · `ADM` ADMIN ·
`OPS` STAFF/ADMIN · `OWN` propietario validado contra id persistente ·
`PUB` solo información publicada · `403/404*` denegación según política
consistente (aquí: D2) · `—` rol no aplica o sin caso dedicado.

---

## Tabla 1 — Cobertura HTTP por ruta y rol (22 rutas × 5 actores)

| # | Método y ruta (§7) | Caso base | ANONYMOUS | USER_A | USER_B | STAFF | ADMIN | Casos que lo verifican (bitácora) |
|---|---|---|---|---|---|---|---|---|
| 1 | GET /api/venues/active | E2E-VEN-02 | 200 | 200 | 200 | 200 | 200 | `PRE-00`, `E2E-VEN-02`, `TC-AUTH-015A` |
| 2 | GET /api/venues/{code} | E2E-VEN-01 | 200 / 404 | 200 / 404 | 200 / 404 | 200 / 404 | 200 / 404 | `E2E-VEN-01`, `E2E-VEN-01N` (404) |
| 3 | GET /api/artists/active | E2E-ART-03 | 200 | 200 | 200 | 200 | 200 | `E2E-ART-03` |
| 4 | GET /api/artists/{id} | E2E-ART-01 | 200 / 404 | 200 / 404 | 200 / 404 | 200 / 404 | 200 / 404 | `E2E-ART-01`, `E2E-ART-01N` (404) |
| 5 | GET /api/artists/by-stage-name | E2E-ART-02 | 200 / 404 | 200 / 404 | 200 / 404 | 200 / 404 | 200 / 404 | `E2E-ART-02`, `E2E-ART-02N` (404) |
| 6 | POST /api/users | E2E-USR-01 | 201 | — | — | — | — | `TC-AUTH-001`, `TC-BIZ-006A`, `USR-01-409` (409), `VAL-400-01/02` (400), `TC-SEC-016` |
| 7 | POST /api/auth/login | E2E-AUTH-01 | 200 / 401 | — | — | — | — | `TC-AUTH-002/003` (200), `TC-AUTH-004/005` (401), `AUTH-STAFF/ADMIN/B`, `TC-SEC-016B` |
| 8 | GET /api/auth/me | E2E-AUTH-02 | 401 | 200 | 200 | 200 | 200 | `TC-AUTH-006/006B/006S/006A`; 401: `TC-AUTH-007`–`013`, `015B`, `016C` |
| 9 | GET /api/users/by-email | E2E-USR-02 | 401 | 200 (propio) | 403 (ajeno) | 403 | 200 | `E2E-USR-02`, `TC-SEC-006`, `USR-02-ADMIN`, `USR-02-401` (401), `USR-02-404` (404) |
| 10 | GET /api/users/by-username | E2E-USR-03 | 401 | 200 (propio) | 403 (ajeno) | 403 | 200 | `E2E-USR-03`, `TC-SEC-007`, `USR-03-ADMIN` |
| 11 | POST /api/events | E2E-EVT-01 | 401 | 403 | 403 | 403 | 201 | `TC-SEC-001A` (401), `TC-SEC-001B/TC-AUTH-014` y `EVT-01-STAFF` (403), `E2E-EVT-01`, `EVT-01-400` (400), `EVT-01-409` (409), `VAL-400-03`, `ERR-403-ANATOMIA` |
| 12 | GET /api/events/{eventCode} | E2E-EVT-02 | 200 PUB / **404** borrador | 200 PUB / **404** borrador | 200 PUB / **404** borrador | 200 PUB / 200 borrador | 200 PUB / 200 borrador | `E2E-EVT-02`, `TC-SEC-004`, `TC-SEC-015`, `EVT-02-STAFF`, `BIZ-V0A` (200 DRAFT), `BIZ-V0` (404), `BIZ-V1`, `EVT-02-404`, `ERR-404-ANATOMIA` |
| 13 | GET /api/events/published | E2E-EVT-03 | 200 (solo PUB) | 200 | 200 | 200 | 200 | `E2E-EVT-03`, `BIZ-V2` |
| 14 | PATCH /api/events/{eventCode}/publish | E2E-EVT-04 | 401 | 403 | 403 | 403 | 200 | `TC-SEC-002`, `TC-SEC-002B` (403), `E2E-EVT-04`, `EVT-04-409` (409), `EVT-04-404` (404) |
| 15 | POST /api/events/{eventCode}/artists/{artistId} | E2E-EVT-05 | 401 | 403 | 403 | 403 | 200 | `TC-SEC-003` (403), `EVT-05-STAFF` (403), `E2E-EVT-05`, `EVT-05-409` (409), `EVT-05-404` (404) |
| 16 | GET /api/events/by-artist | E2E-EVT-06 | 200 (solo PUB) | 200 (solo PUB) | 200 (solo PUB) | 200 (ve borradores) | 200 (ve borradores) | `E2E-EVT-06`, `TC-SEC-005`, `EVT-06-OPS`, `BIZ-V3` |
| 17 | POST /api/tickets | E2E-TKT-01 | 401 | 201 (email propio) | 403 (email ajeno) | — | — | `E2E-TKT-01/01B`, `TC-SEC-008`, `TKT-01-401` (401), `TKT-01-404` (404), `TC-BIZ-014A/B/C` (409), `VAL-400-04/05` (400) |
| 18 | GET /api/tickets/{ticketCode} | E2E-TKT-02 | 401 | 200 (OWN) / 403 (ajeno) | 403 (ajeno) | 200 (OPS) | 200 | `E2E-TKT-02`, `TC-SEC-009`, `TKT-02-STAFF`, `TKT-02-404` (404), `BIZ-V4`, `BIZ-V5` |
| 19 | GET /api/tickets/by-user | E2E-TKT-03 | 401 | 200 (propio) | 403 (ajeno) | 403 | 200 | `E2E-TKT-03`, `TC-SEC-010`, `TKT-03-ADMIN`, `TKT-03-401` (401) |
| 20 | GET /api/events/{eventCode}/tickets/paid | E2E-TKT-04 | 401 | 403 | 403 | 200 (OPS) | 200 | `E2E-TKT-04`, `TC-SEC-013`, `TKT-04-401` (401), `TC-SEC-017` (403 con rol revocado) |
| 21 | PATCH /api/tickets/{ticketCode}/cancel | E2E-TKT-05 | 401 | 200 (OWN) / 409 repetido | 403 (ajeno) | 403 (excluido) | 200 (ADM) | `E2E-TKT-05`, `TC-SEC-011`, `TKT-05-409` (409), `TKT-05-STAFF` (403) |
| 22 | PATCH /api/tickets/{ticketCode}/use | E2E-TKT-06 | 401 | 403 (dueño sin rol) | 403 (ajeno) | 200 (OPS) | 200 (ADM) | `E2E-TKT-06`, `TC-SEC-012`, `TKT-06-OWNER`, `TKT-06-409` (409), `TKT-06-401` (401) |

Notas de celdas:

- `401` de las filas 9–22 es el deny-by-default de `SecurityConfig` (anónimo en
  ruta protegida). Casos dedicados de 401 anónimo: `USR-02-401`, `TKT-01-401`,
  `TKT-03-401`, `TKT-04-401`, `TKT-06-401`, `TC-AUTH-007`; el resto se apoya en
  la misma regla con la misma petición sin `Authorization`.
- Celdas de ADMIN en cancelar/usar (filas 21–22) provienen de
  `ensureTicketOwnerOrAdmin`/`ensurePrivileged` del código; sin caso dedicado
  de ADMIN en esas dos rutas (señalado para docente: política de código, no
  ejecutada).
- STAFF/ADMIN en fila 6–7: `—` (el registro/login se ejercitan como anónimo,
  que es su vía real de uso).
- Cada `403` de mutación (filas 11, 14, 15, 17, 21, 22) exige relectura de
  integridad posterior en la bitácora (NFR-E2E-006, §8.3).

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
| FR-E2E-001 Colección PM v2.1 | Inspección de carpetas y peticiones | 22/22 rutas presentes | NOT RUN | 10 carpetas §10.1, 114 peticiones; falta importación en Postman |
| FR-E2E-002 Entorno parametrizable | `PRE-00` + revisión de ambiente | Todas vía `{{baseUrl}}` | NOT RUN | `PulsePass-local.postman_environment.json` (32 vars, secretos vacíos) |
| FR-E2E-003 Autenticación manual | `TC-AUTH-001`–`005` | `POST /api/users`, `POST /api/auth/login` | NOT RUN | §12.1/§12.2: 201→200, copia manual de `accessToken` |
| FR-E2E-004 Token por actor | `TC-AUTH-006/006B/006S/006A` | `GET /api/auth/me` | NOT RUN | Variables separadas `userAToken`, `userBToken`, `staffToken`, `adminToken` |
| FR-E2E-005 Comprobación manual | Todas (descripción §10.2 con HTTP esperado) | 22 rutas | NOT RUN | Cada petición declara ID, objetivo, esperado, revisión y evidencia |
| FR-E2E-006 Encadenamiento manual | `E2E-ART-03`→`artistId`; `E2E-EVT-01`→`eventToPublishCode`; `E2E-TKT-01/01B`→códigos de ticket | Filas 5, 11, 17–22 | NOT RUN | Checklist A4, C1, D1 indican qué copiar y dónde |
| FR-E2E-007 Matriz de autorización | `TC-SEC-001`–`017`, `TC-AUTH-007/014/015` | Tabla 1 completa | NOT RUN | 5 actores contrastados por ruta (§2.3.3) |
| FR-E2E-008 Integridad funcional | `BIZ-V0A/V0/V1/V2/V3/V4/V5` + relecturas tras cada 403 | Filas 12, 13, 16, 18, 21, 22 | NOT RUN | GET posterior tras cada transición y denegación |
| FR-E2E-009 ErrorResponse | `ERR-403-ANATOMIA`, `ERR-404-ANATOMIA`, `VAL-400-*`, `USR-02-404` | Varias | NOT RUN | 5 campos (timestamp/status/error/message/details), sin datos internos |
| FR-E2E-010 Datos controlados | Seed `pulsepass_e2e` + `fixtures/README.md` | — | NOT RUN | Restauración documentada solo sobre la BD E2E |
| FR-E2E-011 Instrucciones paso a paso | `checklist-e2e-manual.md` (Flujos A–E) | Todas | NOT RUN | Precondición, actor, petición, acción, esperado y comprobación por paso |
| FR-E2E-012 Bitácora manual | `registro-ejecucion-manual.md` (69 casos) | 22 rutas + familias §8 | NOT RUN | Columnas §16.2 con fecha/hora y resultado explícito |
| FR-E2E-013 Confidencialidad | Revisión de exportables | — | NOT RUN | Sin secretos versionados (NFR-E2E-002); `.gitignore` local; verificar también al exportar evidencias |
| FR-E2E-014 Regresión | Flujo E (nominales + errores §9.5) | Todas | NOT RUN | Cualquier 500 en nominal falla todo el recorrido |
| FR-E2E-015 Bloqueos | Regla §13.5 de la bitácora | — | NOT RUN | Casos dependientes de un `BLOCKED` van a `BLOCKED`, nunca `PASS` |
| FR-E2E-016 Repetibilidad | `runId` + seed idempotente + `.gitignore` | — | NOT RUN | `tests/e2e/fixtures/README.md` §repetibilidad |
| FR-E2E-017 Contratos heredados | `TC-HTTP` (§8.1): `VAL-400-01`–`05`, `VAL-409-01`, revisión de DTO | Varias | NOT RUN | Bodies/respuestas contrastados con los DTO reales del código (Fases 1–4 intactas) |
| NFR-E2E-001 Portabilidad | Inspección del JSON | — | NOT RUN | Collection v2.1, sin scripts (`event` = 0) |
| NFR-E2E-002 Seguridad de secretos | Búsqueda de secretos en el repo | — | NOT RUN | `.env`, tokens y contraseñas no versionados; vars `secret` vacías |
| NFR-E2E-003 Determinismo | Seed con `ON CONFLICT` + `runId` | — | NOT RUN | Datos propios de cada corrida |
| NFR-E2E-004 Legibilidad | Carpetas §10.1 + IDs rastreables | — | NOT RUN | Nombres de caso expresivos en las 114 peticiones |
| NFR-E2E-005 Diagnóstico | Bitácora con actor y escenario | — | NOT RUN | Sin datos sensibles en las columnas |
| NFR-E2E-006 Integridad | Relecturas de `TC-SEC-003/008`–`012` | Filas 15, 17–22 | NOT RUN | Ausencia de mutación persistida tras cada denegación |
| NFR-E2E-007 Compatibilidad | `git status` de `src/` | — | NOT RUN | Esta fase no modifica la API ni `src/` |

---

## Consolidación de cobertura

| Corte | Estado |
|---|---|
| Rutas con ≥1 caso nominal previsto en la colección | **22/22** (ejecución pendiente: 0/22 en bitácora) |
| Actores contrastados por la matriz | 5: ANONYMOUS, USER_A, USER_B, STAFF, ADMIN (+ INACTIVE_USER en `TC-AUTH-013`) |
| Casos totales registrados en la bitácora | 69 (22 §7 + 15 §8.2 + 17 §8.3 + 15 §8.4), todos `NOT RUN` |
| Peticiones en la colección | 114 en 10 carpetas, 0 scripts |
| Defectos registrados | Ninguno (sin ejecución) |
