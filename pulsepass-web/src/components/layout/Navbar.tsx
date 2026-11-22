import { useState } from 'react'
import { Link, NavLink } from 'react-router'
import { useAuth } from '../../features/auth/AuthContext'
import { RUTAS } from '../../lib/rutas'
import { IconoCerrar, IconoMenu } from '../ui/Iconos'
import { BotonTema } from './BotonTema'
import { Logo } from './Logo'
import { MenuUsuario } from './MenuUsuario'
import { NavbarMovil } from './NavbarMovil'
import { ENLACES_PRINCIPALES, enlacesDeSesion } from './enlaces'

const claseEnlace = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-2 text-sm font-semibold transition ${
    isActive
      ? 'bg-marca-50 text-marca-700 dark:bg-marca-900/40 dark:text-marca-200'
      : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-white'
  }`

export function Navbar() {
  const { sesion } = useAuth()
  const [menuAbierto, setMenuAbierto] = useState(false)
  const enlaces = [...ENLACES_PRINCIPALES, ...enlacesDeSesion(sesion)]

  return (
    <header className="relative z-40 border-b border-slate-200 bg-white/90 backdrop-blur dark:border-slate-800 dark:bg-slate-950/90">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-4 sm:px-6 lg:px-8">
        <Logo />

        <nav aria-label="Navegación principal" className="hidden items-center gap-1 lg:flex">
          {enlaces.map((enlace) => (
            <NavLink key={enlace.to} to={enlace.to} end={enlace.end} className={claseEnlace}>
              {enlace.etiqueta}
            </NavLink>
          ))}
        </nav>

        <div className="hidden items-center gap-2 lg:flex">
          <BotonTema />
          {sesion.estado === 'autenticado' ? (
            <MenuUsuario />
          ) : (
            <>
              <Link
                to={RUTAS.login}
                className="rounded-xl px-3 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 dark:text-slate-200 dark:hover:bg-slate-800"
              >
                Iniciar sesión
              </Link>
              <Link
                to={RUTAS.registro}
                className="rounded-xl bg-marca-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-marca-700"
              >
                Crear cuenta
              </Link>
            </>
          )}
        </div>

        <div className="flex items-center gap-1 lg:hidden">
          <BotonTema />
          <button
            type="button"
            onClick={() => setMenuAbierto((actual) => !actual)}
            aria-expanded={menuAbierto}
            aria-controls="menu-movil"
            aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
            className="rounded-full p-2 text-slate-600 transition hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
          >
            {menuAbierto ? <IconoCerrar className="size-6" /> : <IconoMenu className="size-6" />}
          </button>
        </div>
      </div>

      {menuAbierto && <NavbarMovil onCerrar={() => setMenuAbierto(false)} />}
    </header>
  )
}
