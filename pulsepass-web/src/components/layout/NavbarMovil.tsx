import { Link, NavLink } from 'react-router'
import { useAuth } from '../../features/auth/AuthContext'
import { RUTAS } from '../../lib/rutas'
import { BotonTema } from './BotonTema'
import { ENLACES_PRINCIPALES, enlacesDeSesion } from './enlaces'

interface NavbarMovilProps {
  onCerrar: () => void
}

const claseEnlace = ({ isActive }: { isActive: boolean }) =>
  `block rounded-lg px-3 py-2.5 text-sm font-semibold transition ${
    isActive
      ? 'bg-marca-50 text-marca-700 dark:bg-marca-900/40 dark:text-marca-200'
      : 'text-slate-700 hover:bg-slate-50 dark:text-slate-200 dark:hover:bg-slate-800'
  }`

export function NavbarMovil({ onCerrar }: NavbarMovilProps) {
  const { sesion, cerrarSesion } = useAuth()
  const enlaces = [...ENLACES_PRINCIPALES, ...enlacesDeSesion(sesion)]

  return (
    <nav
      id="menu-movil"
      aria-label="Navegación móvil"
      className="absolute top-full right-0 left-0 border-b border-slate-200 bg-white p-4 shadow-xl lg:hidden dark:border-slate-800 dark:bg-slate-950"
    >
      <div className="flex flex-col gap-1">
        {enlaces.map((enlace) => (
          <NavLink
            key={enlace.to}
            to={enlace.to}
            end={enlace.end}
            onClick={onCerrar}
            className={claseEnlace}
          >
            {enlace.etiqueta}
          </NavLink>
        ))}
      </div>

      <div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-4 dark:border-slate-800">
        <BotonTema />
        {sesion.estado === 'autenticado' ? (
          <div className="flex flex-col items-end gap-2">
            <Link
              to={RUTAS.cuenta}
              onClick={onCerrar}
              className="text-sm font-semibold text-marca-700 dark:text-marca-300"
            >
              Mi cuenta
            </Link>
            <button
              type="button"
              onClick={() => {
                onCerrar()
                cerrarSesion('manual')
              }}
              className="text-sm font-semibold text-peligro-700 dark:text-peligro-100"
            >
              Cerrar sesión
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-end gap-2">
            <Link
              to={RUTAS.login}
              onClick={onCerrar}
              className="text-sm font-semibold text-slate-700 dark:text-slate-200"
            >
              Iniciar sesión
            </Link>
            <Link
              to={RUTAS.registro}
              onClick={onCerrar}
              className="rounded-xl bg-marca-600 px-4 py-2 text-sm font-semibold text-white"
            >
              Crear cuenta
            </Link>
          </div>
        )}
      </div>
    </nav>
  )
}
