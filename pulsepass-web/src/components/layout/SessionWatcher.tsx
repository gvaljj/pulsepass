import { useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { useAuth } from '../../features/auth/AuthContext'
import { RUTAS } from '../../lib/rutas'

export function SessionWatcher() {
  const { motivoCierre, limpiarMotivo } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    if (!motivoCierre) return

    const temporizador = window.setTimeout(() => {
      const esExpirada = motivoCierre === 'expirada'
      const destino = esExpirada ? RUTAS.login : RUTAS.inicio

      limpiarMotivo()
      if (location.pathname === destino) return

      navigate(destino, {
        replace: true,
        state: esExpirada
          ? { motivo: 'expirada', desde: `${location.pathname}${location.search}` }
          : undefined,
      })
    }, 0)

    return () => window.clearTimeout(temporizador)
  }, [motivoCierre, limpiarMotivo, navigate, location.pathname, location.search])

  return null
}
