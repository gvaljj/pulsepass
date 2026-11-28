import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { PrecioCOP } from './PrecioCOP'

describe('PrecioCOP', () => {
  it('formatea pesos colombianos sin decimales', () => {
    render(<PrecioCOP valor={250000} />)
    expect(screen.getByText('$ 250.000')).toBeInTheDocument()
  })
})