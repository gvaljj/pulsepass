import { z } from 'zod'
import { solicitar } from '../../lib/http'
import {
  EventSchema,
  EventSummarySchema,
  type CreateEventRequest,
  type Event,
  type EventStatus,
  type EventSummary,
} from './tipos'

const EventSummaryListSchema = z.array(EventSummarySchema)

export function listarEventosPublicados(): Promise<EventSummary[]> {
  return solicitar('/events/published', { esquema: EventSummaryListSchema })
}

export function obtenerEvento(eventCode: string): Promise<Event> {
  return solicitar(`/events/${encodeURIComponent(eventCode)}`, { esquema: EventSchema })
}

export function listarEventosPorArtista(stageName: string): Promise<EventSummary[]> {
  return solicitar(`/events/by-artist?stageName=${encodeURIComponent(stageName)}`, {
    esquema: EventSummaryListSchema,
  })
}

export function crearEvento(cuerpo: CreateEventRequest): Promise<Event> {
  return solicitar('/events', {
    metodo: 'POST',
    cuerpo,
    esquema: EventSchema,
  })
}

export function publicarEvento(eventCode: string, status: EventStatus): Promise<Event> {
  return solicitar(`/events/${encodeURIComponent(eventCode)}`, {
    metodo: 'PATCH',
    cuerpo: { status },
    esquema: EventSchema,
  })
}

export function asociarArtista(eventCode: string, artistId: number): Promise<Event> {
  return solicitar(
    `/events/${encodeURIComponent(eventCode)}/artists/${encodeURIComponent(String(artistId))}`,
    {
      metodo: 'POST',
      esquema: EventSchema,
    },
  )
}
