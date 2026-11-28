import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { AuthProvider } from '../auth/AuthContext'
import { AvisoProvider } from '../../components/ui/Aviso'
import PaginaDetalleEvento from './PaginaDetalleEvento'

describe('PaginaDetalleEvento', () => {
  it('muestra el detalle del evento y el panel de compra para anónimos', async () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })

    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <AvisoProvider>
            <MemoryRouter initialEntries={['/eventos/JAZZ-2026']}>
              <Routes>
                <Route path="/eventos/:eventCode" element={<PaginaDetalleEvento />} />
              </Routes>
            </MemoryRouter>
          </AvisoProvider>
        </AuthProvider>
      </QueryClientProvider>,
    )

    expect(await screen.findByText('Noche de Jazz y Cultura')).toBeInTheDocument()
    expect(screen.getAllByText('Inicia sesión para comprar')).toHaveLength(2)
  })
})