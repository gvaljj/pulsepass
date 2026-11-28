import type { EventSummary } from '../../features/eventos/tipos'
import type { Venue } from '../../features/sedes/tipos'
import { TarjetaEvento } from './TarjetaEvento'

interface GrillaEventosProps {
  eventos: EventSummary[]
  sedesPorCodigo: Map<string, Venue>
  className?: string
}

export function GrillaEventos({ eventos, sedesPorCodigo, className }: GrillaEventosProps) {
  return (
    <div className={`grid gap-6 sm:grid-cols-2 lg:grid-cols-3 ${className ?? ''}`}>
      {eventos.map((evento) => {
        const sede = sedesPorCodigo.get(evento.venueCode)
        return (
          <TarjetaEvento
            key={evento.eventCode}
            evento={evento}
            ciudad={sede?.city}
            sedeNombre={sede?.name}
          />
        )
      })}
    </div>
  )
}
