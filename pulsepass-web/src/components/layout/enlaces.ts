import type { Sesion } from '../../features/auth/AuthContext'
import { RUTAS } from '../../lib/rutas'

export interface EnlaceNav {
  etiqueta: string
  to: string
  end?: boolean
}

export const ENLACES_PRINCIPALES: EnlaceNav[] = [
  { etiqueta: 'Inicio', to: RUTAS.inicio, end: true },
  { etiqueta: 'Eventos', to: RUTAS.eventos },
  { etiqueta: 'Artistas', to: RUTAS.artistas },
  { etiqueta: 'Sedes', to: RUTAS.sedes },
]

const CODIGO_EVENTO_DEMO = 'CMF-2026'

export function enlacesDeSesion(sesion: Sesion): EnlaceNav[] {
  if (sesion.estado !== 'autenticado') return []

  const roles = sesion.usuario.roles
  const enlaces: EnlaceNav[] = []

  if (roles.includes('USER')) {
    enlaces.push({ etiqueta: 'Mis tickets', to: RUTAS.misTickets })
  }

  if (roles.includes('STAFF') || roles.includes('ADMIN')) {
    enlaces.push(
      { etiqueta: 'Validar ingreso', to: RUTAS.staffValidar },
      { etiqueta: 'Tickets pagados', to: RUTAS.staffTickets(CODIGO_EVENTO_DEMO) },
    )
  }

  if (roles.includes('ADMIN')) {
    enlaces.push({ etiqueta: 'Administrar eventos', to: RUTAS.adminEventos })
  }

  return enlaces
}
