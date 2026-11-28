import { useMemo } from 'react'
import { Link, Navigate, useParams } from 'react-router'
import { esApiError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useArtista } from './hooks'
import { useEventosPorArtista } from '../eventos/hooks'
import { useSedesActivas } from '../sedes/hooks'
import { AvatarArtista } from '../../components/catalogo/AvatarArtista'
import { GrillaEventos } from '../../components/catalogo/GrillaEventos'
import { Badge } from '../../components/ui/Badge'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'
import { IconoChevronIzquierda } from '../../components/ui/Iconos'

export default function PaginaDetalleArtista() {
  const { id } = useParams<{ id: string }>()
  const idNumerico = Number(id)
  const idValido = Number.isInteger(idNumerico) && idNumerico > 0

  const artistaQuery = useArtista(idValido ? idNumerico : null)
  const artista = artistaQuery.data ?? null
  const eventosQuery = useEventosPorArtista(artista?.stageName ?? null)
  const sedesQuery = useSedesActivas()

  const sedesPorCodigo = useMemo(
    () => new Map((sedesQuery.data ?? []).map((sede) => [sede.code, sede])),
    [sedesQuery.data],
  )

  if (!idValido) return <Navigate to={RUTAS.noEncontrada} replace />

  if (artistaQuery.isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <Cargando filas={3} mensaje="Cargando artista…" />
      </div>
    )
  }

  if (artistaQuery.isError) {
    if (esApiError(artistaQuery.error) && artistaQuery.error.status === 404) {
      return <Navigate to={RUTAS.noEncontrada} replace />
    }
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <EstadoError
          mensaje="No pudimos cargar el artista."
          onReintentar={() => void artistaQuery.refetch()}
        />
      </div>
    )
  }

  if (!artista) return <Navigate to={RUTAS.noEncontrada} replace />

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <Link
        to={RUTAS.artistas}
        className="inline-flex items-center gap-1 text-sm font-semibold text-slate-500 hover:text-marca-700 dark:text-slate-400 dark:hover:text-marca-300"
      >
        <IconoChevronIzquierda className="size-4" />
        Artistas
      </Link>

      <section className="mt-5 flex flex-col items-center gap-6 rounded-2xl border border-slate-200 bg-white p-6 text-center shadow-sm sm:flex-row sm:text-left dark:border-slate-800 dark:bg-slate-900">
        <AvatarArtista nombre={artista.stageName} tamano="xl" />
        <div>
          <div className="flex flex-wrap justify-center gap-2 sm:justify-start">
            <Badge tono="marca">{artista.genre}</Badge>
            <Badge tono={artista.active ? 'exito' : 'neutral'}>
              {artista.active ? 'Activo' : 'Inactivo'}
            </Badge>
          </div>
          <h1 className="mt-3 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
            {artista.stageName}
          </h1>
          <p className="mt-2 max-w-2xl text-sm text-slate-500 dark:text-slate-400">
            Nombre artístico · género {artista.genre}. Próximos eventos y presentaciones en
            PulsePass.
          </p>
        </div>
      </section>

      <section aria-labelledby="eventos-artista" className="mt-10">
        <h2
          id="eventos-artista"
          className="text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white"
        >
          Próximos eventos de {artista.stageName}
        </h2>
        <div className="mt-5">
          {eventosQuery.isLoading ? (
            <Cargando filas={3} mensaje="Cargando eventos del artista…" />
          ) : eventosQuery.isError ? (
            <EstadoError
              mensaje="No pudimos cargar los eventos del artista."
              onReintentar={() => void eventosQuery.refetch()}
            />
          ) : (eventosQuery.data ?? []).length === 0 ? (
            <EstadoVacio
              titulo="Sin eventos próximos"
              mensaje="Este artista todavía no tiene eventos publicados."
              accion={
                <Link to={RUTAS.eventos}>
                  <Boton variante="secundario">Ver cartelera</Boton>
                </Link>
              }
            />
          ) : (
            <GrillaEventos eventos={eventosQuery.data ?? []} sedesPorCodigo={sedesPorCodigo} />
          )}
        </div>
      </section>
    </div>
  )
}
