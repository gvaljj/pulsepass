import type { ComponentType, SVGProps } from 'react'
import type { EventCategory } from '../../features/eventos/tipos'
import {
  IconoCultura,
  IconoDeporte,
  IconoEducacion,
  IconoEntretenimiento,
  IconoMusica,
  IconoTecnologia,
} from '../ui/Iconos'

const iconos: Record<EventCategory, ComponentType<SVGProps<SVGSVGElement>>> = {
  MUSIC: IconoMusica,
  TECHNOLOGY: IconoTecnologia,
  SPORTS: IconoDeporte,
  CULTURE: IconoCultura,
  EDUCATION: IconoEducacion,
  ENTERTAINMENT: IconoEntretenimiento,
}

interface IconoCategoriaProps {
  categoria: EventCategory
  className?: string
}

export function IconoCategoria({ categoria, className }: IconoCategoriaProps) {
  const Icono = iconos[categoria]
  return <Icono className={className} />
}
