import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { AuthProvider } from '../auth/AuthContext'
import { AvisoProvider } from '../../components/ui/Aviso'
import { guardarToken } from '../../lib/token'
import { server } from '../../test/server'
import PaginaValidacionIngreso from './PaginaValidacionIngreso'

describe('PaginaValidacionIngreso', () => {
  it('busca un ticket y muestra sus datos', async () => {
    guardarToken('token-staff', 3600)
    server.use(
      http.get('/api/auth/me', () =>
        HttpResponse.json({
          id: 2,
          username: 'staff',
          email: 'staff@pulsepass.com',
          roles: ['STAFF'],
          active: true,
        }),
      ),
    )

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    const user = userEvent.setup()

    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <AvisoProvider>
            <MemoryRouter initialEntries={['/staff/validacion']}>
              <Routes>
                <Route path="/staff/validacion" element={<PaginaValidacionIngreso />} />
              </Routes>
            </MemoryRouter>
          </AvisoProvider>
        </AuthProvider>
      </QueryClientProvider>,
    )

    await user.type(screen.getByPlaceholderText('Código de ticket'), 'TKT-001')
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    expect(await screen.findByText('Noche de Jazz y Cultura')).toBeInTheDocument()
    expect(screen.getByText('andrea@pulsepass.com')).toBeInTheDocument()
  })
})