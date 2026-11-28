import { Link } from 'react-router'
import { RUTAS } from '../../lib/rutas'

export function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-950">
      <div className="mx-auto grid max-w-7xl gap-8 px-4 py-10 sm:px-6 md:grid-cols-3 lg:px-8">
        <div>
          <p className="font-titulo text-lg font-extrabold text-slate-900 dark:text-white">
            PulsePass
          </p>
          <p className="mt-2 max-w-xs text-sm text-slate-500 dark:text-slate-400">
            Descubre eventos, artistas y sedes. Compra tus entradas y vive la experiencia.
          </p>
        </div>

        <nav aria-label="Explorar">
          <h2 className="text-sm font-bold tracking-wide text-slate-900 uppercase dark:text-white">
            Explorar
          </h2>
          <ul className="mt-3 space-y-2 text-sm text-slate-500 dark:text-slate-400">
            <li>
              <Link to={RUTAS.eventos} className="hover:text-marca-700 dark:hover:text-marca-300">
                Eventos
              </Link>
            </li>
            <li>
              <Link to={RUTAS.artistas} className="hover:text-marca-700 dark:hover:text-marca-300">
                Artistas
              </Link>
            </li>
            <li>
              <Link to={RUTAS.sedes} className="hover:text-marca-700 dark:hover:text-marca-300">
                Sedes
              </Link>
            </li>
          </ul>
        </nav>

        <nav aria-label="Cuenta">
          <h2 className="text-sm font-bold tracking-wide text-slate-900 uppercase dark:text-white">
            Cuenta
          </h2>
          <ul className="mt-3 space-y-2 text-sm text-slate-500 dark:text-slate-400">
            <li>
              <Link to={RUTAS.misTickets} className="hover:text-marca-700 dark:hover:text-marca-300">
                Mis tickets
              </Link>
            </li>
            <li>
              <Link to={RUTAS.cuenta} className="hover:text-marca-700 dark:hover:text-marca-300">
                Mi cuenta
              </Link>
            </li>
            <li>
              <Link to={RUTAS.login} className="hover:text-marca-700 dark:hover:text-marca-300">
                Iniciar sesión
              </Link>
            </li>
          </ul>
        </nav>
      </div>

      <div className="border-t border-slate-100 py-4 text-center text-xs text-slate-500 dark:border-slate-800 dark:text-slate-400">
        © 2026 PulsePass · Caso de estudio académico
      </div>
    </footer>
  )
}
