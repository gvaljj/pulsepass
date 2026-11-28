import { useSedesActivas } from './hooks'
import { TarjetaSede } from '../../components/catalogo/TarjetaSede'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'

export default function PaginaListaSedes() {
  const sedesQuery = useSedesActivas()
  const sedes = sedesQuery.data ?? []

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <header className="max-w-2xl">
        <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
          Espacios
        </p>
        <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl dark:text-white">
          Sedes
        </h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          Descubre los espacios donde vive PulsePass.
        </p>
      </header>

      <div className="mt-8">
        {sedesQuery.isLoading ? (
          <Cargando filas={6} mensaje="Cargando sedes…" />
        ) : sedesQuery.isError ? (
          <EstadoError
            mensaje="No pudimos cargar las sedes."
            onReintentar={() => void sedesQuery.refetch()}
          />
        ) : sedes.length === 0 ? (
          <EstadoVacio
            titulo="Aún no hay sedes activas"
            mensaje="Las sedes disponibles aparecerán pronto."
          />
        ) : (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {sedes.map((sede) => (
              <TarjetaSede key={sede.code} sede={sede} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}