import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ApiError, mensajeDeError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useLogin } from './hooks'
import { LoginRequestSchema, type LoginRequest } from './tipos'
import { Logo } from '../../components/layout/Logo'
import { Boton } from '../../components/ui/Boton'
import { Campo } from '../../components/ui/Campo'
import { IconoOjo, IconoOjoCerrado } from '../../components/ui/Iconos'

function destinoSeguro(valor: unknown): string {
  if (typeof valor !== 'string') return RUTAS.inicio
  if (!valor.startsWith('/') || valor.startsWith('//')) return RUTAS.inicio
  return valor
}

export default function PaginaLogin() {
  const location = useLocation()
  const navigate = useNavigate()
  const login = useLogin()
  const [mostrarPassword, setMostrarPassword] = useState(false)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<LoginRequest>({
    resolver: zodResolver(LoginRequestSchema),
    defaultValues: { identifier: '', password: '' },
  })

  const state = location.state as { desde?: string; motivo?: string; registrado?: boolean } | null

  async function onSubmit(values: LoginRequest) {
    try {
      await login.mutateAsync(values)
      navigate(destinoSeguro(state?.desde), { replace: true })
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.status === 400 && error.details) {
          Object.entries(error.details).forEach(([campo, mensaje]) => {
            if (campo === 'identifier' || campo === 'password') {
              setError(campo, { message: mensaje })
            } else {
              setError('root', { message: mensaje })
            }
          })
        } else if (error.status === 401) {
          setError('root', { message: 'Credenciales inválidas.' })
        } else {
          setError('root', { message: mensajeDeError(error, 'No pudimos iniciar sesión.') })
        }
      } else {
        setError('root', { message: mensajeDeError(error, 'No pudimos iniciar sesión.') })
      }
    }
  }

  const cargando = isSubmitting || login.isPending

  return (
    <div className="mx-auto flex min-h-[calc(100vh-8rem)] max-w-md items-center px-4 py-12">
      <div className="w-full rounded-2xl border border-slate-200 bg-white p-7 shadow-xl dark:border-slate-800 dark:bg-slate-900">
        <div className="flex flex-col items-center text-center">
          <Logo />
          <h1 className="mt-5 text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">
            Inicia sesión
          </h1>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Accede a tu cuenta PulsePass.
          </p>
        </div>

        {state?.motivo === 'expirada' && (
          <p role="alert" className="mt-5 rounded-xl bg-aviso-50 p-3 text-sm text-aviso-800 dark:bg-aviso-700/20 dark:text-aviso-100">
            Tu sesión expiró. Inicia sesión de nuevo.
          </p>
        )}
        {state?.registrado && (
          <p role="status" className="mt-5 rounded-xl bg-exito-50 p-3 text-sm text-exito-800 dark:bg-exito-700/20 dark:text-exito-100">
            Cuenta creada. Inicia sesión para continuar.
          </p>
        )}

        <form className="mt-6 space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
          <Campo
            etiqueta="Usuario o correo"
            placeholder="andrea"
            autoComplete="username"
            error={errors.identifier?.message}
            {...register('identifier')}
          />

          <Campo
            etiqueta="Contraseña"
            type={mostrarPassword ? 'text' : 'password'}
            placeholder="••••••••••••"
            autoComplete="current-password"
            error={errors.password?.message}
            {...register('password')}
            sufijo={
              <button
                type="button"
                onClick={() => setMostrarPassword((actual) => !actual)}
                aria-label={mostrarPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                className="rounded-full p-1 text-slate-400 transition hover:text-slate-700 dark:hover:text-slate-200"
              >
                {mostrarPassword ? (
                  <IconoOjoCerrado className="size-5" />
                ) : (
                  <IconoOjo className="size-5" />
                )}
              </button>
            }
          />

          {errors.root?.message && (
            <p role="alert" className="rounded-xl bg-peligro-50 p-3 text-sm text-peligro-800 dark:bg-peligro-700/20 dark:text-peligro-100">
              {errors.root.message}
            </p>
          )}

          <Boton type="submit" className="w-full" tamano="lg" cargando={cargando}>
            Ingresar
          </Boton>
        </form>

        <p className="mt-5 text-center text-sm text-slate-500 dark:text-slate-400">
          ¿No tienes cuenta?{' '}
          <Link to={RUTAS.registro} className="font-bold text-marca-700 hover:text-marca-800 dark:text-marca-300 dark:hover:text-marca-200">
            Regístrate
          </Link>
        </p>
      </div>
    </div>
  )
}