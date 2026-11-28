import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import axe from 'axe-core'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import PaginaCatalogoEventos from './PaginaCatalogoEventos'

describe('Accesibilidad de la cartelera', () => {
  it('no tiene violaciones axe-core', async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })

    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <PaginaCatalogoEventos />
        </MemoryRouter>
      </QueryClientProvider>,
    )

    await new Promise((resolve) => setTimeout(resolve, 50))
    const results = await axe.run(container)
    expect(results.violations).toEqual([])
  })
})