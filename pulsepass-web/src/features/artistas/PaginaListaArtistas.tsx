import { Link } from 'react-router'
import { useArtistasActivos } from '../artistas/hooks'
import { AvatarArtista } from '../../components/catalogo/AvatarArtista'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'
import { IconoChevronDerecha } from '../../components/ui/Iconos'
import { RUTAS } from '../../lib/rutas'

export default function PaginaListaArtistas() {
  const artistasQuery = useArtistasActivos()
  const artistas = artistasQuery.data ?? []

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <header className="max-w-2xl">
        <p className="text-xs font-bold tracking-widest text-acento-600 uppercase dark:text-acento-300">
          Talento
        </p>
        <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl dark:text-white">
          Artistas
        </h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          Conoce a los artistas que están dando de qué hablar.
        </p>
      </header>

      <div className="mt-8">
        {artistasQuery.isLoading ? (
          <Cargando filas={6} mensaje="Cargando artistas…" />
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
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {artistas.map((artista) => (
              <Link
                key={artista.id}
                to={RUTAS.artista(artista.id)}
                className="group flex flex-col items-center rounded-2xl border border-slate-200 bg-white p-6 text-center shadow-sm transition hover:-translate-y-0.5 hover:border-marca-300 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900 dark:hover:border-marca-700"
              >
                <AvatarArtista nombre={artista.stageName} tamano="xl" />
                <h2 className="mt-4 text-xl font-bold text-slate-900 group-hover:text-marca-700 dark:text-white dark:group-hover:text-marca-300">
                  {artista.stageName}
                </h2>
                <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{artista.genre}</p>
                <span className="mt-4 inline-flex items-center gap-1 text-sm font-bold text-marca-700 dark:text-marca-300">
                  Ver eventos
                  <IconoChevronDerecha className="size-4" />
                </span>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
