import { createBrowserRouter } from 'react-router'
import { Layout } from './components/layout/Layout'
import { RutaPorRol } from './components/layout/RutaPorRol'
import { RutaProtegida } from './components/layout/RutaProtegida'
import { SoloAnonimo } from './components/layout/SoloAnonimo'
import { ErrorPaginaLazy } from './pages/ErrorPaginaLazy'
import PaginaInicio from './features/eventos/PaginaInicio'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    errorElement: <ErrorPaginaLazy />,
    children: [
      { index: true, Component: PaginaInicio },
      {
        path: 'eventos',
        lazy: async () => ({
          Component: (await import('./features/eventos/PaginaCatalogoEventos')).default,
        }),
      },
      {
        path: 'eventos/:eventCode',
        lazy: async () => ({
          Component: (await import('./features/eventos/PaginaDetalleEvento')).default,
        }),
      },
      {
        path: 'artistas',
        lazy: async () => ({
          Component: (await import('./features/artistas/PaginaListaArtistas')).default,
        }),
      },
      {
        path: 'artistas/:id',
        lazy: async () => ({
          Component: (await import('./features/artistas/PaginaDetalleArtista')).default,
        }),
      },
      {
        path: 'sedes',
        lazy: async () => ({
          Component: (await import('./features/sedes/PaginaListaSedes')).default,
        }),
      },
      {
        path: 'sedes/:code',
        lazy: async () => ({
          Component: (await import('./features/sedes/PaginaDetalleSede')).default,
        }),
      },
      {
        element: <SoloAnonimo />,
        children: [
          {
            path: 'login',
            lazy: async () => ({
              Component: (await import('./features/auth/PaginaLogin')).default,
            }),
          },
          {
            path: 'registro',
            lazy: async () => ({
              Component: (await import('./features/auth/PaginaRegistro')).default,
            }),
          },
        ],
      },
      {
        element: <RutaProtegida />,
        children: [
          {
            path: 'cuenta',
            lazy: async () => ({
              Component: (await import('./features/auth/PaginaCuenta')).default,
            }),
          },
          {
            path: 'mis-tickets',
            lazy: async () => ({
              Component: (await import('./features/tickets/PaginaMisTickets')).default,
            }),
          },
          {
            path: 'mis-tickets/:ticketCode',
            lazy: async () => ({
              Component: (await import('./features/tickets/PaginaDetalleTicket')).default,
            }),
          },
          {
            path: 'compra/confirmacion/:ticketCode',
            lazy: async () => ({
              Component: (await import('./features/tickets/PaginaConfirmacionCompra')).default,
            }),
          },
        ],
      },
      {
        element: <RutaPorRol roles={['STAFF', 'ADMIN']} />,
        children: [
          {
            path: 'staff/validacion',
            lazy: async () => ({
              Component: (await import('./features/staff/PaginaValidacionIngreso')).default,
            }),
          },
          {
            path: 'staff/eventos/:eventCode/tickets',
            lazy: async () => ({
              Component: (await import('./features/staff/PaginaTicketsPagadosEvento')).default,
            }),
          },
        ],
      },
      {
        element: <RutaPorRol roles={['ADMIN']} />,
        children: [
          {
            path: 'admin/eventos',
            lazy: async () => ({
              Component: (await import('./features/admin/PaginaAdminEventos')).default,
            }),
          },
          {
            path: 'admin/eventos/nuevo',
            lazy: async () => ({
              Component: (await import('./features/admin/PaginaAdminCrearEvento')).default,
            }),
          },
          {
            path: 'admin/eventos/:eventCode',
            lazy: async () => ({
              Component: (await import('./features/admin/PaginaAdminGestionEvento')).default,
            }),
          },
        ],
      },
      {
        path: '403',
        lazy: async () => ({
          Component: (await import('./pages/SinPermisos')).default,
        }),
      },
      {
        path: '*',
        lazy: async () => ({
          Component: (await import('./pages/NoEncontrada')).default,
        }),
      },
    ],
  },
])