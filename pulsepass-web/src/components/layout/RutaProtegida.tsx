import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../../features/auth/AuthContext'
import { RUTAS } from '../../lib/rutas'
import { CargandoPagina } from './CargandoPagina'

export function RutaProtegida() {
  const { sesion } = useAuth()
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

  return <Outlet />
}
