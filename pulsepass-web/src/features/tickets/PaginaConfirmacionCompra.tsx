import { Link, Navigate, useParams } from 'react-router'
import { esApiError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useTicket } from './hooks'
import { formatearFechaHora, precioCOP } from '../../lib/formato'
import { BadgeEstadoTicket, BadgeTipoTicket } from '../../components/ui/Etiquetas'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'
import { IconoCheck, IconoTicket } from '../../components/ui/Iconos'

export default function PaginaConfirmacionCompra() {
  const { ticketCode: ticketCodeParam } = useParams<{ ticketCode: string }>()
  const ticketCode = ticketCodeParam?.trim() ?? ''
  const ticketQuery = useTicket(ticketCode || null)
  const ticket = ticketQuery.data ?? null

  if (!ticketCode) return <Navigate to={RUTAS.noEncontrada} replace />

  if (ticketQuery.isLoading) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <Cargando filas={2} mensaje="Confirmando compra…" />
      </div>
    )
  }

  if (ticketQuery.isError) {
    if (esApiError(ticketQuery.error) && (ticketQuery.error.status === 404 || ticketQuery.error.status === 403)) {
      return (
        <div className="mx-auto max-w-3xl px-4 py-16">
          <EstadoVacio
            titulo="Ticket no disponible"
            mensaje="No pudimos confirmar esta compra."
            accion={
              <Link to={RUTAS.eventos}>
                <Boton variante="secundario">Ver cartelera</Boton>
              </Link>
            }
          />
        </div>
      )
    }
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <EstadoError
          mensaje="No pudimos confirmar la compra."
          onReintentar={() => void ticketQuery.refetch()}
        />
      </div>
    )
  }

  if (!ticket) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16">
        <EstadoVacio titulo="Ticket no disponible" mensaje="No encontramos este ticket." />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6">
      <section className="rounded-2xl border border-exito-100 bg-white p-8 text-center shadow-sm dark:border-exito-700/40 dark:bg-slate-900">
        <div className="mx-auto flex size-16 items-center justify-center rounded-full bg-exito-50 text-exito-700 dark:bg-exito-700/20 dark:text-exito-100">
          <IconoCheck className="size-8" />
        </div>
        <p className="mt-4 text-xs font-bold tracking-widest text-exito-700 uppercase dark:text-exito-100">
          Compra confirmada
        </p>
        <h1 className="mt-2 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          ¡Listo! Tu entrada está reservada.
        </h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          Guarda tu código para validar el ingreso.
        </p>

        <div className="mt-6 rounded-2xl bg-slate-50 p-5 dark:bg-slate-800/60">
          <p className="text-xs font-bold tracking-widest text-slate-500 uppercase dark:text-slate-400">
            Código
          </p>
          <p className="mt-2 font-mono text-2xl font-extrabold tracking-wider text-slate-900 dark:text-white">
            {ticket.ticketCode}
          </p>
        </div>

        <dl className="mt-6 grid gap-4 text-left sm:grid-cols-2">
          <div>
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Evento
            </dt>
            <dd className="mt-1 font-semibold text-slate-900 dark:text-white">{ticket.eventName}</dd>
          </div>
          <div>
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Tipo
            </dt>
            <dd className="mt-1">
              <BadgeTipoTicket tipo={ticket.type} />
            </dd>
          </div>
          <div>
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Precio
            </dt>
            <dd className="mt-1 font-semibold text-slate-900 dark:text-white">{precioCOP(ticket.price)}</dd>
          </div>
          <div>
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Estado
            </dt>
            <dd className="mt-1">
              <BadgeEstadoTicket status={ticket.status} />
            </dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Compra
            </dt>
            <dd className="mt-1 text-slate-800 dark:text-slate-100">
              {formatearFechaHora(ticket.purchaseDate)}
            </dd>
          </div>
        </dl>

        <div className="mt-7 flex flex-wrap justify-center gap-3">
          <Link to={RUTAS.ticket(ticket.ticketCode)}>
            <Boton variante="secundario">
              <IconoTicket className="size-4" />
              Ver ticket
            </Boton>
          </Link>
          <Link to={RUTAS.misTickets}>
            <Boton>Mis tickets</Boton>
          </Link>
          <Link to={RUTAS.eventos}>
            <Boton variante="fantasma">Seguir explorando</Boton>
          </Link>
        </div>
      </section>
    </div>
  )
}