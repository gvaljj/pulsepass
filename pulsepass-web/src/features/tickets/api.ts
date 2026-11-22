import { z } from 'zod'
import { solicitar } from '../../lib/http'
import {
  TicketSchema,
  type PurchaseTicketRequest,
  type Ticket,
} from './tipos'

const TicketListSchema = z.array(TicketSchema)

export function listarTicketsPorUsuario(email: string): Promise<Ticket[]> {
  return solicitar(`/tickets/by-user?email=${encodeURIComponent(email)}`, {
    esquema: TicketListSchema,
  })
}

export function obtenerTicket(ticketCode: string): Promise<Ticket> {
  return solicitar(`/tickets/${encodeURIComponent(ticketCode)}`, { esquema: TicketSchema })
}

export function comprarTicket(cuerpo: PurchaseTicketRequest): Promise<Ticket> {
  return solicitar('/tickets', {
    metodo: 'POST',
    cuerpo,
    esquema: TicketSchema,
  })
}

export function cancelarTicket(ticketCode: string): Promise<Ticket> {
  return solicitar(`/tickets/${encodeURIComponent(ticketCode)}/cancel`, {
    metodo: 'PATCH',
    esquema: TicketSchema,
  })
}

export function usarTicket(ticketCode: string): Promise<Ticket> {
  return solicitar(`/tickets/${encodeURIComponent(ticketCode)}/use`, {
    metodo: 'PATCH',
    esquema: TicketSchema,
  })
}

export function listarTicketsPagadosPorEvento(eventCode: string): Promise<Ticket[]> {
  return solicitar(`/events/${encodeURIComponent(eventCode)}/tickets/paid`, {
    esquema: TicketListSchema,
  })
}
