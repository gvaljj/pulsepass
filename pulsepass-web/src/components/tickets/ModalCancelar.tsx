import { useState } from 'react'
import type { Ticket } from '../../features/tickets/tipos'
import { esApiError, mensajeDeError } from '../../lib/http'
import { useAviso } from '../ui/Aviso'
import { useCancelarTicket } from '../../features/tickets/hooks'
import { Boton } from '../ui/Boton'
import { Modal } from '../ui/Modal'

interface ModalCancelarProps {
  abierto: boolean
  ticket: Ticket | null
  email: string
  eventCode: string
  onCerrar: () => void
  onCancelado: (ticket: Ticket) => void
}

export function ModalCancelar({
  abierto,
  ticket,
  email,
  eventCode,
  onCerrar,
  onCancelado,
}: ModalCancelarProps) {
  const { mostrarAviso } = useAviso()
  const [error, setError] = useState<string | undefined>()
  const cancelar = useCancelarTicket(ticket?.ticketCode ?? '', email, eventCode)

  async function confirmar() {
    if (!ticket) return
    setError(undefined)
    try {
      const actualizado = await cancelar.mutateAsync()
      onCancelado(actualizado)
      onCerrar()
    } catch (err) {
      if (esApiError(err)) {
        if (err.status === 409) {
          setError(err.message || 'No se puede cancelar este ticket.')
        } else if (err.status === 403) {
          onCerrar()
          mostrarAviso(err.message || 'No tienes permisos para cancelar.', 'error')
        } else if (err.status === 401) {
          onCerrar()
        } else {
          setError(mensajeDeError(err, 'No pudimos cancelar el ticket.'))
        }
      } else {
        setError(mensajeDeError(err, 'No pudimos cancelar el ticket.'))
      }
    }
  }

  return (
    <Modal
      abierto={abierto}
      titulo="Cancelar ticket"
      descripcion="Esta acción no se puede deshacer."
      onCerrar={() => {
        if (!cancelar.isPending) onCerrar()
      }}
      cerrarAlHacerClicFuera={false}
      pie={
        <>
          <Boton variante="secundario" onClick={onCerrar} disabled={cancelar.isPending}>
            Volver
          </Boton>
          <Boton variante="peligro" onClick={() => void confirmar()} cargando={cancelar.isPending}>
            Cancelar ticket
          </Boton>
        </>
      }
    >
      <p className="text-sm text-slate-600 dark:text-slate-300">
        ¿Seguro que quieres cancelar el ticket{' '}
        <span className="font-mono font-semibold">{ticket?.ticketCode}</span>?
      </p>
      {error && (
        <p role="alert" className="mt-4 rounded-xl bg-peligro-50 p-3 text-sm text-peligro-800 dark:bg-peligro-700/20 dark:text-peligro-100">
          {error}
        </p>
      )}
    </Modal>
  )
}