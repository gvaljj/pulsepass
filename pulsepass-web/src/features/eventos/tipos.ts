import { z } from 'zod'
import { ArtistSchema } from '../artistas/tipos'

export const CATEGORIAS_EVENTO = [
  'MUSIC',
  'TECHNOLOGY',
  'SPORTS',
  'CULTURE',
  'EDUCATION',
  'ENTERTAINMENT',
] as const

export const ESTADOS_EVENTO = [
  'DRAFT',
  'PUBLISHED',
  'SOLD_OUT',
  'CANCELLED',
  'FINISHED',
] as const

export const EventCategorySchema = z.enum(CATEGORIAS_EVENTO)
export const EventStatusSchema = z.enum(ESTADOS_EVENTO)

export type EventCategory = z.infer<typeof EventCategorySchema>
export type EventStatus = z.infer<typeof EventStatusSchema>

export const EventSummarySchema = z.object({
  id: z.number(),
  eventCode: z.string(),
  name: z.string(),
  category: EventCategorySchema,
  status: EventStatusSchema,
  eventDate: z.string(),
  venueCode: z.string(),
})

export const EventSchema = EventSummarySchema.extend({
  description: z.string().nullable().optional(),
  minimumAge: z.number(),
  venueName: z.string(),
  artists: z.array(ArtistSchema),
})

export type EventSummary = z.infer<typeof EventSummarySchema>
export type Event = z.infer<typeof EventSchema>

export const CreateEventRequestSchema = z.object({
  eventCode: z.string().trim().min(1, 'El código es obligatorio'),
  name: z.string().trim().min(1, 'El nombre es obligatorio'),
  description: z
    .string()
    .max(1000, 'La descripción no puede superar 1000 caracteres')
    .optional()
    .or(z.literal('')),
  category: EventCategorySchema,
  eventDate: z.string().min(1, 'La fecha es obligatoria'),
  minimumAge: z.coerce.number().int().min(0, 'La edad mínima no puede ser negativa'),
  venueCode: z.string().trim().min(1, 'La sede es obligatoria'),
})

export type CreateEventRequest = z.infer<typeof CreateEventRequestSchema>

export const PublishEventRequestSchema = z.object({
  status: EventStatusSchema,
})

export type PublishEventRequest = z.infer<typeof PublishEventRequestSchema>
