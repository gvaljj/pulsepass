import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router'
import { AuthProvider } from './features/auth/AuthContext'
import { AvisoProvider } from './components/ui/Aviso'
import { queryClient } from './lib/queryClient'
import { router } from './router'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <AvisoProvider>
          <RouterProvider router={router} />
        </AvisoProvider>
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
)