import type { ReactNode, SVGProps } from 'react'

type IconoProps = SVGProps<SVGSVGElement> & { children: ReactNode }

function IconoBase({ children, className, ...props }: IconoProps) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className={className ?? 'size-5'}
      {...props}
    >
      {children}
    </svg>
  )
}

export function IconoChevronAbajo(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="m6 9 6 6 6-6" />
    </IconoBase>
  )
}

export function IconoChevronIzquierda(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="m15 18-6-6 6-6" />
    </IconoBase>
  )
}

export function IconoChevronDerecha(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="m9 18 6-6-6-6" />
    </IconoBase>
  )
}

export function IconoMenu(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M4 7h16M4 12h16M4 17h16" />
    </IconoBase>
  )
}

export function IconoCerrar(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="m6 6 12 12M18 6 6 18" />
    </IconoBase>
  )
}

export function IconoSol(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.66 6.34l1.41-1.41" />
    </IconoBase>
  )
}

export function IconoLuna(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M21 12.8A8.5 8.5 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z" />
    </IconoBase>
  )
}

export function IconoBuscar(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" />
    </IconoBase>
  )
}

export function IconoCalendario(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M6 3v3M18 3v3M4 8h16M5 5h14a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1Z" />
      <path d="M8 12h.01M12 12h.01M16 12h.01M8 16h.01M12 16h.01" />
    </IconoBase>
  )
}

export function IconoUbicacion(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" />
      <circle cx="12" cy="10" r="2.5" />
    </IconoBase>
  )
}

export function IconoUsuario(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21a8 8 0 0 1 16 0" />
    </IconoBase>
  )
}

export function IconoTicket(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M4 8a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v2a2 2 0 0 0 0 4v2a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-2a2 2 0 0 0 0-4V8Z" />
      <path d="M13 6v2M13 11v2M13 16v2" />
    </IconoBase>
  )
}

export function IconoCheck(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="m5 12 4 4L19 6" />
    </IconoBase>
  )
}

export function IconoAlerta(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M12 3 2.5 20h19L12 3Z" />
      <path d="M12 9v5M12 17h.01" />
    </IconoBase>
  )
}

export function IconoMusica(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M9 18V5l10-2v13" />
      <circle cx="6" cy="18" r="3" />
      <circle cx="16" cy="16" r="3" />
    </IconoBase>
  )
}

export function IconoTecnologia(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <rect x="7" y="7" width="10" height="10" rx="2" />
      <path d="M9 2v3M15 2v3M9 19v3M15 19v3M2 9h3M2 15h3M19 9h3M19 15h3" />
    </IconoBase>
  )
}

export function IconoDeporte(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 3a14 14 0 0 0 0 18M3.5 9a15 15 0 0 0 17 0M3.5 15a15 15 0 0 1 17 0" />
    </IconoBase>
  )
}

export function IconoCultura(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M4 21V8l8-5 8 5v13" />
      <path d="M9 21v-6h6v6M8 10h.01M12 10h.01M16 10h.01" />
    </IconoBase>
  )
}

export function IconoEducacion(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="m2 9 10-5 10 5-10 5L2 9Z" />
      <path d="M6 11v5c0 1.5 2.7 3 6 3s6-1.5 6-3v-5M22 9v6" />
    </IconoBase>
  )
}

export function IconoEntretenimiento(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="m12 3 1.8 4.2L18 9l-4.2 1.8L12 15l-1.8-4.2L6 9l4.2-1.8L12 3Z" />
      <path d="m19 15 .9 2.1L22 18l-2.1.9L19 21l-.9-2.1L16 18l2.1-.9L19 15ZM5 14l.7 1.3L7 16l-1.3.7L5 18l-.7-1.3L3 16l1.3-.7L5 14Z" />
    </IconoBase>
  )
}

export function IconoEditar(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5Z" />
    </IconoBase>
  )
}

export function IconoBasura(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M4 7h16M10 11v6M14 11v6M6 7l1 14h10l1-14M9 7V4h6v3" />
    </IconoBase>
  )
}

export function IconoOjo(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" />
      <circle cx="12" cy="12" r="2.5" />
    </IconoBase>
  )
}

export function IconoOjoCerrado(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="m3 3 18 18M10.6 6.2A10.4 10.4 0 0 1 12 6c6.5 0 10 6 10 6a17 17 0 0 1-3 3.6M6.3 6.3C3.5 8.2 2 12 2 12s3.5 6 10 6a10.5 10.5 0 0 0 4.1-.8" />
      <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
    </IconoBase>
  )
}

export function IconoSalir(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M10 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h5M14 8l4 4-4 4M8 12h10" />
    </IconoBase>
  )
}

export function IconoMas(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M12 5v14M5 12h14" />
    </IconoBase>
  )
}

export function IconoReloj(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </IconoBase>
  )
}

export function IconoActualizar(props: SVGProps<SVGSVGElement>) {
  return (
    <IconoBase {...props}>
      <path d="M20 11a8 8 0 0 0-14.9-4M4 4v5h5M4 13a8 8 0 0 0 14.9 4M20 20v-5h-5" />
    </IconoBase>
  )
}
