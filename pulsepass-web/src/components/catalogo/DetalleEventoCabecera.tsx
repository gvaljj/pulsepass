import type { Event, EventCategory } from '../../features/eventos/tipos'
import { formatearFechaHora } from '../../lib/formato'
import { BadgeCategoria, BadgeEstadoEvento } from '../ui/Etiquetas'
import { IconoAlerta, IconoCalendario } from '../ui/Iconos'
import { IconoCategoria } from './IconoCategoria'

const gradientes: Record<EventCategory, string> = {
  MUSIC: 'from-violet-600 via-fuchsia-500 to-pink-500',
  TECHNOLOGY: 'from-sky-600 via-blue-500 to-indigo-500',
  SPORTS: 'from-emerald-600 via-teal-500 to-cyan-500',
  CULTURE: 'from-amber-600 via-orange-500 to-rose-500',
  EDUCATION: 'from-blue-700 via-indigo-500 to-violet-500',
  ENTERTAINMENT: 'from-pink-600 via-rose-500 to-orange-400',
}

export function DetalleEventoCabecera({ evento }: { evento: Event }) {
  return (
    <article className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div
        className={`relative flex h-48 items-center justify-center overflow-hidden bg-gradient-to-br sm:h-64 ${gradientes[evento.category]}`}
      >
        <div className="absolute -top-12 -right-10 size-48 rounded-full bg-white/10" />
        <div className="absolute -bottom-16 -left-10 size-56 rounded-full bg-black/10" />
        <IconoCategoria categoria={evento.category} className="size-20 text-white/85" />
      </div>

      <div className="p-5 sm:p-7">
        <div className="flex flex-wrap items-center gap-2">
          <BadgeCategoria categoria={evento.category} />
          <BadgeEstadoEvento status={evento.status} />
        </div>
        <h1 className="mt-3 text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl dark:text-white">
          {evento.name}
        </h1>
        <div className="mt-4 flex flex-wrap items-center gap-x-5 gap-y-2 text-sm text-slate-600 dark:text-slate-300">
          <span className="flex items-center gap-2">
            <IconoCalendario className="size-4" />
            {formatearFechaHora(evento.eventDate)}
          </span>
          <span className="flex items-center gap-2">
            <IconoAlerta className="size-4" />
            {evento.minimumAge > 0 ? `Edad mínima: ${evento.minimumAge}` : 'Todo público'}
          </span>
        </div>
        <p className="mt-5 whitespace-pre-line text-sm leading-6 text-slate-600 sm:text-base dark:text-slate-300">
          {evento.description?.trim() || 'Sin descripción disponible.'}
        </p>
      </div>
    </article>
  )
}
