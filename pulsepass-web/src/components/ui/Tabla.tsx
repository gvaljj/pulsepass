import type { ReactNode } from 'react'

export interface ColumnaTabla<T> {
  clave: string
  encabezado: string
  render: (fila: T) => ReactNode
  className?: string
  alineacion?: 'izquierda' | 'centro' | 'derecha'
}

export interface TablaProps<T> {
  columnas: ColumnaTabla<T>[]
  filas: T[]
  claveFila: (fila: T) => string | number
  etiqueta: string
  className?: string
}

const alineaciones = {
  izquierda: 'text-left',
  centro: 'text-center',
  derecha: 'text-right',
}

export function Tabla<T>({ columnas, filas, claveFila, etiqueta, className }: TablaProps<T>) {
  return (
    <div className={`overflow-x-auto ${className ?? ''}`}>
      <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800">
        <caption className="sr-only">{etiqueta}</caption>
        <thead>
          <tr>
            {columnas.map((columna) => (
              <th
                key={columna.clave}
                scope="col"
                className={`px-4 py-3 text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400 ${
                  alineaciones[columna.alineacion ?? 'izquierda']
                } ${columna.className ?? ''}`}
              >
                {columna.encabezado}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
          {filas.map((fila) => (
            <tr key={claveFila(fila)} className="hover:bg-slate-50 dark:hover:bg-slate-900/60">
              {columnas.map((columna) => (
                <td
                  key={columna.clave}
                  className={`px-4 py-4 text-sm text-slate-700 dark:text-slate-200 ${
                    alineaciones[columna.alineacion ?? 'izquierda']
                  } ${columna.className ?? ''}`}
                >
                  {columna.render(fila)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
