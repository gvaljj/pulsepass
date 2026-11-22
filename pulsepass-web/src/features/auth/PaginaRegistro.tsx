import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ApiError, mensajeDeError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useRegistrarUsuario } from './hooks'
import { RegisterUserRequestSchema, type RegisterUserRequest } from './tipos'
import { Logo } from '../../components/layout/Logo'
import { Boton } from '../../components/ui/Boton'
import { Campo } from '../../components/ui/Campo'
import { IconoOjo, IconoOjoCerrado } from '../../components/ui/Iconos'

function fuerzaPassword(valor: string): { nivel: number; etiqueta: string } {
  let nivel = 0
  if (valor.length >= 12) nivel += 1
  if (/[a-z]/.test(valor) && /[A-Z]/.test(valor)) nivel += 1
  if (/\d/.test(valor)) nivel += 1
  if (/[^A-Za-z0-9]/.test(valor)) nivel += 1
  if (valor.length >= 16) nivel += 1
  const normalizado = Math.min(4, nivel)
  const etiquetas = ['Muy débil', 'Débil', 'Aceptable', 'Fuerte', 'Muy fuerte']
  return { nivel: normalizado, etiqueta: etiquetas[normalizado] ?? 'Muy débil' }
}

export default function PaginaRegistro() {
  const navigate = useNavigate()
  const registrar = useRegistrarUsuario()
  const [mostrarPassword, setMostrarPassword] = useState(false)

  const {
    register,
    handleSubmit,
    setError,
    control,
    formState: { errors, isSubmitting },
  } = useForm<RegisterUserRequest>({
    resolver: zodResolver(RegisterUserRequestSchema),
    defaultValues: {
      username: '',
      email: '',
      firstName: '',
      lastName: '',
      birthDate: '',
      password: '',
    },
  })

  const password = useWatch({ control, name: 'password' }) ?? ''
  const fuerza = useMemo(() => fuerzaPassword(password ?? ''), [password])

  async function onSubmit(values: RegisterUserRequest) {
    try {
      await registrar.mutateAsync(values)
      navigate(RUTAS.login, { replace: true, state: { registrado: true } })
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.status === 409) {
          setError('root', { message: 'El usuario o correo ya está registrado.' })
        } else if (error.status === 400 && error.details) {
          Object.entries(error.details).forEach(([campo, mensaje]) => {
            if (
              campo === 'username' ||
              campo === 'email' ||
              campo === 'firstName' ||
              campo === 'lastName' ||
              campo === 'birthDate' ||
              campo === 'password'
            ) {
              setError(campo as keyof RegisterUserRequest, { message: mensaje })
            } else {
              setError('root', { message: mensaje })
            }
          })
        } else {
          setError('root', { message: mensajeDeError(error, 'No pudimos crear la cuenta.') })
        }
      } else {
        setError('root', { message: mensajeDeError(error, 'No pudimos crear la cuenta.') })
      }
    }
  }

  const cargando = isSubmitting || registrar.isPending

  return (
    <div className="mx-auto flex min-h-[calc(100vh-8rem)] max-w-2xl items-center px-4 py-12">
      <div className="w-full rounded-2xl border border-slate-200 bg-white p-7 shadow-xl dark:border-slate-800 dark:bg-slate-900">
        <div className="flex flex-col items-center text-center">
          <Logo />
          <h1 className="mt-5 text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">
            Crear cuenta
          </h1>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Únete a PulsePass y compra tus entradas.
          </p>
        </div>

        <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(onSubmit)} noValidate>
          <Campo
            etiqueta="Nombre de usuario"
            placeholder="andrea"
            autoComplete="username"
            error={errors.username?.message}
            {...register('username')}
          />
          <Campo
            etiqueta="Correo electrónico"
            type="email"
            placeholder="andrea@pulsepass.com"
            autoComplete="email"
            error={errors.email?.message}
            {...register('email')}
          />
          <Campo
            etiqueta="Nombre"
            placeholder="Andrea"
            autoComplete="given-name"
            error={errors.firstName?.message}
            {...register('firstName')}
          />
          <Campo
            etiqueta="Apellido"
            placeholder="Ruiz"
            autoComplete="family-name"
            error={errors.lastName?.message}
            {...register('lastName')}
          />
          <Campo
            etiqueta="Fecha de nacimiento"
            type="date"
            max={new Date().toISOString().slice(0, 10)}
            error={errors.birthDate?.message}
            {...register('birthDate')}
          />
          <Campo
            etiqueta="Contraseña"
            type={mostrarPassword ? 'text' : 'password'}
            placeholder="Mínimo 12 caracteres"
            autoComplete="new-password"
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

          <div className="sm:col-span-2">
            <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
              <span>Fuerza: {fuerza.etiqueta}</span>
              <span>{new TextEncoder().encode(password ?? '').length}/72 bytes</span>
            </div>
            <div className="mt-1.5 h-2 overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
              <div
                className="h-full rounded-full bg-gradient-to-r from-peligro-500 via-aviso-500 to-exito-500 transition-all"
                style={{ width: `${(fuerza.nivel / 4) * 100}%` }}
              />
            </div>
          </div>

          {errors.root?.message && (
            <p role="alert" className="sm:col-span-2 rounded-xl bg-peligro-50 p-3 text-sm text-peligro-800 dark:bg-peligro-700/20 dark:text-peligro-100">
              {errors.root.message}
            </p>
          )}

          <div className="sm:col-span-2">
            <Boton type="submit" className="w-full" tamano="lg" cargando={cargando}>
              Crear cuenta
            </Boton>
          </div>
        </form>

        <p className="mt-5 text-center text-sm text-slate-500 dark:text-slate-400">
          ¿Ya tienes cuenta?{' '}
          <Link to={RUTAS.login} className="font-bold text-marca-700 hover:text-marca-800 dark:text-marca-300 dark:hover:text-marca-200">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  )
}
