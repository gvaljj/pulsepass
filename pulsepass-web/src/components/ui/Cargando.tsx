export interface CargandoProps {
  mensaje?: string
  filas?: number
}

export function Cargando({ mensaje = 'Cargando contenido…', filas = 6 }: CargandoProps) {
  return (
    <div role="status" aria-live="polite" className="space-y-4">
      <span className="sr-only">{mensaje}</span>
      <div aria-hidden="true" className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: filas }).map((_, indice) => (
          <div
            key={indice}
            className="animate-pulse rounded-2xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
          >
            <div className="h-36 rounded-xl bg-slate-200 dark:bg-slate-800" />
            <div className="mt-4 h-5 w-3/4 rounded bg-slate-200 dark:bg-slate-800" />
            <div className="mt-2 h-4 w-1/2 rounded bg-slate-100 dark:bg-slate-800" />
            <div className="mt-4 h-4 w-2/3 rounded bg-slate-100 dark:bg-slate-800" />
          </div>
        ))}
      </div>
    </div>
  )
}
