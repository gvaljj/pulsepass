import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import axe from 'axe-core'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { AuthProvider } from '../auth/AuthContext'
import { AvisoProvider } from '../../components/ui/Aviso'
import PaginaInicio from './PaginaInicio'
import PaginaLogin from '../auth/PaginaLogin'

describe('Accesibilidad de páginas principales', () => {
  it('inicio no tiene violaciones axe-core', async () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <AvisoProvider>
            <MemoryRouter>
              <PaginaInicio />
            </MemoryRouter>
          </AvisoProvider>
        </AuthProvider>
      </QueryClientProvider>,
    )
    await new Promise((resolve) => setTimeout(resolve, 100))
    const results = await axe.run(container)
    expect(results.violations).toEqual([])
  })

  it('login no tiene violaciones axe-core', async () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <AvisoProvider>
            <MemoryRouter>
              <Routes>
                <Route path="/" element={<PaginaLogin />} />
              </Routes>
            </MemoryRouter>
          </AvisoProvider>
        </AuthProvider>
      </QueryClientProvider>,
    )
    const results = await axe.run(container)
    expect(results.violations).toEqual([])
  })
})