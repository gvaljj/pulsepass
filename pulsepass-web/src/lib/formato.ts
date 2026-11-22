const formatoMoneda = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
})

const formatoFecha = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
})

const formatoFechaHora = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

const formatoFechaCorta = new Intl.DateTimeFormat('es-CO', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
})

function aFechaLocal(valor: string): Date | null {
  if (!valor) return null

  const soloFecha = /^(\d{4})-(\d{2})-(\d{2})$/.exec(valor)
  if (soloFecha) {
    const [, anio, mes, dia] = soloFecha
    return new Date(Number(anio), Number(mes) - 1, Number(dia))
  }

  const conHora = /^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})(?::(\d{2}))?/.exec(valor)
  if (conHora) {
    const [, anio, mes, dia, hora, minuto, segundo] = conHora
    return new Date(
      Number(anio),
      Number(mes) - 1,
      Number(dia),
      Number(hora),
      Number(minuto),
      Number(segundo ?? 0),
    )
  }

  const fecha = new Date(valor)
  return Number.isNaN(fecha.getTime()) ? null : fecha
}

export function precioCOP(valor: number | string): string {
  const numero = typeof valor === 'number' ? valor : Number(valor)
  return Number.isFinite(numero) ? formatoMoneda.format(numero) : '—'
}

export function formatearFecha(valor: string): string {
  const fecha = aFechaLocal(valor)
  return fecha ? formatoFecha.format(fecha) : '—'
}

export function formatearFechaHora(valor: string): string {
  const fecha = aFechaLocal(valor)
  return fecha ? formatoFechaHora.format(fecha) : '—'
}

export function formatearFechaCorta(valor: string): string {
  const fecha = aFechaLocal(valor)
  return fecha ? formatoFechaCorta.format(fecha) : '—'
}

export function iniciales(nombre: string): string {
  return (
    nombre
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((parte) => parte.charAt(0).toUpperCase())
      .join('') || '?'
  )
}
