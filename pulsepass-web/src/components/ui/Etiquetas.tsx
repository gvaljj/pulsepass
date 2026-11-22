import { Badge, type TonoBadge } from './Badge'
import {
  etiquetaCategoria,
  etiquetaEstadoEvento,
  etiquetaEstadoTicket,
  etiquetaTipoTicket,
} from './etiquetas'
import type { EventCategory, EventStatus } from '../../features/eventos/tipos'
import type { TicketStatus, TicketType } from '../../features/tickets/tipos'

const estadoEventoTono: Record<EventStatus, TonoBadge> = {
  DRAFT: 'neutral',
  PUBLISHED: 'exito',
  SOLD_OUT: 'aviso',
  CANCELLED: 'peligro',
  FINISHED: 'info',
}

const tipoTicketTono: Record<TicketType, TonoBadge> = {
  GENERAL: 'neutral',
  VIP: 'marca',
  BACKSTAGE: 'aviso',
  STUDENT: 'info',
}

const estadoTicketTono: Record<TicketStatus, TonoBadge> = {
  PAID: 'exito',
  USED: 'info',
  CANCELLED: 'peligro',
}

export function BadgeCategoria({ categoria }: { categoria: EventCategory }) {
  return <Badge tono="marca">{etiquetaCategoria(categoria)}</Badge>
}

export function BadgeEstadoEvento({ status }: { status: EventStatus }) {
  return <Badge tono={estadoEventoTono[status]}>{etiquetaEstadoEvento(status)}</Badge>
}

export function BadgeTipoTicket({ tipo }: { tipo: TicketType }) {
  return <Badge tono={tipoTicketTono[tipo]}>{etiquetaTipoTicket(tipo)}</Badge>
}

export function BadgeEstadoTicket({ status }: { status: TicketStatus }) {
  return <Badge tono={estadoTicketTono[status]}>{etiquetaEstadoTicket(status)}</Badge>
}
