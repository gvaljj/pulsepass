import { useState } from 'react'
import { Link, Navigate, useParams } from 'react-router'
import { esApiError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useAuth } from '../auth/AuthContext'
import { useAviso } from '../../components/ui/Aviso'
import { useTicket } from './hooks'
import { useUsuarioPorEmail } from '../auth/hooks'
import { formatearFechaHora, precioCOP } from '../../lib/formato'
import { ModalCancelar } from '../../components/tickets/ModalCancelar'
import { BadgeEstadoTicket, BadgeTipoTicket } from '../../components/ui/Etiquetas'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'


export default function PaginaDetalleTicket() {
  const { ticketCode: ticketCodeParam } = useParams<{ ticketCode: string }>()
  const ticketCode = ticketCodeParam?.trim() ?? ''
  const { sesion } = useAuth()
  const { mostrarAviso } = useAviso()
  const email = sesion.estado === 'autenticado' ? sesion.usuario.email : null
  const ticketQuery = useTicket(ticketCode || null)
  const perfilQuery = useUsuarioPorEmail(email)
  const ticket = ticketQuery.data ?? null
  const [ticketACancelar, setTicketACancelar] = useState(false)


  if (!ticketCode) return <Navigate to={RUTAS.noEncontrada} replace />

  if (ticketQuery.isLoading) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <Cargando filas={2} mensaje="Cargando ticket…" />
      </div>
    )
  }

  if (ticketQuery.isError) {
    if (esApiError(ticketQuery.error) && (ticketQuery.error.status === 404 || ticketQuery.error.status === 403)) {
      return (
        <div className="mx-auto max-w-3xl px-4 py-16">
          <EstadoVacio
            titulo="Ticket no disponible"
            mensaje="No pudimos encontrar este ticket o no tienes acceso a él."
            accion={
              <Link to={RUTAS.misTickets}>
                <Boton variante="secundario">Mis tickets</Boton>
              </Link>
            }
          />
        </div>
      )
    }
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <EstadoError
          mensaje="No pudimos cargar el ticket."
          onReintentar={() => void ticketQuery.refetch()}
        />
      </div>
    )
  }

  if (!ticket) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16">
        <EstadoVacio
          titulo="Ticket no disponible"
          mensaje="No pudimos encontrar este ticket."
        />
      </div>
    )
  }

  const titular =
    perfilQuery.data
      ? `${perfilQuery.data.firstName} ${perfilQuery.data.lastName}`
      : ticket.userEmail

  async function copiarCodigo() {
    try {
      await navigator.clipboard?.writeText(ticket!.ticketCode)
      mostrarAviso('Código copiado al portapapeles.')
    } catch {
      mostrarAviso('No pudimos copiar el código.', 'error')
    }
  }

  function cancelado() {
    mostrarAviso('Ticket cancelado correctamente.')
    setTicketACancelar(false)
    void ticketQuery.refetch()
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6">
      <Link
        to={RUTAS.misTickets}
        className="inline-flex items-center gap-1 text-sm font-semibold text-slate-500 hover:text-marca-700 dark:text-slate-400 dark:hover:text-marca-300"
      >
        Mis tickets
      </Link>

      <section className="mt-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
              Ticket
            </p>
            <h1 className="mt-1 text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">
              {ticket.eventName}
            </h1>
          </div>
          <BadgeEstadoTicket status={ticket.status} />
        </div>

        <div className="mt-6 rounded-2xl bg-slate-50 p-5 text-center dark:bg-slate-800/60">
          <p className="text-xs font-bold tracking-widest text-slate-500 uppercase dark:text-slate-400">
            Código de ticket
          </p>
          <p className="mt-2 font-mono text-2xl font-extrabold tracking-wider text-slate-900 dark:text-white">
            {ticket.ticketCode}
          </p>
          <Boton variante="secundario" tamano="sm" className="mt-3" onClick={() => void copiarCodigo()}>
            Copiar código
          </Boton>
        </div>

        <dl className="mt-6 grid gap-4 sm:grid-cols-2">
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
              Titular
            </dt>
            <dd className="mt-1 text-slate-800 dark:text-slate-100">{titular}</dd>
          </div>
          <div>
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Compra
            </dt>
            <dd className="mt-1 text-slate-800 dark:text-slate-100">
              {formatearFechaHora(ticket.purchaseDate)}
            </dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Evento
            </dt>
            <dd className="mt-1">
              <Link
                to={RUTAS.evento(ticket.eventCode)}
                className="font-semibold text-marca-700 hover:text-marca-800 dark:text-marca-300 dark:hover:text-marca-200"
              >
                {ticket.eventName}
              </Link>
            </dd>
          </div>
        </dl>

        {ticket.status === 'PAID' && (
          <div className="mt-6 flex justify-end">
            <Boton variante="peligro" onClick={() => setTicketACancelar(true)}>
              Cancelar ticket
            </Boton>
          </div>
        )}
      </section>

      <ModalCancelar
        abierto={ticketACancelar}
        ticket={ticket}
        email={email ?? ticket.userEmail}
        eventCode={ticket.eventCode}
        onCerrar={() => setTicketACancelar(false)}
        onCancelado={() => cancelado()}
      />
    </div>
  )
}
