import { clsx } from 'clsx'
import { useId, type ComponentProps } from 'react'
import { IconoChevronAbajo } from './Iconos'

export interface OpcionSelector {
  valor: string
  etiqueta: string
}

export interface SelectorProps extends Omit<ComponentProps<'select'>, 'children'> {
  etiqueta: string
  opciones: OpcionSelector[]
  placeholder?: string
  error?: string
  ayuda?: string
}

export function Selector({
  etiqueta,
  opciones,
  placeholder,
  error,
  ayuda,
  id,
  className,
  'aria-describedby': ariaDescribedBy,
  ...props
}: SelectorProps) {
  const idGenerado = useId()
  const selectId = id ?? idGenerado
  const ayudaId = `${selectId}-ayuda`
  const errorId = `${selectId}-error`
  const descripcion = [ariaDescribedBy, ayuda ? ayudaId : null, error ? errorId : null]
    .filter(Boolean)
    .join(' ')

  return (
    <div className={className}>
      <label htmlFor={selectId} className="text-sm font-medium text-slate-700 dark:text-slate-200">
        {etiqueta}
      </label>
      <div className="relative mt-1.5">
        <select
          id={selectId}
          aria-invalid={error ? true : undefined}
          aria-describedby={descripcion || undefined}
          className={clsx(
            'w-full appearance-none rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 pr-10 text-sm text-slate-900 shadow-sm focus:border-marca-500 focus:ring-2 focus:ring-marca-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100',
            error && 'border-peligro-500',
          )}
          {...props}
        >
          {placeholder && <option value="">{placeholder}</option>}
          {opciones.map((opcion) => (
            <option key={opcion.valor} value={opcion.valor}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
        <IconoChevronAbajo
          className="pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-slate-400"
          aria-hidden="true"
        />
      </div>
      {ayuda && (
        <p id={ayudaId} className="mt-1 text-xs text-slate-500 dark:text-slate-400">
          {ayuda}
        </p>
      )}
      {error && (
        <p id={errorId} className="mt-1 text-sm text-peligro-700 dark:text-peligro-100">
          {error}
        </p>
      )}
    </div>
  )
}
