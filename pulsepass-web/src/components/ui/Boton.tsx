import { clsx } from 'clsx'
import type { ComponentProps, ReactNode } from 'react'

type Variante = 'primario' | 'secundario' | 'fantasma' | 'peligro' | 'exito'
type Tamano = 'sm' | 'md' | 'lg'

const variantes: Record<Variante, string> = {
  primario:
    'bg-marca-600 text-white shadow-sm shadow-marca-600/20 hover:bg-marca-700 active:bg-marca-800',
  secundario:
    'border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200 dark:hover:bg-slate-800',
  fantasma:
    'text-marca-700 hover:bg-marca-50 dark:text-marca-300 dark:hover:bg-marca-900/40',
  peligro: 'bg-peligro-600 text-white hover:bg-peligro-700 active:bg-peligro-800',
  exito: 'bg-exito-700 text-white hover:bg-exito-800 active:bg-exito-800',
}

const tamanos: Record<Tamano, string> = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2.5 text-sm',
  lg: 'px-5 py-3 text-base',
}

export interface BotonProps extends ComponentProps<'button'> {
  variante?: Variante
  tamano?: Tamano
  cargando?: boolean
  children: ReactNode
}

export function Boton({
  variante = 'primario',
  tamano = 'md',
  cargando = false,
  disabled,
  className,
  children,
  type = 'button',
  ...props
}: BotonProps) {
  return (
    <button
      type={type}
      disabled={disabled || cargando}
      aria-busy={cargando || undefined}
      className={clsx(
        'inline-flex items-center justify-center gap-2 rounded-xl font-semibold transition disabled:cursor-not-allowed disabled:opacity-60',
        variantes[variante],
        tamanos[tamano],
        className,
      )}
      {...props}
    >
      {cargando && (
        <span
          className="size-4 animate-spin rounded-full border-2 border-current border-t-transparent"
          aria-hidden="true"
        />
      )}
      {children}
    </button>
  )
}
