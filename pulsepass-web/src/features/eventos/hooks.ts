import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  listarEventosPublicados,
  listarEventosPorArtista,
  obtenerEvento,
  crearEvento,
  publicarEvento,
  asociarArtista,
} from './api'
import type { CreateEventRequest, Event, EventStatus } from './tipos'

export function useEventosPublicados() {
  return useQuery({
    queryKey: ['eventos', 'publicados'],
    queryFn: listarEventosPublicados,
    staleTime: 60_000,
  })
}

export function useEvento(eventCode: string | null) {
  return useQuery({
    queryKey: ['evento', eventCode],
    queryFn: () => obtenerEvento(eventCode!),
    enabled: Boolean(eventCode),
    staleTime: 60_000,
  })
}

export function useEventosPorArtista(stageName: string | null) {
  return useQuery({
    queryKey: ['eventos', 'artista', stageName],
    queryFn: () => listarEventosPorArtista(stageName!),
    enabled: Boolean(stageName),
    staleTime: 60_000,
  })
}

export function useCrearEvento() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (datos: CreateEventRequest) => crearEvento(datos),
    onSuccess: (evento: Event) => {
      queryClient.setQueryData(['evento', evento.eventCode], evento)
    },
  })
}

export function usePublicarEvento(eventCode: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (status: EventStatus) => publicarEvento(eventCode, status),
    onSuccess: (evento: Event) => {
      queryClient.setQueryData(['evento', evento.eventCode], evento)
      void queryClient.invalidateQueries({ queryKey: ['eventos', 'publicados'] })
      void queryClient.invalidateQueries({ queryKey: ['eventos', 'artista'] })
    },
  })
}

export function useAsociarArtista(eventCode: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (artistId: number) => asociarArtista(eventCode, artistId),
    onSuccess: (evento: Event) => {
      queryClient.setQueryData(['evento', evento.eventCode], evento)
      void queryClient.invalidateQueries({ queryKey: ['eventos', 'artista'] })
    },
  })
}
