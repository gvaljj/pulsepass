import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { IconoAlerta, IconoCheck, IconoCerrar } from './Iconos'

export type TonoAviso = 'exito' | 'error' | 'info' | 'aviso'

interface Aviso {
  id: number
  mensaje: string
  tono: TonoAviso
}

interface AvisoContextValue {
  mostrarAviso: (mensaje: string, tono?: TonoAviso) => void
}

const AvisoContext = createContext<AvisoContextValue | null>(null)

const clasesTono: Record<TonoAviso, string> = {
  exito:
    'border-exito-100 bg-white text-exito-800 dark:border-exito-700/40 dark:bg-slate-900 dark:text-exito-100',
  error:
    'border-peligro-100 bg-white text-peligro-800 dark:border-peligro-700/40 dark:bg-slate-900 dark:text-peligro-100',
  info: 'border-info-100 bg-white text-info-800 dark:border-info-700/40 dark:bg-slate-900 dark:text-info-100',
  aviso:
    'border-aviso-100 bg-white text-aviso-800 dark:border-aviso-700/40 dark:bg-slate-900 dark:text-aviso-100',
}

export function AvisoProvider({ children }: { children: ReactNode }) {
  const [avisos, setAvisos] = useState<Aviso[]>([])
  const siguienteId = useRef(0)
  const temporizadores = useRef(new Map<number, number>())

  const cerrarAviso = useCallback((id: number) => {
    const temporizador = temporizadores.current.get(id)
    if (temporizador) window.clearTimeout(temporizador)
    temporizadores.current.delete(id)
    setAvisos((actuales) => actuales.filter((aviso) => aviso.id !== id))
  }, [])

  const mostrarAviso = useCallback(
    (mensaje: string, tono: TonoAviso = 'exito') => {
      const id = siguienteId.current++
      setAvisos((actuales) => [...actuales.slice(-2), { id, mensaje, tono }])

      if (tono !== 'error') {
        const temporizador = window.setTimeout(() => cerrarAviso(id), 4500)
        temporizadores.current.set(id, temporizador)
      }
    },
    [cerrarAviso],
  )

  useEffect(() => {
    const pendientes = temporizadores.current
    return () => {
      pendientes.forEach((temporizador) => window.clearTimeout(temporizador))
      pendientes.clear()
    }
  }, [])

  const value = useMemo(() => ({ mostrarAviso }), [mostrarAviso])

  return (
    <AvisoContext.Provider value={value}>
      {children}
      <div
        aria-live="polite"
        aria-atomic="false"
        className="pointer-events-none fixed right-4 bottom-4 z-[60] flex w-[calc(100%-2rem)] max-w-sm flex-col gap-2"
      >
        {avisos.map((aviso) => (
          <div
            key={aviso.id}
            role={aviso.tono === 'error' ? 'alert' : 'status'}
            className={`pointer-events-auto flex items-start gap-3 rounded-xl border p-4 text-sm shadow-lg ${clasesTono[aviso.tono]}`}
          >
            {aviso.tono === 'exito' ? (
              <IconoCheck className="mt-0.5 size-5 shrink-0" />
            ) : (
              <IconoAlerta className="mt-0.5 size-5 shrink-0" />
            )}
            <p className="flex-1">{aviso.mensaje}</p>
            <button
              type="button"
              onClick={() => cerrarAviso(aviso.id)}
              aria-label="Cerrar aviso"
              className="rounded-full p-1 transition hover:bg-black/5 dark:hover:bg-white/10"
            >
              <IconoCerrar className="size-4" />
            </button>
          </div>
        ))}
      </div>
    </AvisoContext.Provider>
  )
}

export function useAviso(): AvisoContextValue {
  const contexto = useContext(AvisoContext)
  if (!contexto) throw new Error('useAviso debe usarse dentro de AvisoProvider')
  return contexto
}
