export interface EventoCreado {
  eventCode: string
  name: string
  status: string
}

const CLAVE = 'pulsepass.admin.eventos'

export function leerEventosCreados(): EventoCreado[] {
  try {
    const bruto = sessionStorage.getItem(CLAVE)
    if (!bruto) return []
    const datos = JSON.parse(bruto) as unknown
    if (!Array.isArray(datos)) return []
    return datos.filter(
      (item): item is EventoCreado =>
        typeof item === 'object' &&
        item !== null &&
        typeof (item as EventoCreado).eventCode === 'string' &&
        typeof (item as EventoCreado).name === 'string' &&
        typeof (item as EventoCreado).status === 'string',
    )
  } catch {
    return []
  }
}

export function guardarEventoCreado(evento: EventoCreado): void {
  const actuales = leerEventosCreados().filter((item) => item.eventCode !== evento.eventCode)
  sessionStorage.setItem(CLAVE, JSON.stringify([evento, ...actuales].slice(0, 20)))
}