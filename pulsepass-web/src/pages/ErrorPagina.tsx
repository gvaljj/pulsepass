import { isRouteErrorResponse, Link, useRouteError } from 'react-router'
import { Logo } from '../components/layout/Logo'
import { Boton } from '../components/ui/Boton'
import { IconoAlerta } from '../components/ui/Iconos'
import { RUTAS } from '../lib/rutas'

export default function ErrorPagina() {
  const error = useRouteError()

  let mensaje = 'Ocurrió un error inesperado. Inténtalo más tarde.'
  if (isRouteErrorResponse(error)) {
    if (error.status === 404) mensaje = 'No encontramos esta página.'
    else if (error.status === 403) mensaje = 'No tienes permisos para acceder a esta sección.'
    else mensaje = 'No pudimos cargar esta página. Inténtalo más tarde.'
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-slate-50 px-4 py-16 dark:bg-slate-950">
      <Logo />
      <section
        role="alert"
        className="mt-10 w-full max-w-xl rounded-2xl border border-peligro-100 bg-white p-8 text-center shadow-xl dark:border-peligro-700/40 dark:bg-slate-900"
      >
        <div className="mx-auto flex size-14 items-center justify-center rounded-full bg-peligro-50 text-peligro-700 dark:bg-peligro-700/20 dark:text-peligro-100">
          <IconoAlerta className="size-7" />
        </div>
        <h1 className="mt-4 text-2xl font-bold text-slate-900 dark:text-white">¡Ups! Algo salió mal.</h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">{mensaje}</p>
        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <Boton onClick={() => window.location.reload()}>Reintentar</Boton>
          <Link
            to={RUTAS.inicio}
            className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200 dark:hover:bg-slate-800"
          >
            Volver al inicio
          </Link>
        </div>
      </section>
    </main>
  )
}
