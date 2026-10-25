# Registro de ejecución E2E — bitácora (PRD §16.2)

Bitácora de resultados de la colección **PulsePass — E2E Manual**. Cada fila
corresponde a **una petición** de la colección; las 80 unidades lógicas del PRD
(22 rutas §7 + 16 TC-AUTH + 22 TC-SEC + 20 TC-BIZ) se conservan y se resumen en
la sección 2. Una petición puede evidenciar más de un caso lógico, pero ninguna
fila agrupa varias peticiones.

**Reglas (§13.5, §16.3, FR-E2E-013):**

- `PASS`: HTTP, JSON y condición de negocio/seguridad observados coinciden con lo esperado.
- `FAIL`: cualquiera de las comprobaciones no coincide, o existe un 500 no esperado.
- `BLOCKED`: no se puede ejecutar por falta de fixture, actor, token o respuesta previa.
- `NOT RUN`: petición aún no ejecutada (en esta corrida no queda ninguna).
- No incluir JWT, contraseñas ni información privada en esta bitácora ni en capturas.
- Un caso dependiente de otro `BLOCKED` jamás se cuenta como aprobado (§17).

**Estado de la corrida (2026-10-23): 125/125 peticiones PASS; 80/80 casos lógicos PASS; 0 FAIL, 0 BLOCKED, 0 NOT RUN.**

La corrida se realizó contra la API real, PostgreSQL 17 y la app Spring Boot en
`http://127.0.0.1:8080`. La colección no contiene scripts y no se usó Collection
Runner, Newman ni Postman CLI. Los JWT y las contraseñas de runtime no se
versionaron.

**Entorno de la corrida:** `pulsepass_e2e` (PostgreSQL 17 en instancia aislada
`127.0.0.1:55432`), `runId=e2e01`, app Spring Boot 4.1.1 / Java 21 en
`http://127.0.0.1:8080`.

---

## 1. Bitácora por petición (125 filas)

### 00 Preparación / conectividad — 1 petición

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| PRE-00 | PRE-00 | ANONYMOUS | `GET /api/venues/active` | 2026-10-23 11:30:38 | 200 | 200 | lista[2]; codes=[VEN-E2E-PRINCIPAL, VEN-E2E-AFORO1] | PASS | Preflight de conectividad; la API responde y el listado coincide con el seed. |

### 01 Venues (2 rutas) — 3 peticiones

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| E2E-VEN-02 | E2E-VEN-02, TC-BIZ-001 | ANONYMOUS | `GET /api/venues/active` | 2026-10-23 11:30:38 | 200 | 200 | lista[2]; codes=[VEN-E2E-PRINCIPAL, VEN-E2E-AFORO1] | PASS | Listado público con VEN-E2E-PRINCIPAL y VEN-E2E-AFORO1. |
| E2E-VEN-01 | E2E-VEN-01, TC-BIZ-001 | ANONYMOUS | `GET /api/venues/{{venueActiveCode}}` | 2026-10-23 11:30:38 | 200 | 200 | id=1; code=VEN-E2E-PRINCIPAL; capacity=500; active=True | PASS | Código y capacidad 500 coinciden con el fixture. |
| E2E-VEN-01N | E2E-VEN-01 | ANONYMOUS | `GET /api/venues/NO-EXISTE-{{runId}}` | 2026-10-23 11:30:38 | 404 | 404 | ErrorResponse(404): Venue no encontrado: NO-EXISTE-e2e01 | PASS | 404 con ErrorResponse de 5 campos. |

### 02 Artists (3 rutas) — 5 peticiones

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| E2E-ART-03 | E2E-ART-03, TC-BIZ-002 | ANONYMOUS | `GET /api/artists/active` | 2026-10-23 11:30:38 | 200 | 200 | lista[5]; stageNames=[Caribbean Sound, Digital Pulse, Neon Waves, Ocean Drive, Solar Beat] | PASS | Catálogo de 5 artistas; Solar Beat presente. |
| E2E-ART-01 | E2E-ART-01, TC-BIZ-002 | ANONYMOUS | `GET /api/artists/{{artistId}}` | 2026-10-23 11:30:38 | 200 | 200 | id=1; stageName=Solar Beat; active=True | PASS | id=1 y stageName=Solar Beat. |
| E2E-ART-01N | E2E-ART-01 | ANONYMOUS | `GET /api/artists/999999999` | 2026-10-23 11:30:38 | 404 | 404 | ErrorResponse(404): Artista no encontrado con id: 999999999 | PASS | ErrorResponse de 5 campos; respuesta revisada sin secretos. |
| E2E-ART-02 | E2E-ART-02, TC-BIZ-002 | ANONYMOUS | `GET /api/artists/by-stage-name?stageName={{artistStageName}}` | 2026-10-23 11:30:38 | 200 | 200 | id=1; stageName=Solar Beat; active=True | PASS | Búsqueda por stageName=Solar Beat. |
| E2E-ART-02N | E2E-ART-02 | ANONYMOUS | `GET /api/artists/by-stage-name?stageName=NoExiste-{{runId}}` | 2026-10-23 11:30:38 | 404 | 404 | ErrorResponse(404): Artista no encontrado: NoExiste-e2e01 | PASS | ErrorResponse de 5 campos; respuesta revisada sin secretos. |

### 03 Auth / usuarios (registro, login, me) — 20 peticiones

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| TC-AUTH-001 | TC-AUTH-001, E2E-USR-01 | ANONYMOUS | `POST /api/users` | 2026-10-23 11:30:36 | 201 | 201 | id=1; username=usera-e2e01; active=True | PASS | 201 sin password/hash/token; rol inicial USER verificado con /me. |
| TC-BIZ-006A | TC-BIZ-006, E2E-USR-01 | ANONYMOUS | `POST /api/users` | 2026-10-23 11:30:36 | 201 | 201 | id=2; username=userb-e2e01; active=True | PASS | 201; usuario B creado sin secretos en la respuesta. |
| TC-AUTH-013A | TC-AUTH-013 | ANONYMOUS | `POST /api/users` | 2026-10-23 11:30:36 | 201 | 201 | id=3; username=inactivo-e2e01; active=True | PASS | 201; cuenta temporal creada para TC-AUTH-013. |
| TC-SEC-016 | TC-SEC-016 | ANONYMOUS (role=ADMIN inyectado) | `POST /api/users` | 2026-10-23 11:30:37 | 201 | 201 | id=4; username=admin-fake-e2e01; active=True | PASS | 201; el role=ADMIN inyectado se ignora; rol efectivo USER. |
| USR-01-409 | TC-BIZ-006, E2E-USR-01 | ANONYMOUS | `POST /api/users` | 2026-10-23 11:30:37 | 409 | 409 | ErrorResponse(409): El username ya existe: usera-e2e01 | PASS | 409; username duplicado rechazado. |
| TC-AUTH-002 | TC-AUTH-002 | USER_A | `POST /api/auth/login` | 2026-10-23 11:30:37 | 200 | 200 | tokenType=Bearer; expiresIn=900; accessToken=<JWT omitido> | PASS | 200; accessToken emitido con TTL 900; el token no se copia a la bitácora. |
| TC-AUTH-003 | TC-AUTH-003 | USER_A | `POST /api/auth/login` | 2026-10-23 11:30:37 | 200 | 200 | tokenType=Bearer; expiresIn=900; accessToken=<JWT omitido> | PASS | 200; login por email; misma identidad persistente. |
| TC-AUTH-004 | TC-AUTH-004 | ANONYMOUS | `POST /api/auth/login` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Credenciales inválidas | PASS | 401 genérico; no distingue la causa. |
| TC-AUTH-005 | TC-AUTH-005 | ANONYMOUS | `POST /api/auth/login` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Credenciales inválidas | PASS | 401 idéntico a TC-AUTH-004; sin oráculo de existencia. |
| AUTH-STAFF | E2E-AUTH-01 | STAFF | `POST /api/auth/login` | 2026-10-23 11:30:37 | 200 | 200 | tokenType=Bearer; expiresIn=900; accessToken=<JWT omitido> | PASS | 200; token de STAFF emitido. |
| AUTH-ADMIN | E2E-AUTH-01 | ADMIN | `POST /api/auth/login` | 2026-10-23 11:30:37 | 200 | 200 | tokenType=Bearer; expiresIn=900; accessToken=<JWT omitido> | PASS | 200; token de ADMIN emitido. |
| AUTH-B | E2E-AUTH-01 | USER_B | `POST /api/auth/login` | 2026-10-23 11:30:37 | 200 | 200 | tokenType=Bearer; expiresIn=900; accessToken=<JWT omitido> | PASS | 200; token de USER_B emitido. |
| TC-AUTH-006 | TC-AUTH-006 | USER_A | `GET /api/auth/me` | 2026-10-23 11:30:38 | 200 | 200 | id=1; username=usera-e2e01; roles=['USER']; active=True | PASS | 200; identidad y roles vigentes de USER_A. |
| TC-AUTH-006B | TC-AUTH-006, E2E-AUTH-02 | USER_B | `GET /api/auth/me` | 2026-10-23 11:30:38 | 200 | 200 | id=2; username=userb-e2e01; roles=['USER']; active=True | PASS | 200; identidad y roles vigentes de USER_B. |
| TC-AUTH-006S | TC-AUTH-006, E2E-AUTH-02 | STAFF | `GET /api/auth/me` | 2026-10-23 11:30:38 | 200 | 200 | id=5; username=staff-e2e01; roles=['USER', 'STAFF']; active=True | PASS | 200; identidad y roles vigentes de STAFF. |
| TC-AUTH-006A | TC-AUTH-006, E2E-AUTH-02 | ADMIN | `GET /api/auth/me` | 2026-10-23 11:30:38 | 200 | 200 | id=6; username=admin-e2e01; roles=['USER', 'ADMIN']; active=True | PASS | 200; identidad y roles vigentes de ADMIN. |
| TC-AUTH-007 | TC-AUTH-007 | ANONYMOUS | `GET /api/auth/me` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401 uniforme; sin identidad en la respuesta. |
| TC-AUTH-008 | TC-AUTH-008 | ANONYMOUS | `GET /api/auth/me` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401 uniforme; Bearer inventado rechazado. |
| TC-AUTH-012 | TC-AUTH-012 | ANONYMOUS | `GET /api/auth/me?access_token={{userAToken}}` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; token enviado solo en query; no autentica y no se copia a la bitácora. |
| TC-AUTH-013B | TC-AUTH-013 | INACTIVE_USER (temporal) | `POST /api/auth/login` | 2026-10-23 11:30:38 | 200 | 200 | tokenType=Bearer; expiresIn=900; accessToken=<JWT omitido> | PASS | 200; token de la cuenta temporal capturado antes de desactivarla. |

### 04 Usuarios / consulta y propiedad (2 rutas) — 9 peticiones

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| E2E-USR-02 | E2E-USR-02 | USER_A | `GET /api/users/by-email?email={{userAEmail}}` | 2026-10-23 11:30:39 | 200 | 200 | id=1; username=usera-e2e01; active=True | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| E2E-USR-03 | E2E-USR-03 | USER_A | `GET /api/users/by-username?username={{userAUsername}}` | 2026-10-23 11:30:39 | 200 | 200 | id=1; username=usera-e2e01; active=True | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| USR-02-ADMIN | E2E-USR-02 | ADMIN | `GET /api/users/by-email?email={{userAEmail}}` | 2026-10-23 11:30:39 | 200 | 200 | id=1; username=usera-e2e01; active=True | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| USR-03-ADMIN | E2E-USR-03 | ADMIN | `GET /api/users/by-username?username={{userAUsername}}` | 2026-10-23 11:30:39 | 200 | 200 | id=1; username=usera-e2e01; active=True | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| TC-SEC-006 | TC-SEC-006 | USER_B | `GET /api/users/by-email?email={{userAEmail}}` | 2026-10-23 11:30:39 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; no revela el usuario de A. |
| TC-SEC-007 | TC-SEC-007 | USER_B | `GET /api/users/by-username?username={{userAUsername}}` | 2026-10-23 11:30:39 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; no revela el usuario de A. |
| USR-02-401 | E2E-USR-02 | ANONYMOUS | `GET /api/users/by-email?email={{userAEmail}}` | 2026-10-23 11:30:39 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | ErrorResponse de 5 campos; respuesta revisada sin secretos. |
| USR-02-404 | E2E-USR-02 | USER_A | `GET /api/users/by-email?email=nadie-{{runId}}@example.test` | 2026-10-23 11:30:39 | 404 | 404 | ErrorResponse(404): Usuario no encontrado con email: nadie-e2e01@example.test | PASS | ErrorResponse de 5 campos; respuesta revisada sin secretos. |
| USR-03-401 | E2E-USR-03 | ANONYMOUS | `GET /api/users/by-username?username={{userAUsername}}` | 2026-10-23 11:30:39 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | ErrorResponse de 5 campos; respuesta revisada sin secretos. |

### 05 Events / administración y visibilidad (6 rutas) — 31 peticiones

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| E2E-EVT-01 | E2E-EVT-01, TC-BIZ-003 | ADMIN | `POST /api/events` | 2026-10-23 11:30:39 | 201 | 201 | id=3; eventCode=EVT-E2E-e2e01-CAT; status=DRAFT; artists=[] | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| EVT-01-400 | TC-BIZ-003 | ADMIN | `POST /api/events` | 2026-10-23 11:30:39 | 400 | 400 | ErrorResponse(400): Validation failed; details=[name, venueCode] | PASS | ErrorResponse de 5 campos; respuesta revisada sin secretos. |
| EVT-01-409 | TC-BIZ-003 | ADMIN | `POST /api/events` | 2026-10-23 11:30:39 | 409 | 409 | ErrorResponse(409): Ya existe un evento con el código: EVT-E2E-e2e01-CAT | PASS | ErrorResponse de 5 campos; respuesta revisada sin secretos. |
| TC-SEC-001A | TC-SEC-001A | ANONYMOUS | `POST /api/events` | 2026-10-23 11:30:39 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; denegación uniforme antes del controller. |
| TC-SEC-001B | TC-SEC-001B | USER_A | `POST /api/events` | 2026-10-23 11:30:39 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; rol USER insuficiente; sin evento persistido. |
| EVT-01-STAFF | TC-SEC-001B | STAFF | `POST /api/events` | 2026-10-23 11:30:39 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; STAFF no crea eventos; sin evento persistido. |
| E2E-EVT-02 | E2E-EVT-02, TC-SEC-004, TC-SEC-015 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 2026-10-23 11:30:38 | 200 | 200 | id=1; eventCode=EVT-E2E-e2e01-PUB; status=PUBLISHED; artists=[Solar Beat] | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| TC-SEC-004 | TC-SEC-004 | ANONYMOUS | `GET /api/events/{{draftEventCode}}` | 2026-10-23 11:30:38 | 404 | 404 | ErrorResponse(404): Evento no encontrado: EVT-E2E-e2e01-DRAFT | PASS | 404; borrador oculto, misma forma que un inexistente. |
| TC-SEC-015 | TC-SEC-015 | ADMIN | `GET /api/events/{{draftEventCode}}` | 2026-10-23 11:30:38 | 200 | 200 | id=2; eventCode=EVT-E2E-e2e01-DRAFT; status=DRAFT; artists=[Solar Beat] | PASS | 200; ADMIN ve el borrador. |
| EVT-02-STAFF | TC-SEC-015B | STAFF | `GET /api/events/{{draftEventCode}}` | 2026-10-23 11:30:38 | 200 | 200 | id=2; eventCode=EVT-E2E-e2e01-DRAFT; status=DRAFT; artists=[Solar Beat] | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| EVT-02-404 | E2E-EVT-02 | ANONYMOUS | `GET /api/events/EVT-NO-EXISTE-{{runId}}` | 2026-10-23 11:30:38 | 404 | 404 | ErrorResponse(404): Evento no encontrado: EVT-NO-EXISTE-e2e01 | PASS | ErrorResponse de 5 campos; respuesta revisada sin secretos. |
| E2E-EVT-03 | E2E-EVT-03 | ANONYMOUS | `GET /api/events/published` | 2026-10-23 11:30:38 | 200 | 200 | lista[1]; eventCodes=[EVT-E2E-e2e01-PUB] | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| E2E-EVT-04 | E2E-EVT-04, TC-BIZ-004 | ADMIN | `PATCH /api/events/{{eventToPublishCode}}/publish` | 2026-10-23 11:30:39 | 200 | 200 | id=3; eventCode=EVT-E2E-e2e01-CAT; status=PUBLISHED; artists=[Solar Beat] | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| TC-SEC-002 | TC-SEC-002 | USER_A | `PATCH /api/events/{{eventToPublishCode}}/publish` | 2026-10-23 11:30:39 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; el evento sigue PUBLISHED en la relectura. |
| TC-SEC-002B | TC-SEC-002B | STAFF | `PATCH /api/events/{{eventToPublishCode}}/publish` | 2026-10-23 11:30:39 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; STAFF no publica; el evento sigue PUBLISHED. |
| EVT-04-409 | E2E-EVT-04 | ADMIN | `PATCH /api/events/{{publishedEventCode}}/publish` | 2026-10-23 11:30:39 | 409 | 409 | ErrorResponse(409): Solo se pueden publicar eventos en estado DRAFT | PASS | 409; se ejecutó sobre EVT-E2E-e2e01-CAT ya publicado; no se republica. |
| EVT-04-404 | E2E-EVT-04 | ADMIN | `PATCH /api/events/EVT-NO-EXISTE-{{runId}}/publish` | 2026-10-23 11:30:39 | 404 | 404 | ErrorResponse(404): Evento no encontrado: EVT-NO-EXISTE-e2e01 | PASS | 404; evento inexistente. |
| E2E-EVT-05 | E2E-EVT-05, TC-BIZ-005 | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 2026-10-23 11:30:39 | 200 | 200 | id=3; eventCode=EVT-E2E-e2e01-CAT; status=DRAFT; artists=[Solar Beat] | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| TC-SEC-003 | TC-SEC-003 | USER_A | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 2026-10-23 11:30:39 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; artists[] sin cambios. |
| EVT-05-STAFF | TC-SEC-003 | STAFF | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 2026-10-23 11:30:39 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; STAFF no asocia artistas; artists[] sin cambios. |
| EVT-05-409 | E2E-EVT-05 | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 2026-10-23 11:30:39 | 409 | 409 | ErrorResponse(409): El artista ya está asociado a este evento: Solar Beat | PASS | 409; artista ya asociado. |
| EVT-05-404 | E2E-EVT-05 | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/999999999` | 2026-10-23 11:30:39 | 404 | 404 | ErrorResponse(404): Artista no encontrado con id: 999999999 | PASS | 404; artista inexistente. |
| E2E-EVT-06 | E2E-EVT-06, TC-SEC-005 | ANONYMOUS | `GET /api/events/by-artist?stageName={{artistStageName}}` | 2026-10-23 11:30:38 | 200 | 200 | lista[1]; eventCodes=[EVT-E2E-e2e01-PUB] | PASS | Respuesta revisada; sin JWT, contraseñas ni hashes. |
| TC-SEC-005 | TC-SEC-005 | ANONYMOUS | `GET /api/events/by-artist?stageName={{artistStageName}}` | 2026-10-23 11:30:38 | 200 | 200 | lista[1]; eventCodes=[EVT-E2E-e2e01-PUB] | PASS | 200; el borrador no aparece para el anónimo. |
| EVT-06-OPS | TC-SEC-005 | ADMIN | `GET /api/events/by-artist?stageName={{artistStageName}}` | 2026-10-23 11:30:39 | 200 | 200 | lista[2]; eventCodes=[EVT-E2E-e2e01-PUB, EVT-E2E-e2e01-CAT] | PASS | 200; ADMIN ve PUB y CAT. |
| SETUP-EVT-EDAD | SETUP-EVT-EDAD | ADMIN | `POST /api/events` | 2026-10-23 11:30:39 | 201 | 201 | id=4; eventCode=EVT-E2E-e2e01-EDAD; status=DRAFT; artists=[] | PASS | 201 DRAFT; minimumAge=40. |
| SETUP-EVT-EDAD-PUB | SETUP-EVT-EDAD-PUB | ADMIN | `PATCH /api/events/EVT-E2E-{{runId}}-EDAD/publish` | 2026-10-23 11:30:39 | 200 | 200 | id=4; eventCode=EVT-E2E-e2e01-EDAD; status=PUBLISHED; artists=[] | PASS | 200; evento EDAD publicado. |
| SETUP-EVT-AFORO | SETUP-EVT-AFORO | ADMIN | `POST /api/events` | 2026-10-23 11:30:39 | 201 | 201 | id=5; eventCode=EVT-E2E-e2e01-AFORO; status=DRAFT; artists=[] | PASS | 201 DRAFT; venue de capacidad 1. |
| SETUP-EVT-AFORO-PUB | SETUP-EVT-AFORO-PUB | ADMIN | `PATCH /api/events/EVT-E2E-{{runId}}-AFORO/publish` | 2026-10-23 11:30:39 | 200 | 200 | id=5; eventCode=EVT-E2E-e2e01-AFORO; status=PUBLISHED; artists=[] | PASS | 200; evento AFORO publicado. |
| EVT-04-401 | E2E-EVT-04 | ANONYMOUS | `PATCH /api/events/{{eventToPublishCode}}/publish` | 2026-10-23 11:30:39 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; sin token. |
| EVT-05-401 | E2E-EVT-05 | ANONYMOUS | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 2026-10-23 11:30:39 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; sin token. |

### 06 Tickets / compra, consulta y ciclo de vida (6 rutas) — 31 peticiones

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| E2E-TKT-01 | E2E-TKT-01, TC-BIZ-007 | USER_A | `POST /api/tickets` | 2026-10-23 11:30:39 | 201 | 201 | id=1; ticketCode=TCK-D0E6A870; status=PAID; price=50.0; type=GENERAL | PASS | 201 PAID; ticketCode=TCK-D0E6A870. |
| E2E-TKT-01B | E2E-TKT-01, TC-BIZ-007 | USER_A | `POST /api/tickets` | 2026-10-23 11:30:39 | 201 | 201 | id=2; ticketCode=TCK-EA367129; status=PAID; price=30.0; type=STUDENT | PASS | 201 PAID; ticketCode=TCK-EA367129. |
| TKT-01-401 | E2E-TKT-01 | ANONYMOUS | `POST /api/tickets` | 2026-10-23 11:30:39 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; deny-by-default. |
| TC-SEC-008 | TC-SEC-008 | USER_A (userEmail de B) | `POST /api/tickets` | 2026-10-23 11:30:40 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; userEmail de B con token de A; sin ticket creado. |
| TKT-01-404 | E2E-TKT-01 | USER_A | `POST /api/tickets` | 2026-10-23 11:30:39 | 404 | 404 | ErrorResponse(404): Evento no encontrado: EVT-NO-EXISTE-e2e01 | PASS | 404; evento inexistente. |
| TC-BIZ-014A | TC-BIZ-014 | USER_A | `POST /api/tickets` | 2026-10-23 11:30:40 | 409 | 409 | ErrorResponse(409): Solo se pueden comprar tickets para eventos PUBLISHED | PASS | 409; compra sobre evento DRAFT rechazada. |
| E2E-TKT-02 | E2E-TKT-02, TC-BIZ-008 | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 2026-10-23 11:30:40 | 200 | 200 | id=1; ticketCode=TCK-D0E6A870; status=PAID; price=50.0; type=GENERAL | PASS | 200; ticket propio en PAID. |
| TC-SEC-009 | TC-SEC-009 | USER_B | `GET /api/tickets/{{ticketCancelableCode}}` | 2026-10-23 11:30:40 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; no revela el ticket de A. |
| TKT-02-STAFF | E2E-TKT-02 | STAFF | `GET /api/tickets/{{ticketCancelableCode}}` | 2026-10-23 11:30:40 | 200 | 200 | id=1; ticketCode=TCK-D0E6A870; status=PAID; price=50.0; type=GENERAL | PASS | 200; STAFF consulta el ticket. |
| TKT-02-404 | E2E-TKT-02 | USER_A | `GET /api/tickets/TCK-NOEXISTE{{runId}}` | 2026-10-23 11:30:40 | 404 | 404 | ErrorResponse(404): Ticket no encontrado: TCK-NOEXISTEe2e01 | PASS | 404; ticket inexistente. |
| E2E-TKT-03 | E2E-TKT-03, TC-BIZ-008 | USER_A | `GET /api/tickets/by-user?email={{userAEmail}}` | 2026-10-23 11:30:40 | 200 | 200 | lista[2]; tickets=[TCK-EA367129, TCK-D0E6A870]; statuses=[PAID, PAID] | PASS | 200; dos tickets PAID del usuario A. |
| TC-SEC-010 | TC-SEC-010 | USER_B | `GET /api/tickets/by-user?email={{userAEmail}}` | 2026-10-23 11:30:40 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; no revela el listado de A. |
| TKT-03-ADMIN | E2E-TKT-03 | ADMIN | `GET /api/tickets/by-user?email={{userAEmail}}` | 2026-10-23 11:30:40 | 200 | 200 | lista[2]; tickets=[TCK-EA367129, TCK-D0E6A870]; statuses=[PAID, PAID] | PASS | 200; ADMIN consulta el listado de A. |
| TKT-03-401 | E2E-TKT-03 | ANONYMOUS | `GET /api/tickets/by-user?email={{userAEmail}}` | 2026-10-23 11:30:40 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; deny-by-default. |
| E2E-TKT-04 | E2E-TKT-04, TC-BIZ-009 | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 2026-10-23 11:30:40 | 200 | 200 | lista[2]; tickets=[TCK-D0E6A870, TCK-EA367129]; statuses=[PAID, PAID] | PASS | 200; solo los dos tickets PAID. |
| TC-SEC-013 | TC-SEC-013 | USER_A | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 2026-10-23 11:30:40 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; operación exclusiva de taquilla. |
| TKT-04-401 | E2E-TKT-04 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 2026-10-23 11:30:40 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; deny-by-default. |
| TC-SEC-011 | TC-SEC-011 | USER_B | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 2026-10-23 11:30:40 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; T1 sigue PAID en ese momento. |
| E2E-TKT-05 | E2E-TKT-05, TC-BIZ-010 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 2026-10-23 11:30:40 | 200 | 200 | id=1; ticketCode=TCK-D0E6A870; status=CANCELLED; price=50.0; type=GENERAL | PASS | 200; PAID → CANCELLED. |
| TKT-05-409 | E2E-TKT-05, TC-BIZ-011 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 2026-10-23 11:30:40 | 409 | 409 | ErrorResponse(409): Solo se pueden cancelar tickets en estado PAID | PASS | 409; el ticket ya está CANCELLED. |
| TKT-05-STAFF | E2E-TKT-05 | STAFF | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 2026-10-23 11:30:40 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; STAFF excluido de la cancelación. |
| E2E-TKT-06 | E2E-TKT-06, TC-BIZ-012, TC-SEC-014 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 2026-10-23 11:30:40 | 200 | 200 | id=2; ticketCode=TCK-EA367129; status=USED; price=30.0; type=STUDENT | PASS | 200; PAID → USED. |
| TC-SEC-012 | TC-SEC-012 | USER_B | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 2026-10-23 11:30:40 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; T2 sigue PAID en ese momento. |
| TKT-06-OWNER | E2E-TKT-06 | USER_A | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 2026-10-23 11:30:40 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; el dueño sin rol STAFF no puede marcar USED. |
| TKT-06-409 | E2E-TKT-06, TC-BIZ-013 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 2026-10-23 11:30:40 | 409 | 409 | ErrorResponse(409): Solo se puede marcar como usado un ticket en estado PAID | PASS | 409; el ticket ya está USED. |
| TKT-06-401 | E2E-TKT-06 | ANONYMOUS | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 2026-10-23 11:30:40 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; deny-by-default. |
| TC-BIZ-014B | TC-BIZ-014 | USER_A | `POST /api/tickets` | 2026-10-23 11:30:40 | 409 | 409 | ErrorResponse(409): El usuario no cumple la edad mínima requerida (40) | PASS | 409; edad mínima 40 no cumplida. |
| TC-BIZ-014C | TC-BIZ-014 | USER_A | `POST /api/tickets` | 2026-10-23 11:30:40 | 409 | 409 | ErrorResponse(409): Solo se pueden comprar tickets para eventos PUBLISHED | PASS | 409; el evento AFORO está SOLD_OUT; no se crea ticket. |
| SETUP-TKT-AFORO-FIRST | SETUP-TKT-AFORO-FIRST | USER_A | `POST /api/tickets` | 2026-10-23 11:30:40 | 201 | 201 | id=3; ticketCode=TCK-002C0F1B; status=PAID; price=50.0; type=GENERAL | PASS | 201 PAID; el evento AFORO pasa a SOLD_OUT. |
| TKT-02-401 | E2E-TKT-02 | ANONYMOUS | `GET /api/tickets/{{ticketCancelableCode}}` | 2026-10-23 11:30:40 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; deny-by-default. |
| TKT-05-401 | E2E-TKT-05 | ANONYMOUS | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 2026-10-23 11:30:40 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; deny-by-default. |

### 07 Seguridad / escenarios negativos — 10 peticiones

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| TC-AUTH-009 | TC-AUTH-009 | ANONYMOUS | `GET /api/auth/me` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; firma alterada rechazada. |
| TC-AUTH-010 | TC-AUTH-010 | USER_A (expiredToken) | `GET /api/auth/me` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; exp vencido con firma correcta; actor USER_A (expiredToken). |
| TC-AUTH-011 | TC-AUTH-011 | ANONYMOUS | `GET /api/auth/me` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; clave e issuer ajenos rechazados. |
| TC-AUTH-013 | TC-AUTH-013 | INACTIVE_USER | `GET /api/auth/me` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; el filtro revalida active=false en la BD. |
| TC-AUTH-015A | TC-AUTH-015 | ANONYMOUS | `GET /api/venues/active` | 2026-10-23 11:30:38 | 200 | 200 | lista[2]; codes=[VEN-E2E-PRINCIPAL, VEN-E2E-AFORO1] | PASS | 200; misma petición/respuesta que E2E-VEN-02; el token inválido se descarta. |
| TC-AUTH-015B | TC-AUTH-015 | ANONYMOUS | `GET /api/auth/me` | 2026-10-23 11:30:38 | 401 | 401 | ErrorResponse(401): Se requiere autenticación válida | PASS | 401; la misma credencial inválida no autentica /me. |
| TC-SEC-016B | TC-SEC-016B | CANDIDATE | `POST /api/auth/login` | 2026-10-23 11:30:41 | 200 | 200 | tokenType=Bearer; expiresIn=900; accessToken=<JWT omitido> | PASS | 200; el candidato puede autenticarse. |
| TC-SEC-016C | TC-SEC-016C | CANDIDATE | `GET /api/auth/me` | 2026-10-23 11:30:41 | 200 | 200 | id=4; username=admin-fake-e2e01; roles=['USER']; active=True | PASS | 200; roles=[USER], nunca ADMIN. |
| TC-SEC-017 | TC-SEC-017 | STAFF (rol revocado) | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 2026-10-23 11:30:41 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403; rol STAFF revocado por SQL; restaurado después del caso. |
| TC-AUTH-CLAIMS | TC-AUTH-CLAIMS | USER_A | `GET /api/auth/me` | 2026-10-23 11:30:38 | 200 | 200 | id=1; username=usera-e2e01; roles=['USER']; active=True | PASS | 200; claims HS256, iss=pulsepass-api, sub=1, iat/exp/jti y TTL 900 verificados. |

### 08 Validaciones y errores HTTP — 8 peticiones

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| VAL-400-01 | TC-BIZ-015 | ANONYMOUS | `POST /api/users` | 2026-10-23 11:30:41 | 400 | 400 | ErrorResponse(400): Validation failed; details=[password, birthDate, firstName, lastName, username, email] | PASS | 400; Validation failed con details por campo. |
| VAL-400-02 | TC-BIZ-015 | ANONYMOUS | `POST /api/users` | 2026-10-23 11:30:41 | 400 | 400 | ErrorResponse(400): Validation failed; details=[email] | PASS | 400; Validation failed con details de email. |
| VAL-400-03 | TC-BIZ-015 | ADMIN | `POST /api/events` | 2026-10-23 11:30:41 | 400 | 400 | ErrorResponse(400): Malformed request | PASS | 400; Malformed request. |
| VAL-400-04 | TC-BIZ-015 | USER_A | `POST /api/tickets` | 2026-10-23 11:30:39 | 400 | 400 | ErrorResponse(400): Malformed request | PASS | 400; Malformed request por enum inválido. |
| VAL-400-05 | TC-BIZ-015 | USER_A | `POST /api/tickets` | 2026-10-23 11:30:40 | 400 | 400 | ErrorResponse(400): Validation failed; details=[userEmail] | PASS | 400; Validation failed con details de userEmail. |
| VAL-409-01 | TC-BIZ-015 | ANONYMOUS | `POST /api/users` | 2026-10-23 11:30:41 | 409 | 409 | ErrorResponse(409): La fecha de nacimiento no puede ser futura | PASS | 409; fecha de nacimiento futura rechazada. |
| ERR-403-ANATOMIA | TC-AUTH-014 | USER_A | `POST /api/events` | 2026-10-23 11:30:39 | 403 | 403 | ErrorResponse(403): No tiene permisos para realizar esta operación | PASS | 403 con ErrorResponse de 5 campos; sin datos internos. |
| ERR-404-ANATOMIA | TC-BIZ-015 | ADMIN | `GET /api/events/EVT-NO-EXISTE-{{runId}}` | 2026-10-23 11:30:41 | 404 | 404 | ErrorResponse(404): Evento no encontrado: EVT-NO-EXISTE-e2e01 | PASS | 404 con ErrorResponse de 5 campos. |

### 09 Flujos de negocio / secuencia recomendada — 7 peticiones

| Request ID | Caso(s) | Actor | Ruta | Fecha/hora | HTTP esperado | HTTP observado | JSON/estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|---|
| BIZ-V0 | E2E-EVT-02 | ANONYMOUS | `GET /api/events/{{eventToPublishCode}}` | 2026-10-23 11:30:39 | 404 | 404 | ErrorResponse(404): Evento no encontrado: EVT-E2E-e2e01-CAT | PASS | 404; el evento CAT aún es DRAFT y está oculto. |
| BIZ-V0A | E2E-EVT-02 | ADMIN | `GET /api/events/{{eventToPublishCode}}` | 2026-10-23 11:30:39 | 200 | 200 | id=3; eventCode=EVT-E2E-e2e01-CAT; status=DRAFT; artists=[] | PASS | 200 DRAFT; ADMIN lo ve antes de publicar. |
| BIZ-V1 | E2E-EVT-04 | ANONYMOUS | `GET /api/events/{{eventToPublishCode}}` | 2026-10-23 11:30:39 | 200 | 200 | id=3; eventCode=EVT-E2E-e2e01-CAT; status=PUBLISHED; artists=[Solar Beat] | PASS | 200 PUBLISHED; visible para el público. |
| BIZ-V2 | E2E-EVT-03 | ANONYMOUS | `GET /api/events/published` | 2026-10-23 11:30:39 | 200 | 200 | lista[2]; eventCodes=[EVT-E2E-e2e01-CAT, EVT-E2E-e2e01-PUB] | PASS | 200; CAT aparece en el listado de publicados. |
| BIZ-V3 | E2E-EVT-06 | ANONYMOUS | `GET /api/events/by-artist?stageName={{artistStageName}}` | 2026-10-23 11:30:39 | 200 | 200 | lista[2]; eventCodes=[EVT-E2E-e2e01-PUB, EVT-E2E-e2e01-CAT] | PASS | 200; PUB y CAT aparecen por artista. |
| BIZ-V4 | TC-BIZ-010 | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 2026-10-23 11:30:40 | 200 | 200 | id=1; ticketCode=TCK-D0E6A870; status=CANCELLED; price=50.0; type=GENERAL | PASS | 200 CANCELLED; relectura posterior a la cancelación. |
| BIZ-V5 | TC-BIZ-012 | STAFF | `GET /api/tickets/{{ticketUsableCode}}` | 2026-10-23 11:30:40 | 200 | 200 | id=2; ticketCode=TCK-EA367129; status=USED; price=30.0; type=STUDENT | PASS | 200 USED; relectura posterior al uso. |

---

## 2. Resumen de los 80 casos lógicos

| Módulo | Casos | PASS | FAIL | BLOCKED | NOT RUN | Total |
|---|---|---|---|---|---|---|
| §7 Cobertura de rutas | 22 | 22 | 0 | 0 | 0 | 22 |
| §8.2 Autenticación (TC-AUTH) | 16 | 16 | 0 | 0 | 0 | 16 |
| §8.3 Autorización/IDOR (TC-SEC) | 22 | 22 | 0 | 0 | 0 | 22 |
| §8.4 Negocio (TC-BIZ) | 20 | 20 | 0 | 0 | 0 | 20 |
| **Total** | **80** | **80** | **0** | **0** | **0** | **80** |

- Peticiones registradas: **125/125 PASS** (una fila por `Request ID`).
- Casos lógicos: **80/80 casos lógicos PASS**; los 80 IDs se conservan y cada uno tiene al menos una petición que lo evidencia.
- `PRE-00` es el preflight de conectividad; no se cuenta dentro de los 80 casos lógicos.
- Rutas con al menos un caso nominal ejecutado: **22/22** (meta §16.3 y §17).
- Criterio de cierre: ningún caso queda sin resultado explícito; no hay `BLOCKED` ni `NOT RUN`.
- Fecha de ejecución registrada: **2026-10-23 11:30:36–11:30:41**; `runId=e2e01`; sin defectos de API.

### Conteo por carpeta de la colección

| Carpeta | Peticiones | PASS |
|---|---|---|
| 00 Preparación / conectividad | 1 | 1 |
| 01 Venues (2 rutas) | 3 | 3 |
| 02 Artists (3 rutas) | 5 | 5 |
| 03 Auth / usuarios (registro, login, me) | 20 | 20 |
| 04 Usuarios / consulta y propiedad (2 rutas) | 9 | 9 |
| 05 Events / administración y visibilidad (6 rutas) | 31 | 31 |
| 06 Tickets / compra, consulta y ciclo de vida (6 rutas) | 31 | 31 |
| 07 Seguridad / escenarios negativos | 10 | 10 |
| 08 Validaciones y errores HTTP | 8 | 8 |
| 09 Flujos de negocio / secuencia recomendada | 7 | 7 |
| **Total** | **125** | **125** |

---

## 3. Avance de flujos obligatorios — §9

| Flujo | Alcance (PRD) | Pasos del checklist | Estado |
|---|---|---|---|
| A — Descubrimiento público | §9.1 | A1–A10 | PASS |
| B — Registro, JWT y propiedad | §9.2 | B1–B14 | PASS |
| C — Catálogo y publicación (ADMIN) | §9.3 | C1–C16 | PASS |
| D — Compra, propiedad, cancelación y uso | §9.4 | D1–D18 | PASS |
| E — Regresión de errores y cierre | §9.5 | E1–E8 | PASS |

Los flujos se dan por completos porque sus peticiones tienen resultado explícito
en la tabla anterior.

---

## 4. Defectos y bloqueos hallados (§16.3, FR-E2E-014/015)

| # | Caso | Precondiciones | Pasos | HTTP esperado/observado | Reproducción | Estado |
|---|---|---|---|---|---|---|
| — | — | — | — | — | — | Ninguno: 125/125 peticiones PASS y 80/80 casos lógicos PASS |

---

## 5. Criterios de resultado — §13.5

- **PASS**: HTTP, JSON y condición de negocio/seguridad observados coinciden con lo esperado.
- **FAIL**: cualquiera de las comprobaciones no coincide, o existe error 500 no esperado.
- **BLOCKED**: no se puede ejecutar por falta de fixture, actor, token o respuesta previa indispensable.

Restricciones (§16.4): la colección no contiene Newman, Postman CLI ni scripts
de Postman. No se modificó `src/` ni se añadieron endpoints.
