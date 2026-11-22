import { Link } from 'react-router'
import { useAuth } from './AuthContext'
import { useUsuarioPorEmail } from './hooks'
import { RUTAS } from '../../lib/rutas'
import { formatearFechaCorta } from '../../lib/formato'
import { AvatarArtista } from '../../components/catalogo/AvatarArtista'
import { Boton } from '../../components/ui/Boton'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { Badge } from '../../components/ui/Badge'
import { IconoSalir, IconoTicket } from '../../components/ui/Iconos'

export default function PaginaCuenta() {
  const { sesion, cerrarSesion } = useAuth()
  const email = sesion.estado === 'autenticado' ? sesion.usuario.email : null
  const perfilQuery = useUsuarioPorEmail(email)
  const perfil = perfilQuery.data ?? null

  if (sesion.estado === 'cargando') {
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <Cargando filas={2} mensaje="Cargando cuenta…" />
      </div>
    )
  }

  if (sesion.estado !== 'autenticado') {
    return null
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6">
      <header>
        <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
          Perfil
        </p>
        <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Mi cuenta
        </h1>
      </header>

      <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        {perfilQuery.isLoading ? (
          <Cargando filas={2} mensaje="Cargando perfil…" />
        ) : perfilQuery.isError ? (
          <EstadoError
            mensaje="No pudimos cargar tu perfil."
            onReintentar={() => void perfilQuery.refetch()}
          />
        ) : perfil ? (
          <div className="flex flex-col gap-6 sm:flex-row sm:items-start">
            <AvatarArtista
              nombre={`${perfil.firstName} ${perfil.lastName}`}
              tamano="xl"
            />
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
                {perfil.firstName} {perfil.lastName}
              </h2>
              <p className="text-sm text-slate-500 dark:text-slate-400">@{sesion.usuario.username}</p>

              <dl className="mt-5 grid gap-4 sm:grid-cols-2">
                <div>
                  <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                    Correo
                  </dt>
                  <dd className="mt-1 text-sm text-slate-800 dark:text-slate-100">{perfil.email}</dd>
                </div>
                <div>
                  <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                    Fecha de nacimiento
                  </dt>
                  <dd className="mt-1 text-sm text-slate-800 dark:text-slate-100">
                    {formatearFechaCorta(perfil.birthDate)}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                    Ciudad
                  </dt>
                  <dd className="mt-1 text-sm text-slate-800 dark:text-slate-100">
                    {perfil.city || 'No registrada'}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs font-bold tracking-wide text-slate-500 uppercase dark:text-slate-400">
                    Teléfono
                  </dt>
                  <dd className="mt-1 text-sm text-slate-800 dark:text-slate-100">
                    {perfil.phone || 'No registrado'}
                  </dd>
                </div>
              </dl>

              <div className="mt-5 flex flex-wrap gap-2">
                {sesion.usuario.roles.map((rol) => (
                  <Badge key={rol} tono="marca">
                    {rol}
                  </Badge>
                ))}
                <Badge tono={perfil.active ? 'exito' : 'neutral'}>
                  {perfil.active ? 'Activa' : 'Inactiva'}
                </Badge>
              </div>
            </div>
          </div>
        ) : (
          <p className="text-sm text-slate-500 dark:text-slate-400">No encontramos tu perfil.</p>
        )}
      </section>

      <div className="mt-6 flex flex-wrap gap-3">
        {sesion.usuario.roles.includes('USER') && (
          <Link to={RUTAS.misTickets}>
            <Boton variante="secundario">
              <IconoTicket className="size-4" />
              Ver mis tickets
            </Boton>
          </Link>
        )}
        <Boton variante="peligro" onClick={() => cerrarSesion('manual')}>
          <IconoSalir className="size-4" />
          Cerrar sesión
        </Boton>
      </div>
    </div>
  )
}