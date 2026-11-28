import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router'
import { esApiError, mensajeDeError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useAviso } from '../../components/ui/Aviso'
import { useTicket, useUsarTicket } from '../../features/tickets/hooks'
import { BadgeEstadoTicket, BadgeTipoTicket } from '../../components/ui/Etiquetas'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'


export default function PaginaValidacionIngreso() {
  const { mostrarAviso } = useAviso()
  const [codigo, setCodigo] = useState('')
  const [codigoBuscado, setCodigoBuscado] = useState<string | null>(null)
  const [errorBusqueda, setErrorBusqueda] = useState<string | undefined>()
  const inputRef = useRef<HTMLInputElement>(null)

  const ticketQuery = useTicket(codigoBuscado)
  const ticket = ticketQuery.data ?? null
  const usar = useUsarTicket(ticket?.ticketCode ?? '', ticket?.eventCode ?? '')

  useEffect(() => {
    inputRef.current?.focus()
  }, [])

  async function buscar(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const buscado = codigo.trim()
    if (!buscado) {
      setErrorBusqueda('Ingresa un código de ticket.')
      return
    }
    setErrorBusqueda(undefined)
    if (buscado === codigoBuscado) {
      await ticketQuery.refetch()
    } else {
      setCodigoBuscado(buscado)
    }
  }

  async function marcarUsado() {
    if (!ticket || ticket.status !== 'PAID') return
    try {
      await usar.mutateAsync()
      setCodigo('')
      mostrarAviso('Ingreso validado correctamente.')
    } catch (error) {
      if (esApiError(error)) {
        if (error.status === 409) {
          mostrarAviso(error.message || 'No se puede marcar este ticket.', 'error')
        } else if (error.status === 403) {
          mostrarAviso(error.message || 'Sin permisos.', 'error')
        } else {
          mostrarAviso(mensajeDeError(error, 'No pudimos validar el ingreso.'), 'error')
        }
      } else {
        mostrarAviso(mensajeDeError(error, 'No pudimos validar el ingreso.'), 'error')
      }
    }
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6">
      <header>
        <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
          Operación
        </p>
        <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Validación de ingreso
        </h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          Escanea o ingresa el código del ticket.
        </p>
      </header>

      <form className="mt-6 flex flex-col gap-3 sm:flex-row" onSubmit={buscar}>
        <label htmlFor="codigo-ticket" className="sr-only">
          Código de ticket
        </label>
        <input
          id="codigo-ticket"
          ref={inputRef}
          value={codigo}
          onChange={(evento) => setCodigo(evento.target.value)}
          placeholder="Código de ticket"
          className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm focus:border-marca-500 focus:ring-2 focus:ring-marca-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
        />
        <Boton type="submit" tamano="lg" className="sm:w-40">
          Buscar
        </Boton>
      </form>

      {errorBusqueda && (
        <p role="alert" className="mt-3 rounded-xl bg-peligro-50 p-3 text-sm text-peligro-800 dark:bg-peligro-700/20 dark:text-peligro-100">
          {errorBusqueda}
        </p>
      )}

      <div className="mt-6">
        {ticketQuery.isLoading && codigoBuscado ? (
          <Cargando filas={1} mensaje="Buscando ticket…" />
        ) : ticketQuery.isError && codigoBuscado ? (
          esApiError(ticketQuery.error) && ticketQuery.error.status === 404 ? (
            <p role="alert" className="rounded-xl bg-peligro-50 p-4 text-sm text-peligro-800 dark:bg-peligro-700/20 dark:text-peligro-100">
              Ticket no encontrado.
            </p>
          ) : esApiError(ticketQuery.error) && ticketQuery.error.status === 403 ? (
            <p role="alert" className="rounded-xl bg-peligro-50 p-4 text-sm text-peligro-800 dark:bg-peligro-700/20 dark:text-peligro-100">
              Sin permisos para validar tickets.
            </p>
          ) : (
            <EstadoError
              mensaje={mensajeDeError(ticketQuery.error, 'No pudimos buscar el ticket.')}
              onReintentar={() => void ticketQuery.refetch()}
            />
          )
        ) : ticket ? (
          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="text-xs font-bold tracking-widest text-slate-500 uppercase dark:text-slate-400">
                  Evento
                </p>
                <h2 className="mt-1 text-xl font-bold text-slate-900 dark:text-white">{ticket.eventName}</h2>
              </div>
              <BadgeEstadoTicket status={ticket.status} />
            </div>

            <dl className="mt-5 grid gap-4 sm:grid-cols-2">
              <div>
                <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                  Código
                </dt>
                <dd className="mt-1 font-mono text-sm text-slate-800 dark:text-slate-100">{ticket.ticketCode}</dd>
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
                  Titular
                </dt>
                <dd className="mt-1 text-slate-800 dark:text-slate-100">{ticket.userEmail}</dd>
              </div>
              <div>
                <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                  Compra
                </dt>
                <dd className="mt-1 text-slate-800 dark:text-slate-100">
                  {new Date(ticket.purchaseDate).toLocaleString('es-CO')}
                </dd>
              </div>
            </dl>

            <div className="mt-6 flex flex-wrap items-center justify-between gap-3">
              {ticket.status === 'PAID' && (
                <Boton onClick={() => void marcarUsado()} cargando={usar.isPending}>
                  Marcar como usado
                </Boton>
              )}
              {ticket.status === 'USED' && (
                <p className="text-sm font-semibold text-info-700 dark:text-info-100">
                  Este ticket ya fue usado.
                </p>
              )}
              {ticket.status === 'CANCELLED' && (
                <p className="text-sm font-semibold text-peligro-700 dark:text-peligro-100">
                  Este ticket está cancelado.
                </p>
              )}
              <Link
                to={RUTAS.staffTickets(ticket.eventCode)}
                className="text-sm font-bold text-marca-700 hover:text-marca-800 dark:text-marca-300 dark:hover:text-marca-200"
              >
                Ver tickets pagados del evento
              </Link>
            </div>
          </section>
        ) : null}
      </div>
    </div>
  )
}