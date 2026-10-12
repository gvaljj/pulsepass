package com.pulsepass.controller;

import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.TicketService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    private TicketResponse sampleTicket(TicketStatus status) {
        return new TicketResponse(1L, "TCK-1", TicketType.GENERAL, new BigDecimal("50.00"),
                status, LocalDateTime.now(), "andrea@email.com", "CMF-2026", "Caribbean Music Fest 2026");
    }

    private String validBody() {
        return """
                {"userEmail":"andrea@email.com","eventCode":"CMF-2026","type":"GENERAL"}
                """;
    }

    // TEST-CTRL-TKT-001: compra válida -> 201 Created
    @Test
    void purchase_Valid_Returns201() throws Exception {
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenReturn(sampleTicket(TicketStatus.PAID));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(ticketService).purchase(any(PurchaseTicketRequest.class));
    }

    // TEST-CTRL-TKT-002: request inválido -> 400 sin invocar el Service
    @Test
    void purchase_InvalidRequest_Returns400_AndDoesNotCallService() throws Exception {
        String invalid = """
                {"userEmail":"no-es-email","eventCode":"CMF-2026"}
                """;

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.userEmail").exists())
                .andExpect(jsonPath("$.details.type").exists());

        verify(ticketService, never()).purchase(any(PurchaseTicketRequest.class));
    }

    // TEST-CTRL-TKT-003: usuario inexistente -> 404
    @Test
    void purchase_MissingUser_Returns404() throws Exception {
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(new ResourceNotFoundException("Usuario no encontrado con email: andrea@email.com"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(ticketService).purchase(any(PurchaseTicketRequest.class));
    }

    // TEST-CTRL-TKT-004: regla de negocio (ej. edad mínima) -> 409
    @Test
    void purchase_BusinessRule_Returns409() throws Exception {
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(new BusinessRuleException("El usuario no cumple la edad mínima requerida (18)"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));

        verify(ticketService).purchase(any(PurchaseTicketRequest.class));
    }

    // TEST-CTRL-TKT-005: consultar ticket -> 200
    @Test
    void findByCode_Returns200() throws Exception {
        when(ticketService.findByCode("TCK-1")).thenReturn(sampleTicket(TicketStatus.PAID));

        mockMvc.perform(get("/api/tickets/TCK-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TCK-1"));

        verify(ticketService).findByCode("TCK-1");
    }

    // TEST-CTRL-TKT-006: tickets por usuario -> 200
    @Test
    void findByUserEmail_Returns200() throws Exception {
        when(ticketService.findByUserEmail("andrea@email.com"))
                .thenReturn(List.of(sampleTicket(TicketStatus.PAID)));

        mockMvc.perform(get("/api/tickets/by-user").param("email", "andrea@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketCode").value("TCK-1"));

        verify(ticketService).findByUserEmail("andrea@email.com");
    }

    // TEST-CTRL-TKT-007: tickets PAID por evento -> 200
    @Test
    void findPaidTicketsByEvent_Returns200() throws Exception {
        when(ticketService.findPaidTicketsByEvent("CMF-2026"))
                .thenReturn(List.of(sampleTicket(TicketStatus.PAID)));

        mockMvc.perform(get("/api/events/CMF-2026/tickets/paid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketCode").value("TCK-1"));

        verify(ticketService).findPaidTicketsByEvent("CMF-2026");
    }

    // TEST-CTRL-TKT-008: cancelar ticket válido -> 200
    @Test
    void cancel_Valid_Returns200() throws Exception {
        when(ticketService.cancel("TCK-1")).thenReturn(sampleTicket(TicketStatus.CANCELLED));

        mockMvc.perform(patch("/api/tickets/TCK-1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(ticketService).cancel("TCK-1");
    }

    // TEST-CTRL-TKT-009: cancelar ticket inválido -> 409
    @Test
    void cancel_Invalid_Returns409() throws Exception {
        when(ticketService.cancel("TCK-1"))
                .thenThrow(new BusinessRuleException("Solo se pueden cancelar tickets en estado PAID"));

        mockMvc.perform(patch("/api/tickets/TCK-1/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(ticketService).cancel("TCK-1");
    }

    // TEST-CTRL-TKT-010: marcar ticket como usado -> 200
    @Test
    void markAsUsed_Valid_Returns200() throws Exception {
        when(ticketService.markAsUsed("TCK-1")).thenReturn(sampleTicket(TicketStatus.USED));

        mockMvc.perform(patch("/api/tickets/TCK-1/use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USED"));

        verify(ticketService).markAsUsed("TCK-1");
    }

    // TEST-CTRL-TKT-011: marcar ticket inválido -> 409
    @Test
    void markAsUsed_Invalid_Returns409() throws Exception {
        when(ticketService.markAsUsed("TCK-1"))
                .thenThrow(new BusinessRuleException("Solo se puede marcar como usado un ticket en estado PAID"));

        mockMvc.perform(patch("/api/tickets/TCK-1/use"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(ticketService).markAsUsed("TCK-1");
    }
}
