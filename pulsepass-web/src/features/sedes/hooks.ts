import { useQuery } from '@tanstack/react-query'
import { listarSedesActivas, obtenerSede } from './api'

export function useSedesActivas() {
  return useQuery({
    queryKey: ['sedes', 'activas'],
    queryFn: listarSedesActivas,
    staleTime: 5 * 60_000,
  })
}

export function useSede(code: string | null) {
  return useQuery({
    queryKey: ['sede', code],
    queryFn: () => obtenerSede(code!),
    enabled: Boolean(code),
    staleTime: 5 * 60_000,
  })
}
