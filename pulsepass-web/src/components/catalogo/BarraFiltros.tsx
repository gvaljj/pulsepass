import { useId } from 'react'
import type { Artist } from '../../features/artistas/tipos'
import { CATEGORIAS_EVENTO, type EventCategory } from '../../features/eventos/tipos'
import { etiquetaCategoria } from '../ui/etiquetasFunciones'
import { Boton } from '../ui/Boton'
import { Selector } from '../ui/Selector'
import { IconoBuscar, IconoCerrar } from '../ui/Iconos'

interface BarraFiltrosProps {
  busqueda: string
  categoria: EventCategory | ''
  artista: string
  artistas: Artist[]
  onBusqueda: (valor: string) => void
  onCategoria: (valor: EventCategory | '') => void
  onArtista: (valor: string) => void
  onLimpiar: () => void
}

export function BarraFiltros({
  busqueda,
  categoria,
  artista,
  artistas,
  onBusqueda,
  onCategoria,
  onArtista,
  onLimpiar,
}: BarraFiltrosProps) {
  const busquedaId = useId()

  return (
    <div className="grid items-end gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm sm:grid-cols-2 lg:grid-cols-[1fr_190px_190px_auto] dark:border-slate-800 dark:bg-slate-900">
      <div>
        <label
          htmlFor={busquedaId}
          className="text-sm font-medium text-slate-700 dark:text-slate-200"
        >
          Buscar por nombre
        </label>
        <div className="relative mt-1.5">
          <IconoBuscar className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-slate-400" />
          <input
            id={busquedaId}
            type="search"
            value={busqueda}
            onChange={(evento) => onBusqueda(evento.target.value)}
            placeholder="Buscar por nombre…"
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pr-3.5 pl-10 text-sm text-slate-900 shadow-sm placeholder:text-slate-400 focus:border-marca-500 focus:ring-2 focus:ring-marca-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:placeholder:text-slate-500"
          />
        </div>
      </div>

      <Selector
        etiqueta="Categoría"
        placeholder="Todas"
        value={categoria}
        onChange={(evento) => onCategoria(evento.target.value as EventCategory | '')}
        opciones={CATEGORIAS_EVENTO.map((valor) => ({
          valor,
          etiqueta: etiquetaCategoria(valor),
        }))}
      />

      <Selector
        etiqueta="Artista"
        placeholder="Todos"
        value={artista}
        onChange={(evento) => onArtista(evento.target.value)}
        opciones={artistas.map((item) => ({
          valor: item.stageName,
          etiqueta: item.stageName,
        }))}
      />

      <Boton variante="fantasma" onClick={onLimpiar}>
        <IconoCerrar className="size-4" />
        Limpiar
      </Boton>
    </div>
  )
}
