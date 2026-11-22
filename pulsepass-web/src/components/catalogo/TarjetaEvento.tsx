import { Link } from 'react-router'
import { formatearFecha } from '../../lib/formato'
import { RUTAS } from '../../lib/rutas'
import type { EventCategory, EventSummary } from '../../features/eventos/tipos'
import { BadgeCategoria, BadgeEstadoEvento } from '../ui/Etiquetas'
import { IconoCalendario, IconoUbicacion } from '../ui/Iconos'
import { IconoCategoria } from './IconoCategoria'

const gradientes: Record<EventCategory, string> = {
  MUSIC: 'from-violet-600 via-fuchsia-500 to-pink-500',
  TECHNOLOGY: 'from-sky-600 via-blue-500 to-indigo-500',
  SPORTS: 'from-emerald-600 via-teal-500 to-cyan-500',
  CULTURE: 'from-amber-600 via-orange-500 to-rose-500',
  EDUCATION: 'from-blue-700 via-indigo-500 to-violet-500',
  ENTERTAINMENT: 'from-pink-600 via-rose-500 to-orange-400',
}

interface TarjetaEventoProps {
  evento: EventSummary
  ciudad?: string
  sedeNombre?: string
  className?: string
}

export function TarjetaEvento({ evento, ciudad, sedeNombre, className }: TarjetaEventoProps) {
  const ubicacion = ciudad ?? sedeNombre ?? 'Sede por confirmar'

  return (
    <Link
      to={RUTAS.evento(evento.eventCode)}
      className={`group flex h-full flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-xl dark:border-slate-800 dark:bg-slate-900 ${className ?? ''}`}
    >
      <div
        className={`relative flex h-40 items-center justify-center overflow-hidden bg-gradient-to-br ${gradientes[evento.category]}`}
      >
        <div className="absolute -top-8 -right-8 size-32 rounded-full bg-white/10" />
        <div className="absolute -bottom-10 -left-8 size-32 rounded-full bg-black/10" />
        <IconoCategoria
          categoria={evento.category}
          className="size-16 text-white/85 transition duration-300 group-hover:scale-110"
        />
      </div>

      <div className="flex flex-1 flex-col p-5">
        <div className="flex flex-wrap items-center gap-2">
          <BadgeCategoria categoria={evento.category} />
          <BadgeEstadoEvento status={evento.status} />
        </div>
        <h2 className="mt-3 text-lg font-bold text-slate-900 transition group-hover:text-marca-700 dark:text-white dark:group-hover:text-marca-300">
          {evento.name}
        </h2>
        <div className="mt-4 space-y-2 text-sm text-slate-500 dark:text-slate-400">
          <p className="flex items-center gap-2">
            <IconoCalendario className="size-4 shrink-0" />
            <span>{formatearFecha(evento.eventDate)}</span>
          </p>
          <p className="flex items-center gap-2">
            <IconoUbicacion className="size-4 shrink-0" />
            <span>{ubicacion}</span>
          </p>
        </div>
      </div>
    </Link>
  )
}
