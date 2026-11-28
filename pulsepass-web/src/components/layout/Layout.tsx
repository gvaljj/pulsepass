import { Outlet, ScrollRestoration } from 'react-router'
import { Footer } from './Footer'
import { Navbar } from './Navbar'
import { SessionWatcher } from './SessionWatcher'

export function Layout() {
  return (
    <div className="flex min-h-screen flex-col">
      <a
        href="#contenido"
        className="sr-only z-50 rounded-lg bg-marca-600 px-4 py-2 text-sm font-semibold text-white focus:not-sr-only focus:absolute focus:top-3 focus:left-3"
      >
        Saltar al contenido
      </a>
      <Navbar />
      <main id="contenido" className="flex-1">
        <Outlet />
      </main>
      <Footer />
      <SessionWatcher />
      <ScrollRestoration />
    </div>
  )
}
