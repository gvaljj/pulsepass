import { Link } from 'react-router'
import type { Artist } from '../../features/artistas/tipos'
import { RUTAS } from '../../lib/rutas'
import { AvatarArtista } from './AvatarArtista'

interface ListaArtistasEventoProps {
  artistas: Artist[]
}

export function ListaArtistasEvento({ artistas }: ListaArtistasEventoProps) {
  return (
    <section aria-labelledby="artistas-evento" className="rounded-2xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
      <h2 id="artistas-evento" className="text-lg font-bold text-slate-900 dark:text-white">
        Artistas
      </h2>
      {artistas.length === 0 ? (
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">Artistas por confirmar.</p>
      ) : (
        <ul className="mt-4 flex flex-wrap gap-3">
          {artistas.map((artista) => (
            <li key={artista.id}>
              <Link
                to={RUTAS.artista(artista.id)}
                className="flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 py-1.5 pr-4 pl-1.5 transition hover:border-marca-300 hover:bg-marca-50 dark:border-slate-700 dark:bg-slate-800 dark:hover:border-marca-700 dark:hover:bg-marca-900/30"
              >
                <AvatarArtista nombre={artista.stageName} tamano="sm" />
                <span>
                  <span className="block text-sm font-semibold text-slate-800 dark:text-slate-100">
                    {artista.stageName}
                  </span>
                  <span className="block text-xs text-slate-500 dark:text-slate-400">
                    {artista.genre}
                  </span>
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
