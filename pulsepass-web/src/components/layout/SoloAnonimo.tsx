import { Navigate, Outlet } from 'react-router'
import { useAuth } from '../../features/auth/AuthContext'
import { RUTAS } from '../../lib/rutas'
import { CargandoPagina } from './CargandoPagina'

export function SoloAnonimo() {
  const { sesion } = useAuth()

  if (sesion.estado === 'cargando') return <CargandoPagina />
  if (sesion.estado === 'autenticado') return <Navigate to={RUTAS.inicio} replace />

  return <Outlet />
}
