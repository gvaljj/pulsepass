import { useQuery } from '@tanstack/react-query'
import { listarArtistasActivos, obtenerArtista, obtenerArtistaPorNombre } from './api'

export function useArtistasActivos() {
  return useQuery({
    queryKey: ['artistas', 'activos'],
    queryFn: listarArtistasActivos,
    staleTime: 5 * 60_000,
  })
}

export function useArtista(id: number | null) {
  return useQuery({
    queryKey: ['artista', id],
    queryFn: () => obtenerArtista(id!),
    enabled: id !== null && Number.isInteger(id) && id > 0,
    staleTime: 5 * 60_000,
  })
}

export function useArtistaPorNombre(stageName: string | null) {
  return useQuery({
    queryKey: ['artista', 'nombre', stageName],
    queryFn: () => obtenerArtistaPorNombre(stageName!),
    enabled: Boolean(stageName),
    staleTime: 5 * 60_000,
  })
}
