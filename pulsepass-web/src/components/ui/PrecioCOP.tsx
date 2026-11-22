import { precioCOP } from '../../lib/formato'

export interface PrecioCOPProps {
  valor: number | string
  className?: string
}

export function PrecioCOP({ valor, className }: PrecioCOPProps) {
  return <span className={className}>{precioCOP(valor)}</span>
}
