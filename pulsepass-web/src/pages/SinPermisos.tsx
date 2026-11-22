import { Link } from 'react-router'
import { Logo } from '../components/layout/Logo'
import { IconoAlerta } from '../components/ui/Iconos'
import { RUTAS } from '../lib/rutas'

export default function SinPermisos() {
  return (
    <div className="mx-auto flex max-w-3xl flex-col items-center px-4 py-20 text-center">
      <Logo />
      <div className="mt-10 w-full rounded-2xl border border-aviso-100 bg-white p-8 shadow-sm dark:border-aviso-700/40 dark:bg-slate-900">
        <div className="mx-auto flex size-14 items-center justify-center rounded-full bg-aviso-50 text-aviso-700 dark:bg-aviso-700/20 dark:text-aviso-100">
          <IconoAlerta className="size-7" />
        </div>
        <p className="mt-4 text-sm font-bold tracking-widest text-aviso-700 uppercase dark:text-aviso-100">
          Error 403
        </p>
        <h1 className="mt-2 text-2xl font-bold text-slate-900 dark:text-white">
          No tienes permisos para acceder a esta sección.
        </h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          Si crees que es un error, inicia sesión con una cuenta autorizada.
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
