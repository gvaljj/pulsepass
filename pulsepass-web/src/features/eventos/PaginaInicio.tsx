import { useMemo } from 'react'
import { Link } from 'react-router'
import { useAuth } from '../auth/AuthContext'
import { useArtistasActivos } from '../artistas/hooks'
import { useEventosPublicados } from '../eventos/hooks'
import { useSedesActivas } from '../sedes/hooks'
import { RUTAS } from '../../lib/rutas'
import { AvatarArtista } from '../../components/catalogo/AvatarArtista'
import { GrillaEventos } from '../../components/catalogo/GrillaEventos'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'
import { IconoChevronDerecha, IconoMusica } from '../../components/ui/Iconos'

export default function PaginaInicio() {
  const { sesion } = useAuth()
  const eventosQuery = useEventosPublicados()
  const artistasQuery = useArtistasActivos()
  const sedesQuery = useSedesActivas()

  const sedesPorCodigo = useMemo(
    () => new Map((sedesQuery.data ?? []).map((sede) => [sede.code, sede])),
    [sedesQuery.data],
  )

  const eventos = (eventosQuery.data ?? []).slice(0, 3)
  const artistas = (artistasQuery.data ?? []).slice(0, 6)

  return (
    <>
      <section className="relative overflow-hidden bg-gradient-to-br from-marca-800 via-marca-700 to-acento-600 text-white">
        <div className="absolute -top-24 -right-20 size-80 rounded-full bg-white/10" />
        <div className="absolute -bottom-28 -left-16 size-80 rounded-full bg-black/10" />
        <div className="relative mx-auto max-w-7xl px-4 py-20 sm:px-6 sm:py-24 lg:px-8">
          <div className="max-w-2xl">
            <span className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3 py-1 text-xs font-bold tracking-wide uppercase">
              <IconoMusica className="size-4" />
              Vive la experiencia
            </span>
            <h1 className="mt-5 text-4xl font-extrabold tracking-tight sm:text-5xl lg:text-6xl">
              Descubre los mejores eventos
            </h1>
            <p className="mt-5 max-w-xl text-base leading-7 text-marca-100 sm:text-lg">
              Música, cultura, deporte y tecnología en un solo lugar.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link
                to={RUTAS.eventos}
                className="inline-flex items-center gap-2 rounded-xl bg-white px-5 py-3 text-sm font-bold text-marca-800 shadow-lg transition hover:bg-marca-50"
              >
                Ver cartelera
                <IconoChevronDerecha className="size-4" />
              </Link>
              {sesion.estado !== 'autenticado' && (
                <Link
                  to={RUTAS.registro}
                  className="inline-flex items-center gap-2 rounded-xl border border-white/40 bg-white/10 px-5 py-3 text-sm font-bold text-white transition hover:bg-white/20"
                >
                  Crear cuenta
                </Link>
              )}
            </div>
          </div>
        </div>
      </section>

      <div className="mx-auto max-w-7xl space-y-14 px-4 py-14 sm:px-6 lg:px-8">
        <section aria-labelledby="proximos-eventos">
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div>
              <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
                Agenda
              </p>
              <h2
                id="proximos-eventos"
                className="mt-1 text-2xl font-extrabold tracking-tight text-slate-900 sm:text-3xl dark:text-white"
              >
                Próximos eventos
              </h2>
            </div>
            <Link
              to={RUTAS.eventos}
              className="inline-flex items-center gap-1 text-sm font-bold text-marca-700 hover:text-marca-800 dark:text-marca-300 dark:hover:text-marca-200"
            >
              Ver todos
              <IconoChevronDerecha className="size-4" />
            </Link>
          </div>

          <div className="mt-6">
            {eventosQuery.isLoading ? (
              <Cargando filas={3} mensaje="Cargando próximos eventos…" />
            ) : eventosQuery.isError ? (
              <EstadoError
                mensaje="No pudimos cargar los eventos. Revisa tu conexión e inténtalo de nuevo."
                onReintentar={() => void eventosQuery.refetch()}
              />
            ) : eventos.length === 0 ? (
              <EstadoVacio
                titulo="Aún no hay eventos publicados"
                mensaje="Vuelve pronto para descubrir nuevas experiencias."
              />
            ) : (
              <GrillaEventos eventos={eventos} sedesPorCodigo={sedesPorCodigo} />
            )}
          </div>
        </section>

        <section aria-labelledby="artistas-destacados">
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div>
              <p className="text-xs font-bold tracking-widest text-acento-600 uppercase dark:text-acento-300">
                Talento
              </p>
              <h2
                id="artistas-destacados"
                className="mt-1 text-2xl font-extrabold tracking-tight text-slate-900 sm:text-3xl dark:text-white"
              >
                Artistas destacados
              </h2>
            </div>
            <Link
              to={RUTAS.artistas}
              className="inline-flex items-center gap-1 text-sm font-bold text-marca-700 hover:text-marca-800 dark:text-marca-300 dark:hover:text-marca-200"
            >
              Ver todos
              <IconoChevronDerecha className="size-4" />
            </Link>
          </div>

          <div className="mt-6">
            {artistasQuery.isLoading ? (
              <Cargando filas={3} mensaje="Cargando artistas destacados…" />
            ) : artistasQuery.isError ? (
              <EstadoError
                mensaje="No pudimos cargar los artistas."
                onReintentar={() => void artistasQuery.refetch()}
              />
            ) : artistas.length === 0 ? (
              <EstadoVacio
                titulo="Aún no hay artistas activos"
                mensaje="La agenda de artistas se está preparando."
              />
            ) : (
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {artistas.map((artista) => (
                  <Link
                    key={artista.id}
                    to={RUTAS.artista(artista.id)}
                    className="flex items-center gap-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md dark:border-slate-800 dark:bg-slate-900"
                  >
                    <AvatarArtista nombre={artista.stageName} tamano="lg" />
                    <span>
                      <span className="block font-titulo text-lg font-bold text-slate-900 dark:text-white">
                        {artista.stageName}
                      </span>
                      <span className="block text-sm text-slate-500 dark:text-slate-400">
                        {artista.genre}
                      </span>
                    </span>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </section>
      </div>
    </>
  )
}
