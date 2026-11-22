import { clsx } from 'clsx'
import { useId, type ComponentProps, type ReactNode } from 'react'

export interface CampoProps extends ComponentProps<'input'> {
  etiqueta: string
  error?: string
  ayuda?: string
  sufijo?: ReactNode
}

const clasesInput =
  'mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm placeholder:text-slate-400 focus:border-marca-500 focus:ring-2 focus:ring-marca-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:placeholder:text-slate-500'

export function Campo({
  etiqueta,
  error,
  ayuda,
  id,
  className,
  sufijo,
  'aria-describedby': ariaDescribedBy,
  ...props
}: CampoProps) {
  const idGenerado = useId()
  const inputId = id ?? idGenerado
  const ayudaId = `${inputId}-ayuda`
  const errorId = `${inputId}-error`
  const descripcion = [ariaDescribedBy, ayuda ? ayudaId : null, error ? errorId : null]
    .filter(Boolean)
    .join(' ')

  return (
    <div className={className}>
      <label htmlFor={inputId} className="text-sm font-medium text-slate-700 dark:text-slate-200">
        {etiqueta}
      </label>
      <div className="relative">
        <input
          id={inputId}
          aria-invalid={error ? true : undefined}
          aria-describedby={descripcion || undefined}
          className={clsx(clasesInput, sufijo && 'pr-10', error && 'border-peligro-500 focus:border-peligro-500')}
          {...props}
        />
        {sufijo && (
          <span className="absolute top-1/2 right-3 flex -translate-y-1/2 items-center">
            {sufijo}
          </span>
        )}
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

export interface CampoTextoLargoProps extends ComponentProps<'textarea'> {
  etiqueta: string
  error?: string
  ayuda?: string
}

export function CampoTextoLargo({
  etiqueta,
  error,
  ayuda,
  id,
  className,
  'aria-describedby': ariaDescribedBy,
  ...props
}: CampoTextoLargoProps) {
  const idGenerado = useId()
  const inputId = id ?? idGenerado
  const ayudaId = `${inputId}-ayuda`
  const errorId = `${inputId}-error`
  const descripcion = [ariaDescribedBy, ayuda ? ayudaId : null, error ? errorId : null]
    .filter(Boolean)
    .join(' ')

  return (
    <div className={className}>
      <label htmlFor={inputId} className="text-sm font-medium text-slate-700 dark:text-slate-200">
        {etiqueta}
      </label>
      <textarea
        id={inputId}
        aria-invalid={error ? true : undefined}
        aria-describedby={descripcion || undefined}
        className={clsx(clasesInput, 'min-h-28 resize-y', error && 'border-peligro-500')}
        {...props}
      />
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
