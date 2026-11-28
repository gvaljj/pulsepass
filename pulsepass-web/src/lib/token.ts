import { z } from 'zod'

const CLAVE_TOKEN = 'pulsepass.token'
const CLAVE_EXPIRA = 'pulsepass.expiraEn'

const tokenSchema = z.string().min(1)
const expiraSchema = z.coerce.number().int().positive()

function leer(clave: string): string | null {
  try {
    return sessionStorage.getItem(clave)
  } catch {
    return null
  }
}

function escribir(clave: string, valor: string): void {
  try {
    sessionStorage.setItem(clave, valor)
  } catch {
    // La sesión sigue viva en memoria aunque sessionStorage no esté disponible.
  }
}

function eliminar(clave: string): void {
  try {
    sessionStorage.removeItem(clave)
  } catch {
    // sin efecto
  }
}

const tokenInicial = tokenSchema.safeParse(leer(CLAVE_TOKEN))
const expiraInicial = expiraSchema.safeParse(leer(CLAVE_EXPIRA))
const sesionInicialValida =
  tokenInicial.success && expiraInicial.success && expiraInicial.data > Date.now()

let tokenEnMemoria: string | null = sesionInicialValida ? tokenInicial.data : null
let expiraEnMemoria: number | null = sesionInicialValida ? expiraInicial.data : null

if (!sesionInicialValida) {
  eliminar(CLAVE_TOKEN)
  eliminar(CLAVE_EXPIRA)
}

export function obtenerToken(): string | null {
  return tokenEnMemoria
}

export function obtenerExpiracion(): number | null {
  return expiraEnMemoria
}

export function tokenEsVigente(): boolean {
  return tokenEnMemoria !== null && expiraEnMemoria !== null && expiraEnMemoria > Date.now()
}

export function guardarToken(accessToken: string, expiresInSegundos: number): number {
  const expiraEn = Date.now() + Math.max(0, expiresInSegundos) * 1000
  tokenEnMemoria = accessToken
  expiraEnMemoria = expiraEn
  escribir(CLAVE_TOKEN, accessToken)
  escribir(CLAVE_EXPIRA, String(expiraEn))
  return expiraEn
}

export function limpiarToken(): void {
  tokenEnMemoria = null
  expiraEnMemoria = null
  eliminar(CLAVE_TOKEN)
  eliminar(CLAVE_EXPIRA)
}
