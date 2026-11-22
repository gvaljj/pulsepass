import { createPortal } from 'react-dom'
import { useEffect, useId, useRef, type ReactNode } from 'react'
import { IconoCerrar } from './Iconos'

export interface ModalProps {
  abierto: boolean
  titulo: string
  descripcion?: string
  children: ReactNode
  pie?: ReactNode
  onCerrar: () => void
  cerrarAlHacerClicFuera?: boolean
}

const SELECTOR_ENFOCABLES =
  'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'

export function Modal({
  abierto,
  titulo,
  descripcion,
  children,
  pie,
  onCerrar,
  cerrarAlHacerClicFuera = true,
}: ModalProps) {
  const tituloId = useId()
  const descripcionId = useId()
  const panelRef = useRef<HTMLDivElement>(null)
  const enfocadoPrevio = useRef<HTMLElement | null>(null)

  useEffect(() => {
    if (!abierto) return

    enfocadoPrevio.current = document.activeElement as HTMLElement | null
    const overflowPrevio = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const panel = panelRef.current
    const primerEnfocable = panel?.querySelector<HTMLElement>(SELECTOR_ENFOCABLES)
    ;(primerEnfocable ?? panel)?.focus()

    function alPresionarTecla(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        evento.preventDefault()
        onCerrar()
        return
      }

      if (evento.key !== 'Tab' || !panelRef.current) return

      const enfocables = Array.from(
        panelRef.current.querySelectorAll<HTMLElement>(SELECTOR_ENFOCABLES),
      ).filter((elemento) => elemento.offsetParent !== null)

      if (enfocables.length === 0) {
        evento.preventDefault()
        panelRef.current.focus()
        return
      }

      const primero = enfocables[0]
      const ultimo = enfocables[enfocables.length - 1]
      if (!primero || !ultimo) return

      if (evento.shiftKey && document.activeElement === primero) {
        evento.preventDefault()
        ultimo.focus()
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault()
        primero.focus()
      }
    }

    document.addEventListener('keydown', alPresionarTecla)
    return () => {
      document.removeEventListener('keydown', alPresionarTecla)
      document.body.style.overflow = overflowPrevio
      enfocadoPrevio.current?.focus()
    }
  }, [abierto, onCerrar])

  if (!abierto) return null

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4 backdrop-blur-sm"
      onMouseDown={(evento) => {
        if (cerrarAlHacerClicFuera && evento.target === evento.currentTarget) onCerrar()
      }}
    >
      <div
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={tituloId}
        aria-describedby={descripcion ? descripcionId : undefined}
        tabIndex={-1}
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl bg-white p-6 shadow-2xl dark:bg-slate-900"
      >
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 id={tituloId} className="text-xl font-bold text-slate-900 dark:text-white">
              {titulo}
            </h2>
            {descripcion && (
              <p id={descripcionId} className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                {descripcion}
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={onCerrar}
            aria-label="Cerrar"
            className="rounded-full p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-800 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-white"
          >
            <IconoCerrar className="size-5" />
          </button>
        </div>
        <div className="mt-5">{children}</div>
        {pie && <div className="mt-6 flex flex-wrap justify-end gap-3">{pie}</div>}
      </div>
    </div>,
    document.body,
  )
}
