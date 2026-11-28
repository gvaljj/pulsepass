import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate, useParams } from 'react-router'
import { esApiError, mensajeDeError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useAuth } from '../../features/auth/AuthContext'
import { useAviso } from '../../components/ui/Aviso'
import { useEvento } from './hooks'
import { useComprarTicket } from '../../features/tickets/hooks'
import { useSede } from '../../features/sedes/hooks'
import { DetalleEventoCabecera } from '../../components/catalogo/DetalleEventoCabecera'
import { ListaArtistasEvento } from '../../components/catalogo/ListaArtistasEvento'
import { TarjetaSede } from '../../components/catalogo/TarjetaSede'
import { ModalConfirmarCompra } from '../../components/eventos/ModalConfirmarCompra'
import { PanelCompra } from '../../components/eventos/PanelCompra'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { EstadoVacio } from '../../components/ui/EstadoVacio'
import { IconoChevronIzquierda } from '../../components/ui/Iconos'
import type { TicketType } from '../../features/tickets/tipos'

export default function PaginaDetalleEvento() {
  const { eventCode: eventCodeParam } = useParams<{ eventCode: string }>()
  const eventCode = eventCodeParam?.trim() ?? ''
  const location = useLocation()
  const navigate = useNavigate()
  const { sesion } = useAuth()
  const { mostrarAviso } = useAviso()

  const eventoQuery = useEvento(eventCode || null)
  const evento = eventoQuery.data ?? null
  const sedeQuery = useSede(evento?.venueCode ?? null)
  const autenticado = sesion.estado === 'autenticado'
  const email = autenticado ? sesion.usuario.email : ''
  const comprar = useComprarTicket(evento?.eventCode ?? '', email)

  const [tipo, setTipo] = useState<TicketType>('GENERAL')
  const [modalAbierto, setModalAbierto] = useState(false)
  const [errorCompra, setErrorCompra] = useState<string | undefined>()

  const esStaff =
    sesion.estado === 'autenticado' &&
    (sesion.usuario.roles.includes('STAFF') || sesion.usuario.roles.includes('ADMIN'))

  const puedeComprar = evento?.status === 'PUBLISHED'
  const motivoBloqueo = !puedeComprar
    ? evento?.status === 'SOLD_OUT'
      ? 'Entradas agotadas.'
      : evento?.status === 'CANCELLED'
        ? 'Evento cancelado.'
        : evento?.status === 'FINISHED'
          ? 'Evento finalizado.'
          : evento?.status === 'DRAFT'
            ? 'Evento no disponible para compra.'
            : 'Evento no disponible.'
    : undefined

  if (!eventCode) return <Navigate to={RUTAS.noEncontrada} replace />

  if (eventoQuery.isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <Cargando filas={3} mensaje="Cargando evento…" />
      </div>
    )
  }

  if (eventoQuery.isError) {
    if (esApiError(eventoQuery.error) && eventoQuery.error.status === 404) {
      return (
        <div className="mx-auto max-w-3xl px-4 py-16">
          <EstadoVacio
            titulo="Evento no disponible"
            mensaje="No encontramos este evento o ya no está publicado."
            accion={
              <Link to={RUTAS.eventos}>
                <Boton variante="secundario">Ver cartelera</Boton>
              </Link>
            }
          />
        </div>
      )
    }
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <EstadoError
          mensaje={mensajeDeError(eventoQuery.error, 'No pudimos cargar el evento.')}
          onReintentar={() => void eventoQuery.refetch()}
        />
      </div>
    )
  }

  if (!evento) return <Navigate to={RUTAS.noEncontrada} replace />

  async function confirmarCompra() {
    if (!autenticado || !puedeComprar) return
    setErrorCompra(undefined)
    try {
      const ticket = await comprar.mutateAsync(tipo)
      setModalAbierto(false)
      navigate(RUTAS.confirmacion(ticket.ticketCode))
    } catch (error) {
      if (esApiError(error)) {
        if (error.status === 409) {
          setErrorCompra(error.message)
        } else if (error.status === 403) {
          setModalAbierto(false)
          mostrarAviso(error.message || 'No tienes permisos para comprar.', 'error')
        } else if (error.status === 404) {
          setErrorCompra('Evento no disponible.')
        } else if (error.status === 401) {
          setModalAbierto(false)
        } else {
          setErrorCompra(mensajeDeError(error, 'No pudimos completar la compra.'))
        }
      } else {
        setErrorCompra(mensajeDeError(error, 'No pudimos completar la compra.'))
      }
    }
  }

  function abrirCompra() {
    if (!autenticado) {
      navigate(RUTAS.login, {
        state: { desde: `${location.pathname}${location.search}` },
      })
      return
    }
    if (!puedeComprar) return
    setErrorCompra(undefined)
    setModalAbierto(true)
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 pb-24 sm:px-6 lg:px-8 lg:pb-10">
      <Link
        to={RUTAS.eventos}
        className="inline-flex items-center gap-1 text-sm font-semibold text-slate-500 hover:text-marca-700 dark:text-slate-400 dark:hover:text-marca-300"
      >
        <IconoChevronIzquierda className="size-4" />
        Volver a la cartelera
      </Link>

      <div className={`mt-5 grid gap-6 ${!esStaff ? 'lg:grid-cols-[1fr_360px]' : ''}`}>
        <div className="space-y-5">
          <DetalleEventoCabecera evento={evento} />
          <ListaArtistasEvento artistas={evento.artists} />

          <section className="rounded-2xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
            <h2 className="text-lg font-bold text-slate-900 dark:text-white">Sede</h2>
            <div className="mt-3">
              {sedeQuery.isLoading ? (
                <div className="h-24 animate-pulse rounded-2xl bg-slate-100 dark:bg-slate-800" />
              ) : sedeQuery.data ? (
                <TarjetaSede sede={sedeQuery.data} />
              ) : (
                <p className="text-sm text-slate-500 dark:text-slate-400">{evento.venueName}</p>
              )}
            </div>
          </section>
        </div>

        {!esStaff && (
          <PanelCompra
            evento={evento}
            tipo={tipo}
            onTipo={setTipo}
            onComprar={abrirCompra}
            onIniciarSesion={abrirCompra}
            autenticado={autenticado}
            puedeComprar={!!puedeComprar}
            motivoBloqueo={motivoBloqueo}
          />
        )}
      </div>

      {!esStaff && (
        <ModalConfirmarCompra
          abierto={modalAbierto}
          evento={evento}
          tipo={tipo}
          email={email}
          error={errorCompra}
          cargando={comprar.isPending}
          onCerrar={() => {
            if (!comprar.isPending) setModalAbierto(false)
          }}
          onConfirmar={() => void confirmarCompra()}
        />
      )}

      {!esStaff && (
        <div className="fixed inset-x-0 bottom-0 z-30 border-t border-slate-200 bg-white/95 p-3 backdrop-blur lg:hidden dark:border-slate-800 dark:bg-slate-950/95">
          <Boton
            className="w-full"
            tamano="lg"
            onClick={abrirCompra}
            disabled={autenticado && !puedeComprar}
          >
            {autenticado ? 'Comprar entrada' : 'Inicia sesión para comprar'}
          </Boton>
        </div>
      )}
    </div>
  )
}