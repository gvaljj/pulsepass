import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { esApiError, mensajeDeError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useAviso } from '../../components/ui/Aviso'
import { useTicketsPagadosPorEvento, useUsarTicket } from '../../features/tickets/hooks'
import type { Ticket } from '../../features/tickets/tipos'
import { precioCOP } from '../../lib/formato'
import { Tabla } from '../../components/ui/Tabla'
import { BadgeEstadoTicket, BadgeTipoTicket } from '../../components/ui/Etiquetas'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'


function AccionUsar({ ticket, eventCode }: { ticket: Ticket; eventCode: string }) {
  const { mostrarAviso } = useAviso()
  const usar = useUsarTicket(ticket.ticketCode, eventCode)

  async function marcar() {
    try {
      await usar.mutateAsync()
      mostrarAviso('Ticket marcado como usado.')
    } catch (error) {
      if (esApiError(error)) {
        mostrarAviso(error.message || 'No se pudo marcar el ticket.', 'error')
      } else {
        mostrarAviso(mensajeDeError(error, 'No se pudo marcar el ticket.'), 'error')
      }
    }
  }

  if (ticket.status !== 'PAID') {
    return <span className="text-xs text-slate-400">—</span>
  }

  return (
    <Boton variante="secundario" tamano="sm" cargando={usar.isPending} onClick={() => void marcar()}>
      Usar
    </Boton>
  )
}

export default function PaginaTicketsPagadosEvento() {
  const { eventCode: eventCodeParam } = useParams<{ eventCode: string }>()
  const navigate = useNavigate()
  const [codigo, setCodigo] = useState(eventCodeParam ?? '')
  const eventCode = eventCodeParam?.trim() ?? ''
  const ticketsQuery = useTicketsPagadosPorEvento(eventCode || null)
  const tickets = ticketsQuery.data ?? []

  if (!eventCode) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <h1 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Tickets pagados por evento
        </h1>
        <form
          className="mt-6 flex flex-col gap-3 sm:flex-row"
          onSubmit={(evento) => {
            evento.preventDefault()
            const buscado = codigo.trim()
            if (buscado) navigate(RUTAS.staffTickets(buscado))
          }}
        >
          <input
            value={codigo}
            onChange={(evento) => setCodigo(evento.target.value)}
            placeholder="Código de evento"
            className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm focus:border-marca-500 focus:ring-2 focus:ring-marca-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
          />
          <Boton type="submit" tamano="lg" className="sm:w-40">
            Buscar
          </Boton>
        </form>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
            Operación
          </p>
          <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
            Tickets pagados
          </h1>
          <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
            Evento <span className="font-mono font-semibold">{eventCode}</span> · {tickets.length} tickets
          </p>
        </div>
        <Link to={RUTAS.staffValidar} className="text-sm font-bold text-marca-700 hover:text-marca-800 dark:text-marca-300 dark:hover:text-marca-200">
          Validar ingreso
        </Link>
      </header>

      <form
        className="mt-6 flex flex-col gap-3 sm:flex-row"
        onSubmit={(evento) => {
          evento.preventDefault()
          const buscado = codigo.trim()
          if (buscado) navigate(RUTAS.staffTickets(buscado))
        }}
      >
        <input
          value={codigo}
          onChange={(evento) => setCodigo(evento.target.value)}
          placeholder="Cambiar código de evento"
          className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm focus:border-marca-500 focus:ring-2 focus:ring-marca-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
        />
        <Boton type="submit" tamano="lg" className="sm:w-40">
          Ir
        </Boton>
      </form>

      <div className="mt-6">
        {ticketsQuery.isLoading ? (
          <Cargando filas={4} mensaje="Cargando tickets pagados…" />
        ) : ticketsQuery.isError ? (
          esApiError(ticketsQuery.error) && ticketsQuery.error.status === 404 ? (
            <EstadoError
              titulo="Evento no encontrado"
              mensaje="No encontramos tickets para este evento."
            />
          ) : (
            <EstadoError
              mensaje={mensajeDeError(ticketsQuery.error, 'No pudimos cargar los tickets.')}
              onReintentar={() => void ticketsQuery.refetch()}
            />
          )
        ) : tickets.length === 0 ? (
          <EstadoVacio
            titulo="Sin tickets pagados"
            mensaje="Este evento todavía no tiene tickets pagados."
          />
        ) : (
          <>
            <div className="hidden lg:block">
              <Tabla
                etiqueta="Tickets pagados del evento"
                filas={tickets}
                claveFila={(fila) => fila.ticketCode}
                columnas={[
                  {
                    clave: 'codigo',
                    encabezado: 'Código',
                    render: (fila) => <span className="font-mono text-xs">{fila.ticketCode}</span>,
                  },
                  {
                    clave: 'titular',
                    encabezado: 'Titular',
                    render: (fila) => <span className="truncate">{fila.userEmail}</span>,
                  },
                  {
                    clave: 'tipo',
                    encabezado: 'Tipo',
                    render: (fila) => <BadgeTipoTicket tipo={fila.type} />,
                  },
                  {
                    clave: 'precio',
                    encabezado: 'Precio',
                    render: (fila) => precioCOP(fila.price),
                    alineacion: 'derecha',
                  },
                  {
                    clave: 'estado',
                    encabezado: 'Estado',
                    render: (fila) => <BadgeEstadoTicket status={fila.status} />,
                  },
                  {
                    clave: 'accion',
                    encabezado: 'Acción',
                    render: (fila) => <AccionUsar ticket={fila} eventCode={eventCode} />,
                    alineacion: 'centro',
                  },
                ]}
              />
            </div>

            <div className="space-y-3 lg:hidden">
              {tickets.map((ticket) => (
                <article
                  key={ticket.ticketCode}
                  className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="font-mono text-xs text-slate-500 dark:text-slate-400">{ticket.ticketCode}</p>
                      <p className="mt-1 font-semibold text-slate-900 dark:text-white">{ticket.userEmail}</p>
                    </div>
                    <BadgeEstadoTicket status={ticket.status} />
                  </div>
                  <div className="mt-3 flex flex-wrap items-center justify-between gap-2 text-sm">
                    <BadgeTipoTicket tipo={ticket.type} />
                    <span className="font-semibold text-slate-900 dark:text-white">{precioCOP(ticket.price)}</span>
                  </div>
                  <div className="mt-3">
                    <AccionUsar ticket={ticket} eventCode={eventCode} />
                  </div>
                </article>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  )
}