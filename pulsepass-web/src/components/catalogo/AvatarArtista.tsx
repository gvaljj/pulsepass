import { clsx } from 'clsx'
import { iniciales } from '../../lib/formato'

type TamanoAvatar = 'sm' | 'md' | 'lg' | 'xl'

const tamanos: Record<TamanoAvatar, string> = {
  sm: 'size-9 text-xs',
  md: 'size-12 text-sm',
  lg: 'size-20 text-xl',
  xl: 'size-28 text-3xl',
}

interface AvatarArtistaProps {
  nombre: string
  tamano?: TamanoAvatar
  className?: string
}

export function AvatarArtista({ nombre, tamano = 'md', className }: AvatarArtistaProps) {
  return (
    <span
      aria-hidden="true"
      className={clsx(
        'inline-flex shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-marca-100 to-acento-300 font-titulo font-extrabold text-marca-800 shadow-inner dark:from-marca-800 dark:to-acento-700 dark:text-white',
        tamanos[tamano],
        className,
      )}
    >
      {iniciales(nombre)}
    </span>
  )
}
