import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  cancelarTicket,
  comprarTicket,
  listarTicketsPorUsuario,
  listarTicketsPagadosPorEvento,
  obtenerTicket,
  usarTicket,
} from './api'
import type { Ticket, TicketType } from './tipos'

export function useTicketsUsuario(email: string | null) {
  return useQuery({
    queryKey: ['tickets', 'usuario', email],
    queryFn: () => listarTicketsPorUsuario(email!),
    enabled: Boolean(email),
    staleTime: 30_000,
  })
}

export function useTicket(ticketCode: string | null) {
  return useQuery({
    queryKey: ['ticket', ticketCode],
    queryFn: () => obtenerTicket(ticketCode!),
    enabled: Boolean(ticketCode),
    staleTime: 30_000,
  })
}

export function useTicketsPagadosPorEvento(eventCode: string | null) {
  return useQuery({
    queryKey: ['tickets', 'pagados', eventCode],
    queryFn: () => listarTicketsPagadosPorEvento(eventCode!),
    enabled: Boolean(eventCode),
    staleTime: 30_000,
  })
}

export function useComprarTicket(eventCode: string, email: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (type: TicketType) => comprarTicket({ userEmail: email, eventCode, type }),
    onSuccess: (ticket: Ticket) => {
      queryClient.setQueryData(['ticket', ticket.ticketCode], ticket)
      void queryClient.invalidateQueries({ queryKey: ['tickets', 'usuario', email] })
      void queryClient.invalidateQueries({ queryKey: ['evento', eventCode] })
    },
  })
}

export function useCancelarTicket(ticketCode: string, email: string, eventCode: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => cancelarTicket(ticketCode),
    onSuccess: (ticket: Ticket) => {
      queryClient.setQueryData(['ticket', ticket.ticketCode], ticket)
      void queryClient.invalidateQueries({ queryKey: ['tickets', 'usuario', email] })
      void queryClient.invalidateQueries({ queryKey: ['tickets', 'pagados', eventCode] })
    },
  })
}

export function useUsarTicket(ticketCode: string, eventCode: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => usarTicket(ticketCode),
    onSuccess: (ticket: Ticket) => {
      queryClient.setQueryData(['ticket', ticket.ticketCode], ticket)
      void queryClient.invalidateQueries({ queryKey: ['tickets', 'pagados', eventCode] })
    },
  })
}
