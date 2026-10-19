# Fixtures E2E — PulsePass (Fase 5)

Este directorio preprovisiona los fixtures que **no se pueden crear por HTTP**
(PRD §5.1, §15.1) y documenta cómo levantar los actores STAFF/ADMIN y las
cuentas especiales **sin dejar ni un hash, contraseña ni token en el repo**
(NFR-E2E-002).

---

## 1. `seed-e2e.sql` — venues y eventos fixture

Aplicar **después** de las migraciones Flyway (arranque normal de la app) y
**solo** contra la BD dedicada `pulsepass_e2e`:

```bash
# 1) Arranca la app E2E al menos una vez para que Flyway cree el esquema (V1..V5)
# 2) Aplica el seed (sustituye RUNID por tu runId en el archivo):
psql "$E2E_DB_URL" -f tests/e2e/fixtures/seed-e2e.sql
```

| Fixture | Código (tras sustituir RUNID) | Uso |
|---|---|---|
| Venue principal (capacity 500) | `VEN-E2E-PRINCIPAL` → `venueActiveCode` | Flujos A–E, compras |
| Venue de aforo 1 | `VEN-E2E-AFORO1` → `venueTinyCode` | TC-BIZ-014C (aforo agotado) |
| Evento publicado | `EVT-E2E-RUNID-PUB` → `publishedEventCode` | Descubrimiento público y compras |
| Evento borrador | `EVT-E2E-RUNID-DRAFT` → `draftEventCode` | TC-SEC-004 / TC-SEC-015 |
| Asociación `Solar Beat` ↔ ambos eventos | — | E2E-EVT-06 y TC-SEC-005 (contraste anónimo vs operador) |

El seed es **idempotente** (`ON CONFLICT DO NOTHING`): puede re-ejecutarse. Los
artistas vienen de la migración V2 (no se insertan aquí); el id se resuelve por
`stage_name`, sin asumir ids autoincrementales (§15.1).

> Los eventos `EVT-E2E-<runId>-EDAD` y `EVT-E2E-<runId>-AFORO` de TC-BIZ-014B/C
> **no** vienen del seed: se crean con ADMIN usando E2E-EVT-01 (copiando el body
> y cambiando `eventCode`/`minimumAge`/`venueCode`) y se publican con
> E2E-EVT-04. Está descrito en la propia petición de la colección.

---

## 2. Actores privilegiados (registro HTTP + promoción por SQL)

STAFF/ADMIN **nunca** se registran con rol (TC-SEC-016 demostraría que el campo
`role` del registro se ignora). El flujo correcto es:

1. Registrar el usuario como cualquiera (`POST /api/users`, carpeta 03 y
   copias de esas peticiones con el nombre de usuario del actor).
2. Promover el rol **directamente en la BD E2E** (patrón del README principal,
   §2.4). No uses `assign_user_credential`: el usuario ya tiene credencial
   creada por la API y la función la rechazaría.

```sql
-- STAFF (identificador del ambiente: staff-<runId>)
INSERT INTO user_roles (user_id, role)
SELECT id, 'STAFF' FROM users WHERE username = 'staff-RUNID'
ON CONFLICT (user_id, role) DO NOTHING;

-- ADMIN (identificador del ambiente: admin-<runId>)
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin-RUNID'
ON CONFLICT (user_id, role) DO NOTHING;
```

Las contraseñas de estos actores son **locales**: elige una en el ambiente
(`staffPassword`, `adminPassword`, ≥ 12 caracteres) al registrarlos. No se
versionan ni se documentan en claro.

## 3. Cuenta desactivada (TC-AUTH-013)

```sql
-- a) Ejecuta TC-AUTH-013A (registro) y TC-AUTH-013B (login -> inactiveToken)
-- b) Desactiva la cuenta:
UPDATE users SET active = FALSE WHERE username = 'inactivo-RUNID';
-- c) Ejecuta TC-AUTH-013 (carpeta 07): el token capturado ahora responde 401
```

## 4. Revocación de rol STAFF (TC-SEC-017)

```sql
-- a) Antes: E2E-TKT-04 con staffToken -> 200
DELETE FROM user_roles
WHERE user_id = (SELECT id FROM users WHERE username = 'staff-RUNID')
  AND role = 'STAFF';
-- b) Postman: TC-SEC-017 -> 403 (el JWT firmado pierde privilegio: roles = BD)
-- c) RESTAURA siempre el rol (o los casos STAFF siguientes quedarán BLOCKED):
INSERT INTO user_roles (user_id, role)
SELECT id, 'STAFF' FROM users WHERE username = 'staff-RUNID'
ON CONFLICT (user_id, role) DO NOTHING;
```

---

## 5. Tokens fixture generados offline (carpeta 07)

`expiredToken`, `foreignToken` e `inactiveToken` son **variables locales
vacías** en el ambiente versionado; se generan a mano antes de ejecutar cada
caso y se pegan solo en tu Postman local. La app valida: **HS256**, `iss`
= `pulsepass-api`, `sub` (id numérico), `iat`, `exp` y `jti` obligatorios
(`JwtService.validate`).

```python
# pip install PyJWT
import base64, os, time, uuid, jwt

# Clave del entorno E2E local. NUNCA uses la clave de otro entorno y jamás
# pegues estos valores en un archivo versionado.
key = base64.b64decode(os.environ["PULSEPASS_JWT_SECRET_BASE64"])  # >= 32 bytes
now = int(time.time())

# --- expiredToken (TC-AUTH-010): firma CORRECTA, exp ya vencido -------------
print("expiredToken =", jwt.encode({
    "sub": "1",                # id numérico del usuario (no el username)
    "iss": "pulsepass-api",
    "iat": now - 7200,
    "exp": now - 6300,         # vencido: el 401 se debe SOLO a la expiración
    "jti": str(uuid.uuid4()),
}, key, algorithm="HS256"))

# --- foreignToken (TC-AUTH-011): OTRA clave y OTRO issuer -------------------
otra_clave = base64.b64encode(b"clave-de-otro-emitente-suficientemente-larga-123").decode()
print("foreignToken =", jwt.encode({
    "sub": "1",
    "iss": "otro-issuer",
    "iat": now,
    "exp": now + 3600,
    "jti": str(uuid.uuid4()),
}, otra_clave, algorithm="HS256"))
```

- **`inactiveToken`** no se genera con PyJWT: se obtiene con
  **TC-AUTH-013B** (login de la cuenta temporal) **antes** de desactivarla (§3).
- Si una de estas variables queda vacía, el caso correspondiente se marca
  **BLOCKED** en la bitácora, nunca PASS (§16.2).

---

## 6. Reglas de no-versionación (NFR-E2E-002)

- Este directorio no contiene contraseñas, hashes BCrypt, tokens ni secretos.
- El secreto JWT (`PULSEPASS_JWT_SECRET_BASE64`) vive solo en el entorno local
  (≥ 32 bytes, p. ej. `openssl rand -base64 48`); las instrucciones de arranque
  están en `readme/README.md`.
- Si exportas la colección o el ambiente desde Postman tras una corrida, guárdalos
  con sufijo `.local.`: `tests/e2e/.gitignore` los excluye.

## 7. Repetibilidad (§15.2)

Antes de cada corrida nueva: **restaura la BD** (dump previo o recrear
`pulsepass_e2e` y dejar que Flyway + seed la repongan) **o** usa un `runId`
nuevo en todas partes (ambiente + `RUNID` del seed). Nunca mezcles usuarios de
corridas anteriores con un `runId` nuevo.
