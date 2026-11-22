import { z } from 'zod'
import { obtenerToken } from './token'

export const ErrorResponseSchema = z.object({
  timestamp: z.union([z.string(), z.array(z.number())]).optional(),
  status: z.number().int(),
  error: z.string().optional(),
  message: z.string().min(1),
  details: z.record(z.string(), z.string()).optional(),
})

export type ErrorResponse = z.infer<typeof ErrorResponseSchema>

export class ApiError extends Error {
  readonly status: number
  readonly details: Record<string, string>
  readonly error?: string

  constructor(
    status: number,
    message: string,
    details: Record<string, string> = {},
    error?: string,
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
    this.error = error
  }
}

export function esApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}

export function mensajeDeError(error: unknown, fallback = 'Ocurrió un error inesperado.'): string {
  if (esApiError(error) || error instanceof Error) {
    return error.message || fallback
  }
  return fallback
}

let alNoAutorizado: (() => void) | null = null

export function configurarNoAutorizado(handler: (() => void) | null): void {
  alNoAutorizado = handler
}

const BASE = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/$/, '')

type Metodo = 'GET' | 'POST' | 'PATCH' | 'DELETE'

interface OpcionesSolicitud<T> {
  metodo?: Metodo
  cuerpo?: unknown
  signal?: AbortSignal
  esquema: z.ZodType<T>
  omitirCierreSesion?: boolean
}

function construirUrl(ruta: string): string {
  if (/^https?:\/\//.test(ruta)) return ruta
  return `${BASE}${ruta.startsWith('/') ? ruta : `/${ruta}`}`
}

async function leerJson(response: Response): Promise<unknown> {
  const texto = await response.text()
  if (!texto) return undefined
  try {
    return JSON.parse(texto)
  } catch {
    return undefined
  }
}

export async function solicitar<T>(ruta: string, opciones: OpcionesSolicitud<T>): Promise<T> {
  const headers = new Headers({ Accept: 'application/json' })
  const token = obtenerToken()

  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (opciones.cuerpo !== undefined) headers.set('Content-Type', 'application/json')

  let response: Response
  try {
    response = await fetch(construirUrl(ruta), {
      method: opciones.metodo ?? 'GET',
      headers,
      body: opciones.cuerpo === undefined ? undefined : JSON.stringify(opciones.cuerpo),
      signal: opciones.signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw new ApiError(0, 'No pudimos conectar con el servidor. Revisa tu conexión.')
  }

  if (response.status === 401 && !opciones.omitirCierreSesion) {
    alNoAutorizado?.()
  }

  if (!response.ok) {
    const data = await leerJson(response)
    const parsed = ErrorResponseSchema.safeParse(data)
    const mensaje = parsed.success ? parsed.data.message : `Error ${response.status}`
    const details = parsed.success ? (parsed.data.details ?? {}) : {}
    throw new ApiError(response.status, mensaje, details, parsed.success ? parsed.data.error : undefined)
  }

  if (response.status === 204) return undefined as T

  const data = await leerJson(response)
  const parsed = opciones.esquema.safeParse(data)

  if (!parsed.success) {
    throw new ApiError(
      response.status,
      'La respuesta del servidor no tiene el formato esperado.',
    )
  }

  return parsed.data
}
