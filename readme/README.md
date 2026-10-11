# PulsePass — Plataforma de Eventos, Artistas y Entradas

Caso de estudio académico enfocado en la capa de persistencia, integridad referencial, migraciones de base de datos y pruebas de integración.

## Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 4.x**
- **Spring Data JPA / Hibernate**
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

## Ejecución de Pruebas

Para ejecutar la suite de pruebas de integración que valida las reglas de negocio e integridad de PostgreSQL utilizando Testcontainers: ./mvnw clean test

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
