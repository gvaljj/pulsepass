# Pruebas E2E manuales — PulsePass (Fase 5)

Suite de pruebas **end-to-end manuales** sobre la API HTTP de PulsePass
(PRD Fase 5, v1.1). Todo se ejecuta con la colección Postman incluida, enviando
peticiones reales a un entorno local y revisando la respuesta a mano.

## 1. Alcance y reglas (FR-E2E-001, §16)

- **HTTP blanco**: 22 rutas base (§7) más escenarios negativos de riesgo (§8).
- **Sin scripts**: la colección no tiene Pre-request ni Tests; nada de Collection
  Runner ni Newman (FR-E2E-001). Se envía con **Send** y se revisa visualmente.
- Un `2xx` **no basta**: hay que inspeccionar el JSON y, cuando aplique,
  re-consultar el estado con un GET posterior (integridad, FR-E2E-008).
- **Nada de secretos versionados** (NFR-E2E-002): sin contraseñas, hashes
  BCrypt ni tokens en ningún archivo de este directorio.
- **Sin tocar `src/`**: la Fase 5 no modifica la API ni añade endpoints.

## 2. Contenido

| Archivo | Propósito |
|---|---|
| `postman/PulsePass-Manual-E2E.postman_collection.json` | Colección v2.1: 10 carpetas (§10.1), 114 peticiones, cada una con descripción §10.2 (ID, objetivo, precondiciones, autorización, ejecución, HTTP esperado, revisión manual, evidencia) |
| `postman/PulsePass-local.postman_environment.json` | Ambiente con las variables de §11 (más las aprobadas: `venueTinyCode`, `expiredToken`, `foreignToken`, `inactiveToken`, `newEventCode`) |
| `fixtures/seed-e2e.sql` | Venues (incl. aforo 1) y eventos fixture PUB/DRAFT con sus asociaciones de artista |
| `fixtures/README.md` | Promoción de roles por SQL, cuenta inactiva, revocación TC-SEC-017 y generación offline de tokens fixture |
| `checklist-e2e-manual.md` | Flujos A–E (§9) con el orden exacto de ejecución |
| `registro-ejecucion-manual.md` | Bitácora de ejecución (§16.2) con todos los casos precargados en `NOT RUN` |
| `matriz-cobertura.md` | Matriz 22 rutas × roles/actores (entregable 7 de §25) |

## 3. Requisitos previos (§14)

- JDK 21, Maven y PostgreSQL 15+ en local.
- Postman (v10+ o superior para variables de tipo *secret*).
- Una **BD dedicada de pruebas**: `pulsepass_e2e` (nunca la BD de desarrollo).

```bash
createdb pulsepass_e2e
```

- Secreto JWT **local y exclusivo** de pruebas (≥ 32 bytes, Base64):

```bash
python -c "import secrets,base64; print(base64.b64encode(secrets.token_bytes(48)).decode())"
```

## 4. Arranque de la app E2E

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/pulsepass_e2e"
export SPRING_DATASOURCE_USERNAME="pulsepass"
export SPRING_DATASOURCE_PASSWORD="tu-password-local"
export PULSEPASS_JWT_SECRET_BASE64="<clave-base64-generada-arriba>"
mvn spring-boot:run
```

Flyway aplica V1..V5 al arrancar (TTL `PT15M` e issuer `pulsepass-api` son los
defaults de `application.properties`). Después aplica el seed (sustituye
`RUNID` por tu `runId`):

```bash
psql "$SPRING_DATASOURCE_URL" -f tests/e2e/fixtures/seed-e2e.sql
```

La app debe responder en `http://localhost:8080`; compruébalo con **PRE-00**.

## 5. Variables del ambiente (§11)

Importa `postman/PulsePass-local.postman_environment.json` en Postman y
completa lo que se marca vacío. Los valores marcados con `{{runId}}` se
resuelven automáticamente si defines `runId`; si tu versión de Postman no
anida variables dentro del ambiente, sustituye `{{runId}}` a mano en esos
valores (búsqueda y reemplazo).

| Variable | Ámbito | Origen / ejemplo |
|---|---|---|
| `baseUrl` | environment | `http://localhost:8080` |
| `runId` | environment | Identificador de corrida propio, p. ej. `r1` (prefija códigos y correos, §15.1) |
| `userAEmail`, `userAUsername` | environment | `usera-{{runId}}@example.test`, `usera-{{runId}}` |
| `userAPassword`, `userBPassword` | environment (secret) | **Local**: contraseña ≥ 12 caracteres que elijas tú; nunca la de otro entorno |
| `userBEmail`, `userBUsername` | environment | `userb-{{runId}}@example.test`, `userb-{{runId}}` |
| `adminIdentifier`, `adminPassword` | environment (secret) | `admin-{{runId}}` + contraseña local (fixture: registro + promoción SQL) |
| `staffIdentifier`, `staffPassword` | environment (secret) | `staff-{{runId}}` + contraseña local (fixture: registro + promoción SQL) |
| `userAToken`, `userBToken`, `staffToken`, `adminToken` | environment (secret) | Se rellenan **a mano** copiando `accessToken` tras cada login |
| `expiredToken`, `foreignToken`, `inactiveToken` | environment (secret) | Se generan a mano (§6 de `fixtures/README.md`); vacías por defecto |
| `venueActiveCode` | environment | `VEN-E2E-PRINCIPAL` (seed) |
| `venueTinyCode` | environment | `VEN-E2E-AFORO1` (seed, capacity 1; añadida para TC-BIZ-014C) |
| `artistId`, `artistStageName` | environment | Se rellenan con la respuesta de **E2E-ART-03** (no asumas ids: §15.1) |
| `publishedEventCode`, `draftEventCode` | environment | `EVT-E2E-{{runId}}-PUB` / `EVT-E2E-{{runId}}-DRAFT` (seed) |
| `newEventCode` | environment | `EVT-E2E-{{runId}}-CAT` (body de E2E-EVT-01) |
| `eventToPublishCode` | environment | Igual a `newEventCode`; verifica la copia con la respuesta de E2E-EVT-01 |
| `ticketCancelableCode`, `ticketUsableCode` | environment | Se rellenan con los `ticketCode` de E2E-TKT-01 y E2E-TKT-01B |
| `expectedIssuer` | environment | `pulsepass-api` |
| `expectedTokenTtlSeconds` | environment | `900` (= `PT15M`) |
| `expectedForeignResourceStatus` | environment | `403` (ver §7) |

## 6. Uso de la colección

1. Selecciona el ambiente **PulsePass-local** y comprueba `baseUrl` + `runId`.
2. Ejecuta en el orden del `checklist-e2e-manual.md` (Flujos A–E).
3. Tras cada **login** copia SOLO `accessToken` a la variable del actor
   (nunca lo guardes dentro de la colección exportada). Los tokens caducan a
   los 900 s: si un caso empieza a devolver 401 inesperados, re-login.
4. Tras cada **creación** copia el código devuelto a la variable indicada en la
   descripción de la petición.
5. Comprueba SIEMPRE qué cabecera `Authorization` lleva cada petición antes de
   enviarla: muchos negativos dependen de usar el token del actor equivocado.
6. Los 403/404/409 se dan con **payloads válidos**; solo las carpetas 08 usan
   payloads inválidos a propósito (riesgo de falso PASS, §24).

## 7. Política anti-enumeración 403/404

La Fase 4 (decisión D2 de `AuthorizationService`) define dos respuestas
distintas y la colección las respeta tal cual:

- Recurso **ajeno existente** → **403** (`expectedForeignResourceStatus=403`).
- Recurso **inexistente o borrador oculto** → **404** idéntico al de un
  inexistente (el mensaje cita el código pedido, nunca revela existencia,
  estado ni propiedad).

No son intercambiables: si un caso devuelve el código "contrario", se registra
**FAIL** con la observación, no se corrige la expectativa.

## 8. Tokens y credenciales (NFR-E2E-002)

- Contraseñas y tokens viven **solo en tu ambiente local** y en tu gestor de
  variables; los valores versionados están vacíos.
- `expiredToken`/`foreignToken`/`inactiveToken` se generan offline siguiendo
  `fixtures/README.md` §5 (PyJWT con claims `sub`, `iss`, `iat`, `exp`, `jti`).
- Si exportas la colección o el ambiente tras una corrida, usa el sufijo
  `.local.`: `tests/e2e/.gitignore` los excluye.
- Nunca generes tokens con el secreto de otro entorno, y jamás pegues un token
  real en un archivo del repositorio.

## 9. Registro de resultados

Cada caso se anota en `registro-ejecucion-manual.md` (§16.2) con: actores
usados, HTTP esperado/observado, revisión del body, resultado
`PASS`/`FAIL`/`BLOCKED`/`NOT RUN` y evidencia (códigos devueltos). Un FAIL
lleva además observación y severidad sugerida; un BLOCKED lleva su causa
(fixture faltante, app caída, token caducado…). **Sin evidencia no hay PASS**
(§16.2).

## 10. Definition of Done (§24, checklist del PRD)

- [ ] Colección Postman v2.1 válida e importable, sin scripts de Pre-request o Post-response/Tests.
- [ ] 22/22 rutas base incluidas y probadas manualmente (20 heredadas + login + me).
- [ ] Logins de A, B, STAFF y ADMIN; JWT copiados a variables locales sin hardcodear.
- [ ] Casos 400, 401, 403, 404, 409 ejecutados cuando aplique.
- [ ] Visibilidad de borradores y propiedad de usuarios/tickets verificada.
- [ ] Compra ajena bloqueada y ausencia de mutación comprobada.
- [ ] Estados de eventos/tickets comprobados tras las operaciones.
- [ ] Estructura ErrorResponse comprobada manualmente.
- [ ] Bitácora diligenciada con esperado, observado, PASS/FAIL/BLOCKED y observación.
- [ ] Variables y evidencias exportables no contienen contraseñas ni tokens.
- [ ] README describe preparación, secuencia, copia de variables y comparación de respuestas.
- [ ] Se registran los defectos y bloqueos; no se declara aprobado lo no ejecutado.
- [ ] Matriz requisito → caso → ruta → resultado disponible para revisión docente.

## 11. Problemas frecuentes

| Síntoma | Causa probable | Acción |
|---|---|---|
| 401 generalizado tras varios casos | Token caducado (900 s) | Re-login del actor y actualizar la variable |
| 404 donde esperabas 403 (o al revés) | Política D2 (§7) o recurso no existe | Verifica que el recurso existe con un actor autorizado antes de juzgar |
| 409 inesperado en E2E-EVT-01 | `newEventCode` ya existe de una corrida anterior | Nuevo `runId` o restaura la BD (`fixtures/README.md` §7) |
| 401 en TC-AUTH-013 | La cuenta temporal ya estaba desactivada | Regístrala de nuevo con `runId` nuevo (TC-AUTH-013A/013B) |
| TC-SEC-017 devuelve 200 | Olvidaste revocar el rol STAFF | Ejecuta el SQL de revocación de `fixtures/README.md` §4 y reintenta |
| STAFF/ADMIN dan 403 en todo | Rol no promovido o revocado sin restaurar | Re-ejecuta la promoción SQL / restaura el rol |
| `{{variables}}` literales en las respuestas | `runId` sin definir o ambiente equivocado | Define `runId` y selecciona PulsePass-local |
