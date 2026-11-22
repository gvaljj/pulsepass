import { Link, Navigate, useParams } from 'react-router'
import { esApiError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useSede } from './hooks'
import { Badge } from '../../components/ui/Badge'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { IconoChevronIzquierda, IconoUbicacion, IconoUsuario } from '../../components/ui/Iconos'

const formatoCapacidad = new Intl.NumberFormat('es-CO')

export default function PaginaDetalleSede() {
  const { code } = useParams<{ code: string }>()
  const codigo = code?.trim() ?? ''
  const sedeQuery = useSede(codigo || null)
  const sede = sedeQuery.data ?? null

  if (!codigo) return <Navigate to={RUTAS.noEncontrada} replace />

  if (sedeQuery.isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <Cargando filas={3} mensaje="Cargando sede…" />
      </div>
    )
  }

  if (sedeQuery.isError) {
    if (esApiError(sedeQuery.error) && sedeQuery.error.status === 404) {
      return <Navigate to={RUTAS.noEncontrada} replace />
    }
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <EstadoError
          mensaje="No pudimos cargar la sede."
          onReintentar={() => void sedeQuery.refetch()}
        />
      </div>
    )
  }

  if (!sede) return <Navigate to={RUTAS.noEncontrada} replace />

  return (
    <div className="mx-auto max-w-4xl px-4 py-12 sm:px-6 lg:px-8">
      <Link
        to={RUTAS.sedes}
        className="inline-flex items-center gap-1 text-sm font-semibold text-slate-500 hover:text-marca-700 dark:text-slate-400 dark:hover:text-marca-300"
      >
        <IconoChevronIzquierda className="size-4" />
        Sedes
      </Link>

      <section className="mt-5 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="bg-gradient-to-br from-marca-700 via-marca-600 to-acento-500 px-6 py-10 text-white">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-bold tracking-widest text-marca-100 uppercase">Sede</p>
              <h1 className="mt-2 text-3xl font-extrabold tracking-tight sm:text-4xl">{sede.name}</h1>
            </div>
            <Badge tono={sede.active ? 'exito' : 'neutral'}>
              {sede.active ? 'Activa' : 'Inactiva'}
            </Badge>
          </div>
        </div>

        <dl className="grid gap-6 p-6 sm:grid-cols-2">
          <div className="flex items-start gap-3">
            <span className="rounded-xl bg-marca-50 p-2 text-marca-600 dark:bg-marca-900/40 dark:text-marca-300">
              <IconoUbicacion className="size-5" />
            </span>
            <div>
              <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                Ciudad
              </dt>
              <dd className="mt-1 font-semibold text-slate-900 dark:text-white">{sede.city}</dd>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <span className="rounded-xl bg-marca-50 p-2 text-marca-600 dark:bg-marca-900/40 dark:text-marca-300">
              <IconoUsuario className="size-5" />
            </span>
            <div>
              <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                Capacidad
              </dt>
              <dd className="mt-1 font-semibold text-slate-900 dark:text-white">
                {formatoCapacidad.format(sede.capacity)} personas
              </dd>
            </div>
          </div>

          <div className="sm:col-span-2">
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Dirección
            </dt>
            <dd className="mt-1 text-slate-700 dark:text-slate-200">{sede.address}</dd>
          </div>

          <div className="sm:col-span-2">
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Código
            </dt>
            <dd className="mt-1 font-mono text-sm text-slate-700 dark:text-slate-200">{sede.code}</dd>
          </div>
        </dl>
      </section>
    </div>
  )
}