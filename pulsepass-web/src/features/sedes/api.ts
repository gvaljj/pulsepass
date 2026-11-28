import { z } from 'zod'
import { solicitar } from '../../lib/http'
import { VenueSchema, type Venue } from './tipos'

const VenueListSchema = z.array(VenueSchema)

export function listarSedesActivas(): Promise<Venue[]> {
  return solicitar('/venues/active', { esquema: VenueListSchema })
}

export function obtenerSede(code: string): Promise<Venue> {
  return solicitar(`/venues/${encodeURIComponent(code)}`, { esquema: VenueSchema })
}
