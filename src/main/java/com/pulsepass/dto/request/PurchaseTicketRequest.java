package com.pulsepass.dto.request;

import com.pulsepass.domain.enums.TicketType;

// PRD §22: campos mínimos de compra. El precio lo define el sistema, nunca el cliente.
public record PurchaseTicketRequest(
        String userEmail,
        String eventCode,
        TicketType type
) {}
