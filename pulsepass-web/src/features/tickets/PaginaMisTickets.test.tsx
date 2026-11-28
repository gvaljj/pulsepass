import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { AuthProvider } from '../auth/AuthContext'
import { AvisoProvider } from '../../components/ui/Aviso'
import { guardarToken } from '../../lib/token'
import PaginaMisTickets from './PaginaMisTickets'

describe('PaginaMisTickets', () => {
  it('lista tickets del usuario y permite filtrar', async () => {
    guardarToken('token-de-prueba', 3600)

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    const user = userEvent.setup()

    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <AvisoProvider>
            <MemoryRouter initialEntries={['/mis-tickets']}>
              <Routes>
                <Route path="/mis-tickets" element={<PaginaMisTickets />} />
              </Routes>
            </MemoryRouter>
          </AvisoProvider>
        </AuthProvider>
      </QueryClientProvider>,
    )

    expect(await screen.findByText('Noche de Jazz y Cultura')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Cancelados' }))
    expect(screen.getByText('Sin tickets en este estado')).toBeInTheDocument()
  })
})