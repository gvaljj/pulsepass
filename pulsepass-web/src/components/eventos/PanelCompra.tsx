import type { Event } from '../../features/eventos/tipos'
import { TIPOS_TICKET, type TicketType } from '../../features/tickets/tipos'
import { etiquetaTipoTicket } from '../ui/etiquetasFunciones'
import { Boton } from '../ui/Boton'
import { IconoAlerta, IconoTicket } from '../ui/Iconos'

interface PanelCompraProps {
  evento: Event
  tipo: TicketType
  onTipo: (tipo: TicketType) => void
  onComprar: () => void
  onIniciarSesion: () => void
  autenticado: boolean
  puedeComprar: boolean
  motivoBloqueo?: string
}

export function PanelCompra({
  evento,
  tipo,
  onTipo,
  onComprar,
  onIniciarSesion,
  autenticado,
  puedeComprar,
  motivoBloqueo,
}: PanelCompraProps) {
  return (
    <aside
      id="panel-compra"
      aria-labelledby="titulo-compra"
      className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm lg:sticky lg:top-24 dark:border-slate-800 dark:bg-slate-900"
    >
      <div className="flex items-center gap-2">
        <span className="rounded-xl bg-marca-50 p-2 text-marca-600 dark:bg-marca-900/40 dark:text-marca-300">
          <IconoTicket className="size-5" />
        </span>
        <h2 id="titulo-compra" className="text-lg font-bold text-slate-900 dark:text-white">
          Entradas
        </h2>
      </div>

      <fieldset className="mt-4">
        <legend className="text-sm font-semibold text-slate-700 dark:text-slate-200">
          Tipo de entrada
        </legend>
        <div className="mt-2 space-y-2">
          {TIPOS_TICKET.map((opcion) => (
            <label
              key={opcion}
              className={`flex cursor-pointer items-center justify-between gap-3 rounded-xl border px-3.5 py-2.5 text-sm transition ${
                tipo === opcion
                  ? 'border-marca-500 bg-marca-50 text-marca-800 dark:border-marca-400 dark:bg-marca-900/40 dark:text-marca-100'
                  : 'border-slate-200 bg-white text-slate-700 hover:border-slate-300 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200 dark:hover:border-slate-600'
              }`}
            >
              <span className="flex items-center gap-2 font-semibold">
                <input
                  type="radio"
                  name="tipo-entrada"
                  value={opcion}
                  checked={tipo === opcion}
                  onChange={() => onTipo(opcion)}
                  className="size-4 accent-marca-600"
                />
                {etiquetaTipoTicket(opcion)}
              </span>
              <span className="text-xs text-slate-500 dark:text-slate-400">Disponible</span>
            </label>
          ))}
        </div>
      </fieldset>

      {motivoBloqueo && (
        <p className="mt-4 flex items-start gap-2 rounded-xl bg-aviso-50 p-3 text-sm text-aviso-800 dark:bg-aviso-700/20 dark:text-aviso-100">
          <IconoAlerta className="mt-0.5 size-4 shrink-0" />
          {motivoBloqueo}
        </p>
      )}

      <Boton
        className="mt-5 w-full"
        tamano="lg"
        onClick={autenticado ? onComprar : onIniciarSesion}
        disabled={autenticado && !puedeComprar}
      >
        {autenticado ? 'Comprar entrada' : 'Inicia sesión para comprar'}
      </Boton>
      <p className="mt-3 text-center text-xs text-slate-500 dark:text-slate-400">
        El precio lo define el sistema al confirmar.
      </p>
      <p className="sr-only">Evento: {evento.name}</p>
    </aside>
  )
}
