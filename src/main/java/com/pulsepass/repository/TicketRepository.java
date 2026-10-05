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

    // Método que retorna Optional directamente como lo pide el Service
    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByUserEmailAndStatus(String email, TicketStatus status);
    
    List<Ticket> findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(String email);

    List<Ticket> findByEventEventCodeAndStatus(String eventCode, TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.eventCode = :eventCode AND t.status = :status")
    long countPaidTicketsByEventCode(@Param("eventCode") String eventCode, @Param("status") TicketStatus status);
    
    default long countByEventEventCodeAndStatus(String eventCode, TicketStatus status) {
        return countPaidTicketsByEventCode(eventCode, status);
    }
}