# PulsePass Web

Frontend de PulsePass construido con React 19, TypeScript estricto, Vite, Tailwind CSS v4 y TanStack Query.

## Requisitos

- Node.js `>=22.22.0`
- npm `>=10`
- Backend PulsePass ejecutándose en `http://localhost:8080` o configurado mediante `VITE_API_BASE_URL`

## Instalación

```bash
npm install
```

## Variables de entorno

Crea un archivo `.env` si necesitas apuntar a otro backend:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

Si no se define, el frontend usa `/api` y el proxy de Vite redirige al backend local.

## Scripts

```bash
npm run dev        # servidor de desarrollo
npm run build      # typecheck + build de producción
npm run lint       # ESLint
npm run test       # Vitest
npm run test:watch # Vitest en modo watch
```

## Estructura principal

- `src/components/ui`: componentes base accesibles.
- `src/components/layout`: layout raíz, navbar, guardas y estados de carga.
- `src/components/catalogo`: tarjetas y componentes de catálogo.
- `src/features`: páginas y hooks por dominio (`eventos`, `artistas`, `sedes`, `auth`, `tickets`, `staff`, `admin`).
- `src/lib`: cliente HTTP, token, rutas, formato y Query Client.
- `src/test`: infraestructura MSW y tests.

## Rutas principales

- `/`: inicio
- `/eventos`: catálogo
- `/eventos/:eventCode`: detalle y compra
- `/artistas`, `/artistas/:id`
- `/sedes`, `/sedes/:code`
- `/login`, `/registro`, `/cuenta`
- `/mis-tickets`, `/mis-tickets/:ticketCode`
- `/compra/confirmacion/:ticketCode`
- `/staff/validacion`, `/staff/eventos/:eventCode/tickets`
- `/admin/eventos`, `/admin/eventos/nuevo`, `/admin/eventos/:eventCode`

## Accesibilidad

El proyecto usa `axe-core` en desarrollo y pruebas automatizadas para detectar violaciones básicas de accesibilidad. La navegación por teclado, foco visible y contraste se validan manualmente sobre las vistas principales.