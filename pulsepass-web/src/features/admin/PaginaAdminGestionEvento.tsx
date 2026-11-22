import { useState } from 'react'
import { Link, Navigate, useParams } from 'react-router'
import { esApiError, mensajeDeError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useAviso } from '../../components/ui/Aviso'
import { useEvento, usePublicarEvento, useAsociarArtista } from '../../features/eventos/hooks'
import { useArtistasActivos } from '../../features/artistas/hooks'
import { BadgeEstadoEvento } from '../../components/ui/Etiquetas'
import { AvatarArtista } from '../../components/catalogo/AvatarArtista'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'
import { Modal } from '../../components/ui/Modal'
import { Selector } from '../../components/ui/Selector'
import { formatearFechaHora } from '../../lib/formato'

export default function PaginaAdminGestionEvento() {
  const { eventCode: eventCodeParam } = useParams<{ eventCode: string }>()
  const eventCode = eventCodeParam?.trim() ?? ''
  const { mostrarAviso } = useAviso()

  const eventoQuery = useEvento(eventCode || null)
  const evento = eventoQuery.data ?? null
  const artistasQuery = useArtistasActivos()
  const publicar = usePublicarEvento(eventCode)
  const asociar = useAsociarArtista(eventCode)

  const [modalPublicar, setModalPublicar] = useState(false)
  const [artistaSeleccionado, setArtistaSeleccionado] = useState('')

  const artistasActivos = artistasQuery.data ?? []
  const artistasDisponibles = artistasActivos.filter(
    (artista) => !evento?.artists.some((asociado) => asociado.id === artista.id),
  )

  if (!eventCode) return <Navigate to={RUTAS.adminEventos} replace />

  if (eventoQuery.isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12">
        <Cargando filas={2} mensaje="Cargando evento…" />
      </div>
    )
  }

  if (eventoQuery.isError) {
    if (esApiError(eventoQuery.error) && eventoQuery.error.status === 404) {
      return (
        <div className="mx-auto max-w-3xl px-4 py-16">
          <EstadoVacio
            titulo="Evento no encontrado"
            mensaje="No encontramos este evento."
            accion={
              <Link to={RUTAS.adminEventos}>
                <Boton variante="secundario">Volver a eventos</Boton>
              </Link>
            }
          />
        </div>
      )
    }
    return (
      <div className="mx-auto max-w-7xl px-4 py-12">
        <EstadoError
          mensaje={mensajeDeError(eventoQuery.error, 'No pudimos cargar el evento.')}
          onReintentar={() => void eventoQuery.refetch()}
        />
      </div>
    )
  }

  if (!evento) return <Navigate to={RUTAS.adminEventos} replace />

  async function publicarEvento() {
    try {
      await publicar.mutateAsync('PUBLISHED')
      mostrarAviso('Evento publicado correctamente.')
      setModalPublicar(false)
    } catch (error) {
      setModalPublicar(false)
      if (esApiError(error)) {
        mostrarAviso(error.message || 'No se pudo publicar el evento.', 'error')
      } else {
        mostrarAviso(mensajeDeError(error, 'No se pudo publicar el evento.'), 'error')
      }
    }
  }

  async function asociarArtista() {
    const id = Number(artistaSeleccionado)
    if (!Number.isInteger(id) || id <= 0) return
    try {
      await asociar.mutateAsync(id)
      mostrarAviso('Artista asociado correctamente.')
      setArtistaSeleccionado('')
    } catch (error) {
      if (esApiError(error)) {
        if (error.status === 409) {
          mostrarAviso('El artista ya está asociado a este evento.', 'error')
        } else if (error.status === 404) {
          mostrarAviso('No encontramos el artista o el evento.', 'error')
        } else if (error.status === 403) {
          mostrarAviso('No tienes permisos para asociar artistas.', 'error')
        } else {
          mostrarAviso(mensajeDeError(error, 'No se pudo asociar el artista.'), 'error')
        }
      } else {
        mostrarAviso(mensajeDeError(error, 'No se pudo asociar el artista.'), 'error')
      }
    }
  }

  const puedePublicar = evento.status === 'DRAFT'

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <Link
        to={RUTAS.adminEventos}
        className="inline-flex items-center gap-1 text-sm font-semibold text-slate-500 hover:text-marca-700 dark:text-slate-400 dark:hover:text-marca-300"
      >
        Eventos
      </Link>

      <section className="mt-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="text-xs font-bold tracking-widest text-slate-500 uppercase dark:text-slate-400">
              {evento.eventCode}
            </p>
            <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
              {evento.name}
            </h1>
          </div>
          <BadgeEstadoEvento status={evento.status} />
        </div>

        <dl className="mt-5 grid gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Fecha
            </dt>
            <dd className="mt-1 text-slate-800 dark:text-slate-100">
              {formatearFechaHora(evento.eventDate)}
            </dd>
          </div>
          <div>
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Sede
            </dt>
            <dd className="mt-1 text-slate-800 dark:text-slate-100">{evento.venueName}</dd>
          </div>
          <div>
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Edad mínima
            </dt>
            <dd className="mt-1 text-slate-800 dark:text-slate-100">
              {evento.minimumAge > 0 ? evento.minimumAge : 'Todo público'}
            </dd>
          </div>
          <div>
            <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
              Categoría
            </dt>
            <dd className="mt-1 text-slate-800 dark:text-slate-100">{evento.category}</dd>
          </div>
        </dl>

        {evento.description && (
          <p className="mt-5 whitespace-pre-line text-sm text-slate-600 dark:text-slate-300">
            {evento.description}
          </p>
        )}
      </section>

      <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <h2 className="text-xl font-bold text-slate-900 dark:text-white">Publicación</h2>
        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Los eventos se crean en borrador. Publícalos cuando estén listos.
        </p>
        <div className="mt-4 flex flex-wrap items-center gap-3">
          <Boton onClick={() => setModalPublicar(true)} disabled={!puedePublicar} cargando={publicar.isPending}>
            Publicar evento
          </Boton>
          {!puedePublicar && (
            <span className="text-sm text-slate-500 dark:text-slate-400">
              Solo se pueden publicar eventos en borrador.
            </span>
          )}
        </div>
      </section>

      <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <h2 className="text-xl font-bold text-slate-900 dark:text-white">Artistas asociados</h2>
        {evento.artists.length === 0 ? (
          <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">Sin artistas asociados.</p>
        ) : (
          <ul className="mt-4 flex flex-wrap gap-3">
            {evento.artists.map((artista) => (
              <li
                key={artista.id}
                className="flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 py-1.5 pr-4 pl-1.5 dark:border-slate-700 dark:bg-slate-800"
              >
                <AvatarArtista nombre={artista.stageName} tamano="sm" />
                <span>
                  <span className="block text-sm font-semibold text-slate-800 dark:text-slate-100">
                    {artista.stageName}
                  </span>
                  <span className="block text-xs text-slate-500 dark:text-slate-400">{artista.genre}</span>
                </span>
              </li>
            ))}
          </ul>
        )}

        <div className="mt-5 flex flex-col gap-3 sm:flex-row sm:items-end">
          <div className="flex-1">
            <Selector
              etiqueta="Agregar artista"
              placeholder="Selecciona un artista"
              value={artistaSeleccionado}
              onChange={(evento) => setArtistaSeleccionado(evento.target.value)}
              opciones={artistasDisponibles.map((artista) => ({
                valor: String(artista.id),
                etiqueta: `${artista.stageName} · ${artista.genre}`,
              }))}
            />
          </div>
          <Boton
            onClick={() => void asociarArtista()}
            disabled={!artistaSeleccionado || asociar.isPending}
            cargando={asociar.isPending}
          >
            Asociar
          </Boton>
        </div>
        {artistasQuery.isLoading && (
          <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">Cargando artistas…</p>
        )}
      </section>

      <div className="mt-6">
        <Link to={RUTAS.staffTickets(evento.eventCode)}>
          <Boton variante="secundario">Ver tickets pagados del evento</Boton>
        </Link>
      </div>

      <Modal
        abierto={modalPublicar}
        titulo="Publicar evento"
        descripcion="El evento pasará a estado publicado y será visible en la cartelera."
        onCerrar={() => {
          if (!publicar.isPending) setModalPublicar(false)
        }}
        pie={
          <>
            <Boton variante="secundario" onClick={() => setModalPublicar(false)} disabled={publicar.isPending}>
              Cancelar
            </Boton>
            <Boton onClick={() => void publicarEvento()} cargando={publicar.isPending}>
              Publicar
            </Boton>
          </>
        }
      >
        <p className="text-sm text-slate-600 dark:text-slate-300">
          ¿Confirmas la publicación de <strong>{evento.name}</strong>?
        </p>
      </Modal>
    </div>
  )
}