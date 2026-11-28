import { useMutation, useQuery } from '@tanstack/react-query'
import {
  iniciarSesionApi,
  obtenerSesionApi,
  obtenerUsuarioPorEmail,
  obtenerUsuarioPorUsername,
  registrarUsuarioApi,
} from './api'
import { useAuth } from './AuthContext'
import { guardarToken, limpiarToken } from '../../lib/token'
import type { LoginRequest, RegisterUserRequest } from './tipos'

export function useLogin() {
  const { iniciarSesion } = useAuth()

  return useMutation({
    mutationFn: async (datos: LoginRequest) => {
      const auth = await iniciarSesionApi(datos)
      guardarToken(auth.accessToken, auth.expiresIn)

      try {
        const usuario = await obtenerSesionApi()
        return { auth, usuario }
      } catch (error) {
        limpiarToken()
        throw error
      }
    },
    onSuccess: ({ auth, usuario }) => iniciarSesion(auth, usuario),
  })
}

export function useRegistrarUsuario() {
  return useMutation({
    mutationFn: (datos: RegisterUserRequest) => registrarUsuarioApi(datos),
  })
}

export function useUsuarioPorEmail(email: string | null) {
  return useQuery({
    queryKey: ['usuario', 'email', email],
    queryFn: () => obtenerUsuarioPorEmail(email!),
    enabled: Boolean(email),
    staleTime: 60_000,
  })
}

export function useUsuarioPorUsername(username: string | null) {
  return useQuery({
    queryKey: ['usuario', 'username', username],
    queryFn: () => obtenerUsuarioPorUsername(username!),
    enabled: Boolean(username),
    staleTime: 60_000,
  })
}
