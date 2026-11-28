import { z } from 'zod'

export const RolSchema = z.enum(['USER', 'STAFF', 'ADMIN'])

export type Rol = z.infer<typeof RolSchema>

export const AuthResponseSchema = z.object({
  accessToken: z.string().min(1),
  tokenType: z.string(),
  expiresIn: z.number().int().positive(),
})

export type AuthResponse = z.infer<typeof AuthResponseSchema>

export const AuthenticatedUserSchema = z.object({
  id: z.number(),
  username: z.string(),
  email: z.string(),
  roles: z.array(RolSchema),
  active: z.boolean(),
})

export type AuthenticatedUser = z.infer<typeof AuthenticatedUserSchema>

export const UserResponseSchema = z.object({
  id: z.number(),
  username: z.string(),
  email: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  phone: z.string().nullable().optional(),
  city: z.string().nullable().optional(),
  birthDate: z.string(),
  active: z.boolean(),
})

export type UserResponse = z.infer<typeof UserResponseSchema>

export const LoginRequestSchema = z.object({
  identifier: z.string().trim().min(1, 'Ingresa tu usuario o correo'),
  password: z.string().min(1, 'Ingresa tu contraseña'),
})

export type LoginRequest = z.infer<typeof LoginRequestSchema>

export const RegisterUserRequestSchema = z.object({
  username: z.string().trim().min(1, 'El nombre de usuario es obligatorio'),
  email: z.string().trim().email('El correo no es válido'),
  firstName: z.string().trim().min(1, 'El nombre es obligatorio'),
  lastName: z.string().trim().min(1, 'El apellido es obligatorio'),
  birthDate: z
    .string()
    .min(1, 'La fecha de nacimiento es obligatoria')
    .refine(
      (value) => value <= new Date().toISOString().slice(0, 10),
      'La fecha de nacimiento no puede ser futura',
    ),
  password: z
    .string()
    .min(12, 'La contraseña debe tener al menos 12 caracteres')
    .refine(
      (value) => new TextEncoder().encode(value).length <= 72,
      'La contraseña no puede superar 72 bytes',
    ),
  phone: z.string().trim().optional(),
  city: z.string().trim().optional(),
})

export type RegisterUserRequest = z.infer<typeof RegisterUserRequestSchema>
