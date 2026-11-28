import type { Event } from '../../features/eventos/tipos'
import type { TicketType } from '../../features/tickets/tipos'
import { etiquetaTipoTicket } from '../ui/etiquetasFunciones'
import { Boton } from '../ui/Boton'
import { Modal } from '../ui/Modal'

interface ModalConfirmarCompraProps {
  abierto: boolean
  evento: Event
  tipo: TicketType
  email: string
  error?: string
  cargando: boolean
  onCerrar: () => void
  onConfirmar: () => void
}

export function ModalConfirmarCompra({
  abierto,
  evento,
  tipo,
  email,
  error,
  cargando,
  onCerrar,
  onConfirmar,
}: ModalConfirmarCompraProps) {
  return (
    <Modal
      abierto={abierto}
      titulo="Confirmar compra"
      descripcion="Revisa los datos antes de confirmar."
      onCerrar={onCerrar}
      cerrarAlHacerClicFuera={false}
      pie={
        <>
          <Boton variante="secundario" onClick={onCerrar} disabled={cargando}>
            Cancelar
          </Boton>
          <Boton onClick={onConfirmar} cargando={cargando}>
            Confirmar compra
          </Boton>
        </>
      }
    >
      <dl className="space-y-3 text-sm">
        <div className="flex justify-between gap-4">
          <dt className="text-slate-500 dark:text-slate-400">Evento</dt>
          <dd className="text-right font-semibold text-slate-900 dark:text-white">{evento.name}</dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt className="text-slate-500 dark:text-slate-400">Tipo</dt>
          <dd className="font-semibold text-slate-900 dark:text-white">{etiquetaTipoTicket(tipo)}</dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt className="text-slate-500 dark:text-slate-400">Cuenta</dt>
          <dd className="truncate font-semibold text-slate-900 dark:text-white">{email}</dd>
        </div>
      </dl>

      {error && (
        <p
          role="alert"
          className="mt-4 rounded-xl bg-peligro-50 p-3 text-sm text-peligro-800 dark:bg-peligro-700/20 dark:text-peligro-100"
        >
          {error}
        </p>
      )}
    </Modal>
  )
}
