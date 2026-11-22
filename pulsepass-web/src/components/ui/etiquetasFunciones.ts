import type { EventCategory, EventStatus } from '../../features/eventos/tipos'
import type { TicketStatus, TicketType } from '../../features/tickets/tipos'

const categoriaEtiqueta: Record<EventCategory, string> = {
  MUSIC: 'Música',
  TECHNOLOGY: 'Tecnología',
  SPORTS: 'Deportes',
  CULTURE: 'Cultura',
  EDUCATION: 'Educación',
  ENTERTAINMENT: 'Entretenimiento',
}

const estadoEventoEtiqueta: Record<EventStatus, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicado',
  SOLD_OUT: 'Agotado',
  CANCELLED: 'Cancelado',
  FINISHED: 'Finalizado',
}

const tipoTicketEtiqueta: Record<TicketType, string> = {
  GENERAL: 'General',
  VIP: 'VIP',
  BACKSTAGE: 'Backstage',
  STUDENT: 'Estudiante',
}

const estadoTicketEtiqueta: Record<TicketStatus, string> = {
  PAID: 'Pagado',
  USED: 'Usado',
  CANCELLED: 'Cancelado',
}

export function etiquetaCategoria(categoria: EventCategory): string {
  return categoriaEtiqueta[categoria]
}

export function etiquetaEstadoEvento(status: EventStatus): string {
  return estadoEventoEtiqueta[status]
}

export function etiquetaTipoTicket(tipo: TicketType): string {
  return tipoTicketEtiqueta[tipo]
}

export function etiquetaEstadoTicket(status: TicketStatus): string {
  return estadoTicketEtiqueta[status]
}
