import { z } from 'zod'
import { solicitar } from '../../lib/http'
import { ArtistSchema, type Artist } from './tipos'

const ArtistListSchema = z.array(ArtistSchema)

export function listarArtistasActivos(): Promise<Artist[]> {
  return solicitar('/artists/active', { esquema: ArtistListSchema })
}

export function obtenerArtista(id: number): Promise<Artist> {
  return solicitar(`/artists/${id}`, { esquema: ArtistSchema })
}

export function obtenerArtistaPorNombre(stageName: string): Promise<Artist> {
  return solicitar(`/artists/by-stage-name?stageName=${encodeURIComponent(stageName)}`, {
    esquema: ArtistSchema,
  })
}
