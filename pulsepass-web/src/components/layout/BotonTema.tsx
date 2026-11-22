import { useEffect, useState } from 'react'
import { IconoLuna, IconoSol } from '../ui/Iconos'

const CLAVE_TEMA = 'pulsepass.tema'

function temaOscuroInicial(): boolean {
  try {
    const guardado = localStorage.getItem(CLAVE_TEMA)
    if (guardado === 'oscuro') return true
    if (guardado === 'claro') return false
  } catch {
    // preferencia del sistema
  }
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false
}

export function BotonTema() {
  const [oscuro, setOscuro] = useState(temaOscuroInicial)

  useEffect(() => {
    document.documentElement.classList.toggle('dark', oscuro)
    try {
      localStorage.setItem(CLAVE_TEMA, oscuro ? 'oscuro' : 'claro')
    } catch {
      // sin persistencia
    }
  }, [oscuro])

  return (
    <button
      type="button"
      onClick={() => setOscuro((actual) => !actual)}
      aria-label={oscuro ? 'Activar modo claro' : 'Activar modo oscuro'}
      aria-pressed={oscuro}
      title={oscuro ? 'Modo claro' : 'Modo oscuro'}
      className="rounded-full p-2 text-slate-600 transition hover:bg-slate-100 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-white"
    >
      {oscuro ? <IconoSol className="size-5" /> : <IconoLuna className="size-5" />}
    </button>
  )
}
