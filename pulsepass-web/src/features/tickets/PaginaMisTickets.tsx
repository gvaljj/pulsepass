import { useMemo, useState } from 'react'
import { Link } from 'react-router'
import { useAuth } from '../auth/AuthContext'
import { useAviso } from '../../components/ui/Aviso'
import { useTicketsUsuario } from './hooks'
import type { Ticket, TicketStatus } from './tipos'
import { RUTAS } from '../../lib/rutas'
import { formatearFechaHora, precioCOP } from '../../lib/formato'
import { ModalCancelar } from '../../components/tickets/ModalCancelar'
import { BadgeEstadoTicket, BadgeTipoTicket } from '../../components/ui/Etiquetas'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'


const filtros: { valor: 'TODOS' | TicketStatus; etiqueta: string }[] = [
  { valor: 'TODOS', etiqueta: 'Todos' },
  { valor: 'PAID', etiqueta: 'Pagados' },
  { valor: 'USED', etiqueta: 'Usados' },
  { valor: 'CANCELLED', etiqueta: 'Cancelados' },
]

export default function PaginaMisTickets() {
  const { sesion } = useAuth()
  const { mostrarAviso } = useAviso()
  const email = sesion.estado === 'autenticado' ? sesion.usuario.email : null
  const ticketsQuery = useTicketsUsuario(email)
  const [filtro, setFiltro] = useState<'TODOS' | TicketStatus>('TODOS')
  const [ticketACancelar, setTicketACancelar] = useState<Ticket | null>(null)

  const tickets = useMemo(() => ticketsQuery.data ?? [], [ticketsQuery.data])
  const ticketsFiltrados = useMemo(
    () => (filtro === 'TODOS' ? tickets : tickets.filter((ticket) => ticket.status === filtro)),
    [tickets, filtro],
  )

  function cancelado() {
    mostrarAviso('Ticket cancelado correctamente.')
    setTicketACancelar(null)
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
            Entradas
          </p>
          <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
            Mis tickets
          </h1>
          <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
            Consulta y gestiona tus entradas.
          </p>
        </div>
      </header>

      <div className="mt-6 flex flex-wrap gap-2">
        {filtros.map((item) => (
          <Boton
            key={item.valor}
            variante={filtro === item.valor ? 'primario' : 'secundario'}
            tamano="sm"
            onClick={() => setFiltro(item.valor)}
          >
            {item.etiqueta}
          </Boton>
        ))}
      </div>

      <div className="mt-6">
        {ticketsQuery.isLoading ? (
          <Cargando filas={3} mensaje="Cargando tickets…" />
        ) : ticketsQuery.isError ? (
          <EstadoError
            mensaje="No pudimos cargar tus tickets."
            onReintentar={() => void ticketsQuery.refetch()}
          />
        ) : tickets.length === 0 ? (
          <EstadoVacio
            titulo="Aún no tienes tickets"
            mensaje="Compra tu primera entrada y empieza a vivir la experiencia."
            accion={
              <Link to={RUTAS.eventos}>
                <Boton>Ver cartelera</Boton>
              </Link>
            }
          />
        ) : ticketsFiltrados.length === 0 ? (
          <EstadoVacio
            titulo="Sin tickets en este estado"
            mensaje="Prueba con otro filtro."
          />
        ) : (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {ticketsFiltrados.map((ticket) => (
              <article
                key={ticket.ticketCode}
                className="flex flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
              >
                <div className="flex items-start justify-between gap-3">
                  <h2 className="text-lg font-bold text-slate-900 dark:text-white">{ticket.eventName}</h2>
                  <BadgeEstadoTicket status={ticket.status} />
                </div>
                <dl className="mt-4 space-y-2 text-sm">
                  <div className="flex justify-between gap-4">
                    <dt className="text-slate-500 dark:text-slate-400">Tipo</dt>
                    <dd>
                      <BadgeTipoTicket tipo={ticket.type} />
                    </dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt className="text-slate-500 dark:text-slate-400">Precio</dt>
                    <dd className="font-semibold text-slate-900 dark:text-white">
                      {precioCOP(ticket.price)}
                    </dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt className="text-slate-500 dark:text-slate-400">Código</dt>
                    <dd className="font-mono text-xs text-slate-700 dark:text-slate-200">{ticket.ticketCode}</dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt className="text-slate-500 dark:text-slate-400">Compra</dt>
                    <dd className="text-slate-700 dark:text-slate-200">
                      {formatearFechaHora(ticket.purchaseDate)}
                    </dd>
                  </div>
                </dl>
                <div className="mt-5 flex flex-wrap gap-2">
                  <Link to={RUTAS.ticket(ticket.ticketCode)}>
                    <Boton variante="secundario" tamano="sm">
                      Ver detalle
                    </Boton>
                  </Link>
                  {ticket.status === 'PAID' && (
                    <Boton
                      variante="peligro"
                      tamano="sm"
                      onClick={() => setTicketACancelar(ticket)}
                    >
                      Cancelar
                    </Boton>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
      </div>

      <ModalCancelar
        abierto={ticketACancelar !== null}
        ticket={ticketACancelar}
        email={email ?? ''}
        eventCode={ticketACancelar?.eventCode ?? ''}
        onCerrar={() => setTicketACancelar(null)}
        onCancelado={() => cancelado()}
      />
    </div>
  )
}
