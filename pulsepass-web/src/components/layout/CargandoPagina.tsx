import { Cargando } from '../ui/Cargando'

export function CargandoPagina() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
      <Cargando filas={3} />
    </div>
  )
}
