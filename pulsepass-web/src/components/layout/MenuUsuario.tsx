import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router'
import { useAuth } from '../../features/auth/AuthContext'
import { iniciales } from '../../lib/formato'
import { RUTAS } from '../../lib/rutas'
import { IconoChevronAbajo, IconoSalir, IconoUsuario } from '../ui/Iconos'

export function MenuUsuario() {
  const { sesion, cerrarSesion } = useAuth()
  const [abierto, setAbierto] = useState(false)
  const contenedorRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!abierto) return

    function alHacerClic(evento: MouseEvent) {
      if (contenedorRef.current && !contenedorRef.current.contains(evento.target as Node)) {
        setAbierto(false)
      }
    }

    function alPresionarTecla(evento: KeyboardEvent) {
      if (evento.key === 'Escape') setAbierto(false)
    }

    document.addEventListener('mousedown', alHacerClic)
    document.addEventListener('keydown', alPresionarTecla)
    return () => {
      document.removeEventListener('mousedown', alHacerClic)
      document.removeEventListener('keydown', alPresionarTecla)
    }
  }, [abierto])

  if (sesion.estado !== 'autenticado') return null

  const { usuario } = sesion
  const nombre = usuario.username

  return (
    <div ref={contenedorRef} className="relative">
      <button
        type="button"
        onClick={() => setAbierto((actual) => !actual)}
        aria-haspopup="menu"
        aria-expanded={abierto}
        className="flex items-center gap-2 rounded-full border border-slate-200 bg-white py-1 pr-2 pl-1 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200 dark:hover:bg-slate-800"
      >
        <span className="flex size-8 items-center justify-center rounded-full bg-marca-100 text-xs font-bold text-marca-700 dark:bg-marca-900/60 dark:text-marca-200">
          {iniciales(nombre)}
        </span>
        <span className="hidden max-w-24 truncate sm:inline">{nombre}</span>
        <IconoChevronAbajo className="size-4 text-slate-400" />
      </button>

      {abierto && (
        <div
          role="menu"
          aria-label="Menú de usuario"
          className="absolute right-0 z-50 mt-2 w-64 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xl dark:border-slate-700 dark:bg-slate-900"
        >
          <div className="border-b border-slate-100 px-4 py-3 dark:border-slate-800">
            <p className="truncate text-sm font-semibold text-slate-900 dark:text-white">{nombre}</p>
            <p className="truncate text-xs text-slate-500 dark:text-slate-400">{usuario.email}</p>
          </div>
          <Link
            to={RUTAS.cuenta}
            role="menuitem"
            onClick={() => setAbierto(false)}
            className="flex items-center gap-2 px-4 py-3 text-sm text-slate-700 transition hover:bg-slate-50 dark:text-slate-200 dark:hover:bg-slate-800"
          >
            <IconoUsuario className="size-4" />
            Mi cuenta
          </Link>
          <button
            type="button"
            role="menuitem"
            onClick={() => {
              setAbierto(false)
              cerrarSesion('manual')
            }}
            className="flex w-full items-center gap-2 border-t border-slate-100 px-4 py-3 text-left text-sm text-peligro-700 transition hover:bg-peligro-50 dark:border-slate-800 dark:text-peligro-100 dark:hover:bg-peligro-700/20"
          >
            <IconoSalir className="size-4" />
            Cerrar sesión
          </button>
        </div>
      )}
    </div>
  )
}
