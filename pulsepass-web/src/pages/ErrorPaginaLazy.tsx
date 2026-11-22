import { lazy, Suspense } from 'react'
import { CargandoPagina } from '../components/layout/CargandoPagina'

const ErrorPagina = lazy(() => import('./ErrorPagina'))

export function ErrorPaginaLazy() {
  return (
    <Suspense fallback={<CargandoPagina />}>
      <ErrorPagina />
    </Suspense>
  )
}
