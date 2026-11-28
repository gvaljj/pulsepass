import { Link } from 'react-router'
import { RUTAS } from '../../lib/rutas'
import { IconoMusica } from '../ui/Iconos'

export function Logo() {
  return (
    <Link
      to={RUTAS.inicio}
      className="inline-flex items-center gap-2 rounded-lg text-slate-900 dark:text-white"
      aria-label="PulsePass, ir al inicio"
    >
      <span className="flex size-9 items-center justify-center rounded-xl bg-gradient-to-br from-marca-600 to-acento-500 text-white shadow-sm shadow-marca-600/30">
        <IconoMusica className="size-5" />
      </span>
      <span className="font-titulo text-xl font-extrabold tracking-tight">PulsePass</span>
    </Link>
  )
}
