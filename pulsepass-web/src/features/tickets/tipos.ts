import { z } from 'zod'

export const TicketTypeSchema = z.enum(['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'])
export const TicketStatusSchema = z.enum(['PAID', 'USED', 'CANCELLED'])

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
