# PulsePass — Plataforma de Eventos, Artistas y Entradas

Caso de estudio académico enfocado en la capa de persistencia, integridad referencial, migraciones de base de datos y pruebas de integración.

## Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 4.x**
- **Spring Web / MVC (API REST)**
- **Bean Validation**
- **Spring Data JPA / Hibernate**
- **Spring Security** (cadenas de seguridad stateless, deny-by-default)
- **JJWT 0.13.x** (JSON Web Tokens HS256)
- **BCrypt** (hash de contraseñas)
- **PostgreSQL**
- **Flyway** (Gestión de migraciones)
- **Testcontainers** (Pruebas de integración sobre PostgreSQL real)

---

## Esquema Relacional y Migraciones

La base de datos se versiona a través de Flyway en `src/main/resources/db/migration/`:

1. `V1__create_schema.sql`: Creación de tablas (`venues`, `events`, `artists`, `event_artists`, `users`, `user_profiles`, `tickets`) con restricciones de clave primaria, clave foránea, `UNIQUE` y `CHECK`.
2. `V2__insert_initial_artists.sql`: Inserción del catálogo inicial de artistas.
3. `V3__add_streaming_url_to_event.sql`: Evolución del esquema para soportar URLs de streaming en eventos.
4. `V4__add_fk_indexes.sql`: Índices sobre columnas foráneas consultadas frecuentemente (`events.venue_id`, `user_profiles.user_id`, `tickets.user_id`, `tickets.event_id`).
5. `V5__security_credentials_roles.sql`: Migración incremental de la capa de seguridad: tablas `user_credentials` (hash BCrypt por usuario, sin contraseñas en claro) y `user_roles` (asignación usuario↔rol con clave compuesta), más el procedimiento `assign_user_credential` para activar usuarios preexistentes sin passwords por defecto ni compartidos. No modifica V1-V4.

Hibernate opera en modo `validate` (`spring.jpa.hibernate.ddl-auto=validate`): el esquema lo crea y evoluciona exclusivamente Flyway.

---

## Estrategia de Consultas (Repository)

Las consultas simples usan **Query Methods** de Spring Data; las complejas usan **JPQL** (`@Query`) con JOIN, COUNT o DISTINCT. No se utiliza SQL nativo.

| Repository | Método | Mecanismo | Requisito |
|---|---|---|---|
| VenueRepository | `findByCode` | Query Method | FR-VEN-001 |
| EventRepository | `findByEventCode` | Query Method | FR-EVT-002 |
| EventRepository | `findByStatusOrderByEventDateAsc` | Query Method | FR-EVT-005 |
| EventRepository | `findByVenueCode` | Query Method | FR-VEN-004 |
| EventRepository | `findByArtistStageName` | JPQL JOIN + DISTINCT | FR-SRC-001 |
| EventRepository | `findByCityAndArtistStageName` | JPQL multi-asociación | FR-SRC-002 |
| EventRepository | `findRecommendedEvents` | JPQL JOIN + LOWER + DISTINCT (solo PUBLISHED) | FR-SRC-003 |
| UserRepository | `findByUsername` / `findByEmailIgnoreCase` | Query Method | FR-USR-002 |
| TicketRepository | `findByTicketCode` | Query Method (devuelve `Optional`) | FR-TKT-002 |
| TicketRepository | `findByUserEmail` / `findByUserEmailAndStatus` | Query Method | FR-TKT-006 |
| TicketRepository | `findByEventEventCodeAndStatus` | Query Method | FR-TKT-007 |
| TicketRepository | `countPaidTicketsByEventCode` | JPQL COUNT | FR-TKT-008 |
| TicketRepository | `findTicketsForFutureEvents` | JPQL + ORDER BY | FR-SRC-004 |
| ArtistRepository | `findByStageName` | Query Method | FR-ART-002 |

---

## API REST (Capa de Controladores)

La capa de controladores expone los 20 métodos de los Services como una API REST. Los controllers son delgados: delegan en los Services, no contienen lógica de negocio ni acceden a Repositories, y todos responden un `ResponseEntity` con DTOs de respuesta (nunca entidades JPA).

| Método | Ruta | Descripción | Éxito | Requisito |
|---|---|---|---|---|
| POST | `/api/auth/login` | Login por username o email (público) | 200 / 400 / 401 | FR-CTRL-AUTH-001 |
| GET | `/api/auth/me` | Identidad vigente del token | 200 / 401 | FR-CTRL-AUTH-002 |
| GET | `/api/venues/{code}` | Venue por código | 200 / 404 | FR-CTRL-VEN-001 |
| GET | `/api/venues/active` | Venues activos | 200 | FR-CTRL-VEN-002 |
| POST | `/api/events` | Crear evento | 201 / 400 | FR-CTRL-EVT-001 |
| GET | `/api/events/{eventCode}` | Evento por código | 200 / 404 | FR-CTRL-EVT-002 |
| GET | `/api/events/published` | Eventos publicados | 200 | FR-CTRL-EVT-003 |
| PATCH | `/api/events/{eventCode}/publish` | Publicar evento | 200 / 409 | FR-CTRL-EVT-004 |
| POST | `/api/events/{eventCode}/artists/{artistId}` | Asociar artista a evento | 200 / 409 | FR-CTRL-EVT-005 |
| GET | `/api/events/by-artist?stageName=` | Eventos por nombre artístico | 200 | FR-CTRL-EVT-006 |
| GET | `/api/artists/{id}` | Artista por ID | 200 / 404 | FR-CTRL-ART-001 |
| GET | `/api/artists/by-stage-name?stageName=` | Artista por nombre artístico | 200 / 404 | FR-CTRL-ART-002 |
| GET | `/api/artists/active` | Artistas activos | 200 | FR-CTRL-ART-003 |
| POST | `/api/users` | Registrar usuario | 201 / 400 / 409 | FR-CTRL-USR-001 |
| GET | `/api/users/by-email?email=` | Usuario por email | 200 / 404 | FR-CTRL-USR-002 |
| GET | `/api/users/by-username?username=` | Usuario por username | 200 / 404 | FR-CTRL-USR-003 |
| POST | `/api/tickets` | Comprar ticket | 201 / 400 / 404 / 409 | FR-CTRL-TKT-001 |
| GET | `/api/tickets/{ticketCode}` | Ticket por código | 200 / 404 | FR-CTRL-TKT-002 |
| GET | `/api/tickets/by-user?email=` | Tickets por email de usuario | 200 | FR-CTRL-TKT-003 |
| GET | `/api/events/{eventCode}/tickets/paid` | Tickets PAID de un evento | 200 | FR-CTRL-TKT-004 |
| PATCH | `/api/tickets/{ticketCode}/cancel` | Cancelar ticket | 200 / 409 | FR-CTRL-TKT-005 |
| PATCH | `/api/tickets/{ticketCode}/use` | Marcar ticket como usado | 200 / 409 | FR-CTRL-TKT-006 |

### Contrato de errores

Todos los errores devuelven un `ErrorResponse` JSON uniforme (`timestamp`, `status`, `error`, `message`, `details`), producido por el `GlobalExceptionHandler` centralizado:

```json
{
  "timestamp": "2026-10-11T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": { "eventCode": "El código del evento es obligatorio" }
}
```

| Condición | Código | Origen |
|---|---|---|
| Token ausente/inválido o login fallido | 401 | `AuthenticationEntryPoint` / `AuthService` |
| Autenticado sin permisos sobre el recurso | 403 | `AccessDeniedHandler` / `AuthorizationService` |
| Request inválido (Bean Validation) | 400 | `MethodArgumentNotValidException` |
| JSON mal formado | 400 | `HttpMessageNotReadableException` |
| Recurso inexistente | 404 | `ResourceNotFoundException` |
| Recurso duplicado | 409 | `DuplicateResourceException` |
| Regla de negocio incumplida | 409 | `BusinessRuleException` |
| Error inesperado (sin stack traces) | 500 | `Exception` (NFR-CTRL-007) |

---

## Capa de Seguridad (Spring Security + JJWT)

La Fase 4 añade autenticación stateless con JWT y autorización deny-by-default sobre toda la API.

### Autenticación (JWT HS256)

- **Registro** (`POST /api/users`, público): `AuthService` crea usuario + perfil + hash BCrypt de la contraseña + rol `USER` en **una sola transacción** (`@Transactional`). La contraseña nunca se persiste en claro: solo su hash en `user_credentials` (migración V5). BCrypt limita la contraseña a un mínimo de 12 caracteres y un máximo de 72 bytes.
- **Login** (`POST /api/auth/login`, público): acepta username o email en `identifier`. Cualquier fallo (usuario inexistente, contraseña incorrecta o cuenta desactivada) produce **el mismo 401 genérico** (`"Credenciales inválidas"`), sin revelar existencia de usuarios ni estado de la cuenta.
- **Token**: JWT firmado con **HS256** (JJWT 0.13.x) con los claims `sub` (id persistente del usuario), `iss` (`pulsepass-api`), `iat`, `exp` y `jti`. TTL por defecto **900 s** (`pulsepass.security.jwt.access-token-ttl`, ISO-8601).
- **Filtro** (`JwtAuthenticationFilter`): lee únicamente el header `Authorization: Bearer <token>`; valida firma, expiración y issuer; **reconstruye la identidad desde la base de datos** en cada petición (roles y estado vigentes: un token de un usuario desactivado deja de servir). Un token inválido/expirado/firmado mal se descarta y la cadena continúa: en rutas protegidas responde el `AuthenticationEntryPoint` con 401 uniforme. Nunca sobrescribe una autenticación previa; una doble cabecera `Authorization` se trata como solicitud ambigua y no autentica.
- **Identidad**: `/api/auth/me` devuelve el `SecurityPrincipal` vigente del token (id, username, email, roles `ROLE_*`, estado activo).

### Configuración obligatoria (falla de arranque si falta)

La aplicación **no arranca sin secreto JWT válido**: si `PULSEPASS_JWT_SECRET_BASE64` falta o decodifica a menos de 32 bytes, `JwtProperties` lanza `IllegalStateException` durante el arranque (deny-by-default).

| Variable de entorno | Propiedad | Default | Descripción |
|---|---|---|---|
| `PULSEPASS_JWT_SECRET_BASE64` | `pulsepass.security.jwt.secret-base64` | *(sin default)* | **Obligatoria.** Secreto Base64 de ≥32 bytes para firmar/verificar HS256. |
| — | `pulsepass.security.jwt.issuer` | `pulsepass-api` | Issuer (`iss`) exigido en la validación. |
| — | `pulsepass.security.jwt.access-token-ttl` | `PT15M` (900 s) | TTL ISO-8601 del access token. |
| `PULSEPASS_CORS_ALLOWED_ORIGINS` | `pulsepass.security.cors.allowed-origins` | *(vacío)* | Orígenes CORS permitidos, separados por comas. Vacío = ningún origen cross-origin (deny-by-default). |

Ver `.env.example` en la raíz del proyecto.

### Matriz de autorización (deny-by-default)

La cadena es `STATELESS` (sin sesión ni CSRF) y cualquier ruta no listada **exige autenticación**. La propiedad (¿es suyo el recurso?) se comprueba en la capa de Services con el id persistente del token, nunca con el email del cliente:

| Rutas | Requisito |
|---|---|
| `POST /api/auth/login`, `POST /api/users` | Público |
| `GET /api/venues/**`, `GET /api/artists/**`, `GET /api/events/published`, `GET /api/events/by-artist`, `GET /api/events/{code}` | Público (los borradores se ocultan con respuesta 404 idéntica a la de un inexistente) |
| `GET /api/auth/me` | Autenticado |
| `POST /api/events`, `PATCH /api/events/*/publish`, `POST /api/events/*/artists/*` | `ROLE_ADMIN` |
| `GET /api/events/*/tickets/paid`, `PATCH /api/tickets/*/use` | `ROLE_STAFF` o `ROLE_ADMIN` |
| `POST /api/tickets` | `ROLE_USER` + el `userEmail` debe ser el propio (anti-IDOR) |
| `GET /api/tickets/{code}`, `GET /api/tickets/by-user`, `PATCH /api/tickets/*/cancel` | Autenticado + propiedad (propietario o `ROLE_ADMIN`; `STAFF` queda excluido de cancelar) |
| `GET /api/users/by-email`, `GET /api/users/by-username` | Autenticado + propiedad (o `ROLE_ADMIN`) |
| Cualquier otra ruta | Autenticado |

**Anti-IDOR**: la propiedad se resuelve con el id del principal (`AuthorizationService`), de modo que el email o el código enviado por el cliente nunca otorgan acceso ajeno. Violación de propiedad sobre un recurso existente → **403**; inexistencia → **404** (sin oráculo de existencia).

### Observabilidad, transporte y CORS (NFR-SEC-006/007/008)

- **Request ID (NFR-SEC-006)**: `RequestIdFilter` genera un identificador por solicitud, lo devuelve en la cabecera `X-Request-Id` y lo correlaciona vía MDC con los logs de cada línea. El login correcto/fallido y cada denegación (`RestAuthenticationEntryPoint` 401, `RestAccessDeniedHandler` 403) se registran con su request id, método, ruta y causa — nunca contraseñas, hashes ni tokens.
- **Transporte (NFR-SEC-007)**: **HTTPS obligatorio fuera de localhost** y de ambientes de laboratorio aislados; solo en desarrollo local se admite HTTP.
- **CORS (NFR-SEC-008)**: política parametrizada por `PULSEPASS_CORS_ALLOWED_ORIGINS` (ver tabla de variables). Por defecto la lista está vacía: ningún origen cross-origin queda permitido.

### Roles de prueba (DoD §22)

No existe ningún endpoint público que conceda roles superiores: el registro público asigna `USER` exclusivamente (NFR-SEC-009) y `RegisterUserRequest` no acepta roles.

- **En la suite de pruebas**: la semilla `@BeforeEach` de `SecurityAuthorizationMatrixTest` crea por repositorio a `andrea` y `luis` (`USER`), `pepe` (`STAFF`), `miguel` (`ADMIN`) y `juan` (`USER` desactivado); los tokens se emiten con el `JwtService` real sobre esa identidad persistente.
- **En una demo local**: asignar el rol con SQL directo sobre la migración V5:

```sql
INSERT INTO user_roles (user_id, role) VALUES (1, 'ADMIN') ON CONFLICT DO NOTHING;
```

- **Activación de usuarios preexistentes (§9.2)**: la función `assign_user_credential(user_id, password_hash, role)` de V5 enlaza el hash BCrypt de un usuario legado sin password por defecto ni compartido; hasta ejecutarla ese usuario no puede iniciar sesión (deny-by-default).

---

## Ejecución de Pruebas

Para ejecutar la suite completa (pruebas unitarias de servicios y seguridad, pruebas de contrato HTTP de los controllers con `@WebMvcTest`/`MockMvc` con **la cadena de seguridad real activa** —nunca `addFilters=false`— y pruebas de integración sobre PostgreSQL real con Testcontainers): `./mvnw clean test`

La suite incluye la **matriz de autorización T-SEC-001..020** (`SecurityAuthorizationMatrixTest`), con los ids exactos de la tabla §17.3 del PRD de seguridad y pruebas complementarias de cobertura §17.4, usando JWTs reales emitidos por el propio `JwtService` y la identidad resuelta desde la base de datos, más `SecurityPersistenceIntegrationTest` (Flyway V5, hash BCrypt, registro atómico y procedimiento de activación de usuarios preexistentes). El secreto JWT de pruebas es exclusivo del entorno de test y se inyecta por `@TestPropertySource`; la suite entera es reproducible sin ningún secreto real.

---

## Pruebas E2E (Fase 5)

La Fase 5 (PRD `5_PRD_PulsePass_Pruebas_E2E_Postman_Manual`) valida la API completa con **pruebas end-to-end desde Postman**: peticiones individuales, inspección de Status/Headers/Body y resultados anotados en una bitácora. La colección **no contiene ningún script** (0 claves `event`, sin Pre-request ni Tests), no se usa Collection Runner, Newman ni Postman CLI, y ningún secreto se versiona (NFR-E2E-002). No se modificó `src/`: esta fase es de verificación, no de implementación (§2.4).

> **Estado de la corrida (2026-10-23): 125/125 peticiones PASS; 80/80 casos
> lógicos PASS**, 0 FAIL, 0 BLOCKED, 0 NOT RUN. La colección permaneció sin
> scripts y no se usó Runner/Newman/CLI. Ver
> `tests/e2e/registro-ejecucion-manual.md` (125 filas) y
> `tests/e2e/matriz-cobertura.md`.

### Entregables (`tests/e2e/`)

| Archivo | Contenido |
|---|---|
| `README.md` | Preparación del ambiente (BD `pulsepass_e2e`, seed, JWT), tablas de las 32 variables, copia de tokens/códigos, política 403/404 y problemas frecuentes. |
| `postman/PulsePass-Manual-E2E.postman_collection.json` | Colección v2.1: 10 carpetas del §10.1, 125 peticiones, cada descripción con la convención §10.2 (ID, objetivo, precondiciones, autorización, ejecución, HTTP esperado, revisión de respuesta, evidencia) y **un ejemplo de respuesta guardado y sanitizado** (JWT como `<JWT omitido>`). |
| `postman/PulsePass-local.postman_environment.json` | Plantilla de variables (§11): datos, códigos, tokens como variables locales vacías y secretos de runtime nunca exportados. |
| `fixtures/seed-e2e.sql` + `fixtures/README.md` | Fixtures repetibles (venues, eventos PUB/DRAFT, cuentas STAFF/ADMIN/inactiva) y procedimientos fuera de HTTP: promoción de roles por SQL, desactivación y generación offline de `expiredToken`/`foreignToken`. |
| `checklist-e2e-manual.md` | Orden de ejecución de los flujos obligatorios A–E (§9) con esperado por paso. |
| `registro-ejecucion-manual.md` | Bitácora §16.2 con **125 filas** (una por petición) y los **80 casos lógicos** (22 de rutas §7 + 16 TC-AUTH + 22 TC-SEC + 20 TC-BIZ); corrida 2026-10-23: **125/125 peticiones PASS; 80/80 casos lógicos PASS**, 0 FAIL, 0 BLOCKED, 0 NOT RUN. |
| `matriz-cobertura.md` | Matriz 22 rutas × actores y trazabilidad requisito → caso → ruta → resultado (DoD §24.13, §25.7); solo marca combinaciones ejecutadas (`—` = no ejecutado) y justifica 403 vs 404. |

### Ejecución resumida

1. Levantar PostgreSQL dedicado `pulsepass_e2e` y la app con `SPRING_DATASOURCE_*` y `PULSEPASS_JWT_SECRET_BASE64` propios del E2E; aplicar Flyway y `seed-e2e.sql` (§2–§3 de `tests/e2e/README.md`).
2. Importar colección y ambiente en Postman, elegir `runId` y contraseñas locales; registrar usuarios A/B y hacer login de A, B, STAFF y ADMIN copiando cada `accessToken` a su variable.
3. Recorrer `checklist-e2e-manual.md` en orden (Flujos A–E) y anotar cada caso en `registro-ejecucion-manual.md`; al cerrar, consolidar 22/22 rutas y actualizar `matriz-cobertura.md`.

**Corrida registrada (2026-10-23):** resultado **125/125 peticiones PASS;
80/80 casos lógicos PASS**, 0 FAIL, 0 BLOCKED, 0 NOT RUN; 22/22 rutas y 125
peticiones cubiertas. La bitácora y la matriz reflejan esa corrida.

La definición de terminación es el checklist de 13 puntos del §24 de ese PRD (documentado en `tests/e2e/README.md`): ningún caso sin resultado explícito y ningún `NOT RUN`/`BLOCKED` contado como aprobado.

--> RESPUESTAS A PREGUNTAS TEÓRICAS

1. ¿Por qué Ticket debe ser una entidad en lugar de una relación @ManyToMany simple entre User y Event?
## En el modelo de dominio, un ticket no es simplemente una asociación binaria entre un usuario y un evento. Posee atributos propios del negocio que le dan identidad independiente, tales como ticketCode (identificador único), type (VIP, General, etc.), price (monto monetario de compra), status (RESERVED, PAID, CANCELLED, USED) y purchaseDate. Al contener estado y ciclo de vida propio, requiere modelarse como una entidad explícita relacionada mediante dos asociaciones @ManyToOne (hacia User y hacia Event).

2. ¿Qué reglas pertenecen a PostgreSQL y cuáles deberían quedar para una futura capa Service?
## En PostgreSQL: Pertenecen las restricciones de integridad estructural e invariable de datos, tales como la unicidad (UNIQUE para códigos, emails y relación 1:1), no nulidad (NOT NULL), claves foráneas (FOREIGN KEY) y reglas de rango básico (CHECK capacity > 0 y CHECK price >= 0).
## En la capa Service: Pertenecen las validaciones de flujo de negocio y lógica transaccional compleja, tales como verificar si hay aforo disponible antes de emitir un ticket, validar si la fecha de compra es coherente, ejecutar pasarelas de pago externas o coordinar cambios de estado entre múltiples entidades.

3. ¿Qué consultas pueden expresarse claramente como Query Methods y cuáles justifican JPQL?
## Query Methods: Son ideales para operaciones sencillas basadas en propiedades directas o navegaciones de relaciones simples sin ambigüedad (ej. findByCode, findByStatusOrderByEventDateAsc o findByUserEmailAndStatus).
## JPQL: Se justifica cuando la consulta requiere operaciones avanzadas de agregación (COUNT), funciones escalar/de texto (LOWER, CONCAT), relaciones N:M con múltiples JOIN, filtros dinámicos o la palabra clave DISTINCT para evitar duplicados en la colección de resultados (ej. findRecommendedEvents o countPaidTicketsByEventCode).

4. ¿Qué consecuencias tendría modificar V1__create_schema.sql después de haberla aplicado en un ambiente compartido?
## Alterar un archivo de migración ya ejecutado corrompe el hash de suma de comprobación (checksum) que Flyway almacena en la tabla flyway_schema_history. Al intentar desplegar nuevamente la aplicación, Flyway detectará una discrepancia de integridad y detendrá el arranque con un error de migración. En entornos compartidos o productivos, cualquier cambio estructural debe agregarse mediante un nuevo archivo de migración incremental (ej. V4__...sql).

5. ¿Qué diferencias podría ocultar una prueba con H2 frente a PostgreSQL?
## H2 es una base de datos en memoria que difiere en la sintaxis SQL nativa, el manejo de concurrencia, los tipos de datos numéricos/monetarios y la evaluación de ciertas restricciones. Probar con H2 puede ocultar errores como el comportamiento exacto de transacciones, diferencias en funciones de texto case-insensitive, soporte de enums o sintaxis de expresiones regulares y CHECK constraints específicos de PostgreSQL. Testcontainers garantiza realismo al probar contra la misma versión del motor que se usa en producción.

6. ¿Cómo evolucionaría el modelo para soportar inventario de tickets y evitar sobreventa?
## El modelo evolucionaría introduciendo una entidad Section / TicketInventory asociada a cada evento con la capacidad disponible por zona. Para evitar la sobreventa en escenarios concurrentes, se implementaría Optimistic Locking (con un campo @Version en la entidad del inventario) o Pessimistic Locking (PESSIMISTIC_WRITE mediante SELECT FOR UPDATE en PostgreSQL) durante el proceso de reserva/compra en la capa de servicios.
