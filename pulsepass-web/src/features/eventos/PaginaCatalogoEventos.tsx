import { useMemo } from 'react'
import { useSearchParams } from 'react-router'
import { useArtistasActivos } from '../artistas/hooks'
import { useEventosPublicados, useEventosPorArtista } from '../eventos/hooks'
import { EventCategorySchema, type EventCategory } from '../eventos/tipos'
import { useSedesActivas } from '../sedes/hooks'
import { BarraFiltros } from '../../components/catalogo/BarraFiltros'
import { GrillaEventos } from '../../components/catalogo/GrillaEventos'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'
import { mensajeDeError } from '../../lib/http'

function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLocaleLowerCase('es-CO')
    .trim()
}

export default function PaginaCatalogoEventos() {
  const [searchParams, setSearchParams] = useSearchParams()
  const q = searchParams.get('q') ?? ''
  const artista = searchParams.get('artista') ?? ''
  const categoriaParam = searchParams.get('categoria') ?? ''
  const categoriaParseada = EventCategorySchema.safeParse(categoriaParam)
  const categoria: EventCategory | '' = categoriaParseada.success ? categoriaParseada.data : ''

  const eventosQuery = useEventosPublicados(!artista)
  const eventosArtistaQuery = useEventosPorArtista(artista || null)
  const artistasQuery = useArtistasActivos()
  const sedesQuery = useSedesActivas()

  const consulta = artista ? eventosArtistaQuery : eventosQuery
  const base = useMemo(
    () => (artista ? (eventosArtistaQuery.data ?? []) : (eventosQuery.data ?? [])),
    [artista, eventosArtistaQuery.data, eventosQuery.data],
  )

  const sedesPorCodigo = useMemo(
    () => new Map((sedesQuery.data ?? []).map((sede) => [sede.code, sede])),
    [sedesQuery.data],
  )

  const eventosFiltrados = useMemo(() => {
    const termino = normalizar(q)
    return base.filter((evento) => {
      const coincideTexto = !termino || normalizar(evento.name).includes(termino)
      const coincideCategoria = !categoria || evento.category === categoria
      return coincideTexto && coincideCategoria
    })
  }, [base, q, categoria])

  function actualizarParametro(clave: string, valor: string) {
    const siguientes = new URLSearchParams(searchParams)
    if (valor) siguientes.set(clave, valor)
    else siguientes.delete(clave)
    setSearchParams(siguientes, { replace: true })
  }

  function limpiarFiltros() {
    setSearchParams(new URLSearchParams(), { replace: true })
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <header className="max-w-2xl">
        <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
          Explora
        </p>
        <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl dark:text-white">
          Cartelera
        </h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          Encuentra tu próximo plan entre música, cultura, deporte y tecnología.
        </p>
      </header>

      <div className="mt-6">
        <BarraFiltros
          busqueda={q}
          categoria={categoria}
          artista={artista}
          artistas={artistasQuery.data ?? []}
          onBusqueda={(valor) => actualizarParametro('q', valor)}
          onCategoria={(valor) => actualizarParametro('categoria', valor)}
          onArtista={(valor) => actualizarParametro('artista', valor)}
          onLimpiar={limpiarFiltros}
        />
      </div>

      <p className="mt-5 text-sm font-semibold text-slate-500 dark:text-slate-400" aria-live="polite">
        {consulta.isLoading ? 'Buscando eventos…' : `${eventosFiltrados.length} eventos`}
      </p>

      <div className="mt-5">
        {consulta.isLoading ? (
          <Cargando filas={6} mensaje="Cargando cartelera…" />
        ) : consulta.isError ? (
          <EstadoError
            mensaje={mensajeDeError(consulta.error, 'No pudimos cargar la cartelera.')}
            onReintentar={() => void consulta.refetch()}
          />
        ) : eventosFiltrados.length === 0 ? (
          <EstadoVacio
            titulo={base.length === 0 ? 'Aún no hay eventos publicados' : 'Sin resultados para tu búsqueda'}
            mensaje={
              base.length === 0
                ? 'Vuelve pronto para descubrir nuevas experiencias.'
                : 'Prueba con otro término, cambia la categoría o limpia los filtros.'
            }
            accion={
              base.length > 0 ? (
                <Boton variante="secundario" onClick={limpiarFiltros}>
                  Limpiar filtros
                </Boton>
              ) : undefined
            }
          />
        ) : (
          <GrillaEventos eventos={eventosFiltrados} sedesPorCodigo={sedesPorCodigo} />
        )}
      </div>
    </div>
  )
}
