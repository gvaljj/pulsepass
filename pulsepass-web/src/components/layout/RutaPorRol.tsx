import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../../features/auth/AuthContext'
import type { Rol } from '../../features/auth/tipos'
import { RUTAS } from '../../lib/rutas'
import { CargandoPagina } from './CargandoPagina'

interface RutaPorRolProps {
  roles: Rol[]
}

export function RutaPorRol({ roles }: RutaPorRolProps) {
  const { sesion, tieneRol } = useAuth()
  const location = useLocation()

  if (sesion.estado === 'cargando') return <CargandoPagina />
  if (sesion.estado === 'anonimo') {
    return (
      <Navigate
        to={RUTAS.login}
        replace
        state={{ desde: `${location.pathname}${location.search}` }}
      />
    )
  }
  if (!tieneRol(...roles)) return <Navigate to={RUTAS.sinPermisos} replace />

  return <Outlet />
}
