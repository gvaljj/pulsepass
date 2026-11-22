import { Boton } from './Boton'
import { IconoAlerta, IconoActualizar } from './Iconos'

export interface EstadoErrorProps {
  mensaje: string
  titulo?: string
  onReintentar?: () => void
}

export function EstadoError({
  mensaje,
  titulo = 'Algo salió mal',
  onReintentar,
}: EstadoErrorProps) {
  return (
    <div
      role="alert"
      className="rounded-2xl border border-peligro-100 bg-peligro-50 p-6 dark:border-peligro-700/40 dark:bg-peligro-700/10"
    >
      <div className="flex items-start gap-4">
        <div className="rounded-full bg-peligro-100 p-3 text-peligro-700 dark:bg-peligro-700/30 dark:text-peligro-100">
          <IconoAlerta className="size-6" />
        </div>
        <div className="flex-1">
          <h2 className="text-lg font-bold text-peligro-800 dark:text-peligro-100">{titulo}</h2>
          <p className="mt-1 text-sm text-peligro-700 dark:text-peligro-100">{mensaje}</p>
          {onReintentar && (
            <Boton variante="secundario" tamano="sm" className="mt-4" onClick={onReintentar}>
              <IconoActualizar className="size-4" />
              Reintentar
            </Boton>
          )}
        </div>
      </div>
    </div>
  )
}
