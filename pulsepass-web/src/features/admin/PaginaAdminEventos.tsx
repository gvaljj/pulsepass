import { useState } from 'react'
import { Link } from 'react-router'
import { esApiError, mensajeDeError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useEvento } from '../../features/eventos/hooks'
import type { EventStatus } from '../../features/eventos/tipos'
import { BadgeEstadoEvento } from '../../components/ui/Etiquetas'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { leerEventosCreados, type EventoCreado } from './sesionEventos'

export default function PaginaAdminEventos() {
  const [codigo, setCodigo] = useState('')
  const [codigoBuscado, setCodigoBuscado] = useState<string | null>(null)
  const [creados] = useState<EventoCreado[]>(() => leerEventosCreados())
  const busqueda = useEvento(codigoBuscado)

  function buscar(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const buscado = codigo.trim()
    if (buscado) setCodigoBuscado(buscado)
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
            Administración
          </p>
          <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
            Eventos
          </h1>
          <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
            Busca un evento por código o crea uno nuevo.
          </p>
        </div>
        <Link to={RUTAS.adminCrearEvento}>
          <Boton>Crear evento</Boton>
        </Link>
      </header>

      <form className="mt-6 flex flex-col gap-3 sm:flex-row" onSubmit={buscar}>
        <input
          value={codigo}
          onChange={(evento) => setCodigo(evento.target.value)}
          placeholder="Buscar evento por código"
          className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm focus:border-marca-500 focus:ring-2 focus:ring-marca-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
        />
        <Boton type="submit" tamano="lg" className="sm:w-40">
          Buscar
        </Boton>
      </form>

      {codigoBuscado && (
        <div className="mt-6">
          {busqueda.isLoading ? (
            <Cargando filas={1} mensaje="Buscando evento…" />
          ) : busqueda.isError ? (
            esApiError(busqueda.error) && busqueda.error.status === 404 ? (
              <p role="alert" className="rounded-xl bg-peligro-50 p-4 text-sm text-peligro-800 dark:bg-peligro-700/20 dark:text-peligro-100">
                Evento no encontrado.
              </p>
            ) : (
              <EstadoError
                mensaje={mensajeDeError(busqueda.error, 'No pudimos buscar el evento.')}
                onReintentar={() => void busqueda.refetch()}
              />
            )
          ) : busqueda.data ? (
            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <h2 className="text-lg font-bold text-slate-900 dark:text-white">{busqueda.data.name}</h2>
                  <p className="mt-1 font-mono text-sm text-slate-500 dark:text-slate-400">
                    {busqueda.data.eventCode}
                  </p>
                </div>
                <BadgeEstadoEvento status={busqueda.data.status} />
              </div>
              <div className="mt-4">
                <Link to={RUTAS.adminEvento(busqueda.data.eventCode)}>
                  <Boton variante="secundario">Gestionar evento</Boton>
                </Link>
              </div>
            </div>
          ) : null}
        </div>
      )}

      <section className="mt-10">
        <h2 className="text-xl font-bold text-slate-900 dark:text-white">Creados en esta sesión</h2>
        {creados.length === 0 ? (
          <p className="mt-3 text-sm text-slate-500 dark:text-slate-400">
            Aún no has creado eventos en esta sesión.
          </p>
        ) : (
          <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {creados.map((evento) => (
              <Link
                key={evento.eventCode}
                to={RUTAS.adminEvento(evento.eventCode)}
                className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-marca-300 hover:shadow-md dark:border-slate-800 dark:bg-slate-900 dark:hover:border-marca-700"
              >
                <div className="flex items-start justify-between gap-2">
                  <h3 className="font-bold text-slate-900 dark:text-white">{evento.name}</h3>
                  <BadgeEstadoEvento status={evento.status as EventStatus} />
                </div>
                <p className="mt-1 font-mono text-xs text-slate-500 dark:text-slate-400">
                  {evento.eventCode}
                </p>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}