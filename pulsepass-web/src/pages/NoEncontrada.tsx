import { Link } from 'react-router'
import { Logo } from '../components/layout/Logo'
import { IconoBuscar } from '../components/ui/Iconos'
import { RUTAS } from '../lib/rutas'

export default function NoEncontrada() {
  return (
    <div className="mx-auto flex max-w-3xl flex-col items-center px-4 py-20 text-center">
      <Logo />
      <div className="mt-10 w-full rounded-2xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="mx-auto flex size-14 items-center justify-center rounded-full bg-marca-50 text-marca-600 dark:bg-marca-900/40 dark:text-marca-200">
          <IconoBuscar className="size-7" />
        </div>
        <p className="mt-4 text-sm font-bold tracking-widest text-marca-700 uppercase dark:text-marca-300">
          Error 404
        </p>
        <h1 className="mt-2 text-2xl font-bold text-slate-900 dark:text-white">
          No encontramos esta página.
        </h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          La dirección puede estar mal escrita o el contenido ya no está disponible.
        </p>
        <Link
          to={RUTAS.inicio}
          className="mt-6 inline-flex items-center justify-center rounded-xl bg-marca-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-marca-700"
        >
          Volver al inicio
        </Link>
      </div>
    </div>
  )
}
