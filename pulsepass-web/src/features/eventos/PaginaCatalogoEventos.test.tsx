import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import PaginaCatalogoEventos from './PaginaCatalogoEventos'

describe('PaginaCatalogoEventos', () => {
  it('muestra eventos publicados enriquecidos con sedes', async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <PaginaCatalogoEventos />
        </MemoryRouter>
      </QueryClientProvider>,
    )

    expect(await screen.findByText('Noche de Jazz')).toBeInTheDocument()
    expect(screen.getByText('Cartagena')).toBeInTheDocument()
  })
})