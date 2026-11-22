import { Link } from 'react-router'
import type { Venue } from '../../features/sedes/tipos'
import { RUTAS } from '../../lib/rutas'
import { Badge } from '../ui/Badge'
import { IconoUbicacion } from '../ui/Iconos'

interface TarjetaSedeProps {
  sede: Venue
  enlazar?: boolean
  className?: string
}

const formatoCapacidad = new Intl.NumberFormat('es-CO')

export function TarjetaSede({ sede, enlazar = true, className }: TarjetaSedeProps) {
  const contenido = (
    <article className="flex items-start gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-marca-300 hover:shadow-md dark:border-slate-800 dark:bg-slate-900 dark:hover:border-marca-700">
      <span className="flex size-12 shrink-0 items-center justify-center rounded-xl bg-marca-50 text-marca-600 dark:bg-marca-900/40 dark:text-marca-300">
        <IconoUbicacion className="size-6" />
      </span>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-start justify-between gap-2">
          <h2 className="font-bold text-slate-900 dark:text-white">{sede.name}</h2>
          <Badge tono={sede.active ? 'exito' : 'neutral'}>
            {sede.active ? 'Activa' : 'Inactiva'}
          </Badge>
        </div>
        <p className="mt-1 truncate text-sm text-slate-500 dark:text-slate-400">
          {sede.city} · Cap. {formatoCapacidad.format(sede.capacity)}
        </p>
        <p className="mt-1 truncate text-xs text-slate-400 dark:text-slate-500">{sede.address}</p>
      </div>
    </article>
  )

  if (!enlazar) return <div className={className}>{contenido}</div>

  return (
    <Link
      to={RUTAS.sede(sede.code)}
      className={`block rounded-2xl ${className ?? ''}`}
      aria-label={`Ver sede ${sede.name}`}
    >
      {contenido}
    </Link>
  )
}
