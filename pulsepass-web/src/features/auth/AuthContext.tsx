import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError, configurarNoAutorizado } from '../../lib/http'
import { guardarToken, limpiarToken, obtenerExpiracion, obtenerToken } from '../../lib/token'
import { obtenerSesionApi } from './api'
import type { AuthenticatedUser, AuthResponse, Rol } from './tipos'

export type MotivoCierre = 'manual' | 'expirada' | null

export type Sesion =
  | { estado: 'cargando' }
  | { estado: 'anonimo' }
  | { estado: 'autenticado'; usuario: AuthenticatedUser; expiraEn: number }

interface AuthContextValue {
  sesion: Sesion
  motivoCierre: MotivoCierre
  iniciarSesion: (auth: AuthResponse, usuario: AuthenticatedUser) => void
  cerrarSesion: (motivo?: Exclude<MotivoCierre, null>) => void
  limpiarMotivo: () => void
  tieneRol: (...roles: Rol[]) => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const queryClient = useQueryClient()
  const [token, setToken] = useState<string | null>(() => obtenerToken())
  const [expiraEn, setExpiraEn] = useState<number | null>(() => obtenerExpiracion())
  const [motivoCierre, setMotivoCierre] = useState<MotivoCierre>(null)

  const sesionQuery = useQuery({
    queryKey: ['sesion'],
    queryFn: obtenerSesionApi,
    enabled: Boolean(token),
    retry: false,
    staleTime: Infinity,
    gcTime: Infinity,
  })

  const cerrarSesion = useCallback(
    (motivo: Exclude<MotivoCierre, null> = 'manual') => {
      limpiarToken()
      setToken(null)
      setExpiraEn(null)
      setMotivoCierre(motivo)
      queryClient.clear()
    },
    [queryClient],
  )

  useEffect(() => {
    configurarNoAutorizado(() => cerrarSesion('expirada'))
    return () => configurarNoAutorizado(null)
  }, [cerrarSesion])

  useEffect(() => {
    if (!(sesionQuery.error instanceof ApiError) || sesionQuery.error.status !== 401) return
    const temporizador = window.setTimeout(() => cerrarSesion('expirada'), 0)
    return () => window.clearTimeout(temporizador)
  }, [sesionQuery.error, cerrarSesion])

  useEffect(() => {
    if (!expiraEn) return
    const restante = Math.max(0, expiraEn - Date.now())
    const temporizador = window.setTimeout(() => cerrarSesion('expirada'), restante)
    return () => window.clearTimeout(temporizador)
  }, [expiraEn, cerrarSesion])

  const iniciarSesion = useCallback(
    (auth: AuthResponse, usuario: AuthenticatedUser) => {
      const nuevaExpiracion = guardarToken(auth.accessToken, auth.expiresIn)
      setToken(auth.accessToken)
      setExpiraEn(nuevaExpiracion)
      setMotivoCierre(null)
      queryClient.setQueryData(['sesion'], usuario)
    },
    [queryClient],
  )

  const limpiarMotivo = useCallback(() => setMotivoCierre(null), [])

  const sesion = useMemo<Sesion>(() => {
    if (!token) return { estado: 'anonimo' }
    if (sesionQuery.data) {
      return { estado: 'autenticado', usuario: sesionQuery.data, expiraEn: expiraEn ?? 0 }
    }
    return { estado: 'cargando' }
  }, [token, sesionQuery.data, expiraEn])

  const tieneRol = useCallback(
    (...roles: Rol[]) =>
      sesion.estado === 'autenticado' && roles.some((rol) => sesion.usuario.roles.includes(rol)),
    [sesion],
  )

  const value = useMemo<AuthContextValue>(
    () => ({
      sesion,
      motivoCierre,
      iniciarSesion,
      cerrarSesion,
      limpiarMotivo,
      tieneRol,
    }),
    [sesion, motivoCierre, iniciarSesion, cerrarSesion, limpiarMotivo, tieneRol],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const contexto = useContext(AuthContext)
  if (!contexto) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return contexto
}
