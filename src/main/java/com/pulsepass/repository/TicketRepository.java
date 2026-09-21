package com.pulsepass.repository;

import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // FR-TKT-002: Buscar ticket por su código de negocio
    List<Ticket> findByTicketCode(String ticketCode);

    // FR-TKT-006: Consultar tickets de un usuario por email y estado
    List<Ticket> findByUserEmailAndStatus(String email, TicketStatus status);

    // FR-TKT-007: Recuperar tickets pagados (PAID) de un evento mediante eventCode
    List<Ticket> findByEventEventCodeAndStatus(String eventCode, TicketStatus status);

    // FR-TKT-008: Conteo de tickets pagados (PAID) de un evento usando JPQL
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.eventCode = :eventCode AND t.status = :status")
    long countPaidTicketsByEventCode(@Param("eventCode") String eventCode, @Param("status") TicketStatus status);

    // FR-SRC-004: Consultar tickets cuyo evento sea posterior a una fecha
    @Query("SELECT t FROM Ticket t WHERE t.event.eventDate > :date ORDER BY t.event.eventDate ASC")
    List<Ticket> findTicketsForFutureEvents(@Param("date") LocalDateTime date);
}