package com.pulsepass.repository;

import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // FR-TKT-002: Buscar ticket por su código de negocio (único en BD, por eso Optional)
    Optional<Ticket> findByTicketCode(String ticketCode);

    // FR-TKT-006: Consultar tickets de un usuario por email (sin filtro de estado)
    List<Ticket> findByUserEmail(String email);

    // FR-TKT-006: Consultar tickets de un usuario por email y estado opcional
    List<Ticket> findByUserEmailAndStatus(String email, TicketStatus status);

    // PRD SVC: tickets de un usuario por email ignorando mayúsculas y ordenados por compra descendente
    List<Ticket> findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(String email);

    // PRD SVC: tickets de un evento por código y estado
    List<Ticket> findByEventEventCodeAndStatus(String eventCode, TicketStatus status);

    // PRD SVC §32: conteo de tickets por evento y estado (JPQL)
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.eventCode = :eventCode AND t.status = :status")
    long countByEventEventCodeAndStatus(@Param("eventCode") String eventCode, @Param("status") TicketStatus status);

    // FR-SRC-004: tickets cuyo evento es posterior a la fecha indicada, en orden cronológico
    @Query("""
            SELECT t
            FROM Ticket t
            JOIN t.event e
            WHERE e.eventDate > :date
            ORDER BY e.eventDate ASC, t.ticketCode ASC
            """)
    List<Ticket> findTicketsForFutureEvents(@Param("date") LocalDateTime date);
}
