import type { ReactNode } from 'react'
import { IconoBuscar } from './Iconos'

export interface EstadoVacioProps {
  titulo: string
  mensaje: string
  accion?: ReactNode
  icono?: ReactNode
}

export function EstadoVacio({ titulo, mensaje, accion, icono }: EstadoVacioProps) {
  return (
    <div
      role="status"
      className="flex flex-col items-center rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-12 text-center dark:border-slate-700 dark:bg-slate-900"
    >
      <div className="rounded-full bg-marca-50 p-4 text-marca-600 dark:bg-marca-900/40 dark:text-marca-300">
        {icono ?? <IconoBuscar className="size-7" />}
      </div>
      <h2 className="mt-4 text-lg font-bold text-slate-900 dark:text-white">{titulo}</h2>
      <p className="mt-1 max-w-md text-sm text-slate-500 dark:text-slate-400">{mensaje}</p>
      {accion && <div className="mt-5">{accion}</div>}
    </div>
  )
}
