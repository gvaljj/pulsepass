import {
  AuthResponseSchema,
  AuthenticatedUserSchema,
  UserResponseSchema,
  type AuthResponse,
  type AuthenticatedUser,
  type LoginRequest,
  type RegisterUserRequest,
  type UserResponse,
} from './tipos'

import { solicitar } from '../../lib/http'

export function iniciarSesionApi(cuerpo: LoginRequest): Promise<AuthResponse> {
  return solicitar('/auth/login', {
    metodo: 'POST',
    cuerpo,
    esquema: AuthResponseSchema,
    omitirCierreSesion: true,
  })
}

export function obtenerSesionApi(): Promise<AuthenticatedUser> {
  return solicitar('/auth/me', { esquema: AuthenticatedUserSchema })
}

export function registrarUsuarioApi(cuerpo: RegisterUserRequest): Promise<UserResponse> {
  return solicitar('/users', {
    metodo: 'POST',
    cuerpo,
    esquema: UserResponseSchema,
    omitirCierreSesion: true,
  })
}

export function obtenerUsuarioPorEmail(email: string): Promise<UserResponse> {
  return solicitar(`/users/by-email?email=${encodeURIComponent(email)}`, {
    esquema: UserResponseSchema,
  })
}

export function obtenerUsuarioPorUsername(username: string): Promise<UserResponse> {
  return solicitar(`/users/by-username?username=${encodeURIComponent(username)}`, {
    esquema: UserResponseSchema,
  })
}
