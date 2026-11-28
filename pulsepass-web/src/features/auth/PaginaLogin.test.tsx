import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { AuthProvider } from './AuthContext'
import { AvisoProvider } from '../../components/ui/Aviso'
import { server } from '../../test/server'
import PaginaLogin from './PaginaLogin'

function renderLogin() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <AvisoProvider>
          <MemoryRouter initialEntries={['/login']}>
            <Routes>
              <Route path="/login" element={<PaginaLogin />} />
              <Route path="/" element={<div>Inicio</div>} />
            </Routes>
          </MemoryRouter>
        </AvisoProvider>
      </AuthProvider>
    </QueryClientProvider>,
  )
}

describe('PaginaLogin', () => {
  it('muestra error cuando las credenciales son inválidas', async () => {
    server.use(
      http.post('/api/auth/login', () =>
        HttpResponse.json({ status: 401, message: 'Credenciales inválidas.' }, { status: 401 }),
      ),
    )

    const user = userEvent.setup()
    renderLogin()

    await user.type(screen.getByLabelText('Usuario o correo'), 'andrea')
    await user.type(screen.getByLabelText('Contraseña'), 'incorrecta')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(await screen.findByText('Credenciales inválidas.')).toBeInTheDocument()
  })

  it('inicia sesión y navega al inicio', async () => {
    const user = userEvent.setup()
    renderLogin()

    await user.type(screen.getByLabelText('Usuario o correo'), 'andrea')
    await user.type(screen.getByLabelText('Contraseña'), 'Password123!')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    await waitFor(() => {
      expect(screen.getByText('Inicio')).toBeInTheDocument()
    })
  })
})