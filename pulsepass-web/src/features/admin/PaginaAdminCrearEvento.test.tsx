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
import PaginaAdminCrearEvento from './PaginaAdminCrearEvento'

describe('PaginaAdminCrearEvento', () => {
  it('crea un evento con los datos del formulario', async () => {
    guardarToken('token-admin', 3600)
    server.use(
      http.get('/api/auth/me', () =>
        HttpResponse.json({
          id: 2,
          username: 'admin',
          email: 'admin@pulsepass.com',
          roles: ['ADMIN'],
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
            <MemoryRouter initialEntries={['/admin/eventos/nuevo']}>
              <Routes>
                <Route path="/admin/eventos/nuevo" element={<PaginaAdminCrearEvento />} />
                <Route path="/admin/eventos/:eventCode" element={<div>Gestión del evento</div>} />
              </Routes>
            </MemoryRouter>
          </AvisoProvider>
        </AuthProvider>
      </QueryClientProvider>,
    )

    await screen.findByLabelText('Código')
    await user.type(screen.getByLabelText('Código'), 'EVT-TEST')
    await user.type(screen.getByLabelText('Nombre'), 'Evento de prueba')
    await user.selectOptions(screen.getByLabelText('Categoría'), 'MUSIC')
    await user.selectOptions(screen.getByLabelText('Sede'), 'TEATRO-MAIN')
    await user.type(screen.getByLabelText('Fecha y hora'), '2026-12-25T20:00')
    await user.type(screen.getByLabelText('Edad mínima'), '18')
    await user.click(screen.getByRole('button', { name: 'Crear evento' }))

    expect(await screen.findByText('Gestión del evento')).toBeInTheDocument()
  })
})