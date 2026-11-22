import { z } from 'zod'

export const TIPOS_TICKET = ['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'] as const
export const ESTADOS_TICKET = ['PAID', 'USED', 'CANCELLED'] as const

export const TicketTypeSchema = z.enum(TIPOS_TICKET)
export const TicketStatusSchema = z.enum(ESTADOS_TICKET)

export type TicketType = z.infer<typeof TicketTypeSchema>
export type TicketStatus = z.infer<typeof TicketStatusSchema>

export const TicketSchema = z.object({
  id: z.number(),
  ticketCode: z.string(),
  type: TicketTypeSchema,
  price: z.coerce.number(),
  status: TicketStatusSchema,
  purchaseDate: z.string(),
  userEmail: z.string(),
  eventCode: z.string(),
  eventName: z.string(),
})

export type Ticket = z.infer<typeof TicketSchema>

export const PurchaseTicketRequestSchema = z.object({
  userEmail: z.string().email(),
  eventCode: z.string().min(1),
  type: TicketTypeSchema,
})

export type PurchaseTicketRequest = z.infer<typeof PurchaseTicketRequestSchema>
