import { clsx } from 'clsx'
import type { ReactNode } from 'react'

export type TonoBadge = 'neutral' | 'marca' | 'exito' | 'aviso' | 'peligro' | 'info'

const tonos: Record<TonoBadge, string> = {
  neutral:
    'bg-slate-100 text-slate-700 ring-slate-200 dark:bg-slate-800 dark:text-slate-200 dark:ring-slate-700',
  marca:
    'bg-marca-50 text-marca-700 ring-marca-200 dark:bg-marca-900/40 dark:text-marca-200 dark:ring-marca-700',
  exito:
    'bg-exito-50 text-exito-700 ring-exito-100 dark:bg-exito-700/20 dark:text-exito-100 dark:ring-exito-700/40',
  aviso:
    'bg-aviso-50 text-aviso-700 ring-aviso-100 dark:bg-aviso-700/20 dark:text-aviso-100 dark:ring-aviso-700/40',
  peligro:
    'bg-peligro-50 text-peligro-700 ring-peligro-100 dark:bg-peligro-700/20 dark:text-peligro-100 dark:ring-peligro-700/40',
  info:
    'bg-info-50 text-info-700 ring-info-100 dark:bg-info-700/20 dark:text-info-100 dark:ring-info-700/40',
}

export interface BadgeProps {
  tono?: TonoBadge
  children: ReactNode
  className?: string
}

export function Badge({ tono = 'neutral', children, className }: BadgeProps) {
  return (
    <span
      className={clsx(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold tracking-wide uppercase ring-1 ring-inset',
        tonos[tono],
        className,
      )}
    >
      {children}
    </span>
  )
}
