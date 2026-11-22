import { useMemo } from 'react'
import { useNavigate } from 'react-router'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ApiError, mensajeDeError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useSedesActivas } from '../../features/sedes/hooks'
import { useCrearEvento } from '../../features/eventos/hooks'
import { CATEGORIAS_EVENTO, CreateEventRequestSchema, type CreateEventRequest } from '../../features/eventos/tipos'
import { useAviso } from '../../components/ui/Aviso'
import { guardarEventoCreado } from './sesionEventos'
import { Boton } from '../../components/ui/Boton'
import { Campo } from '../../components/ui/Campo'
import { CampoTextoLargo } from '../../components/ui/Campo'
import { Cargando } from '../../components/ui/Cargando'
import { EstadoError } from '../../components/ui/EstadoError'
import { Selector } from '../../components/ui/Selector'
import { etiquetaCategoria } from '../../components/ui/etiquetasFunciones'

export default function PaginaAdminCrearEvento() {
  const navigate = useNavigate()
  const { mostrarAviso } = useAviso()
  const sedesQuery = useSedesActivas()
  const crear = useCrearEvento()

  const {
    register,
    handleSubmit,
    setError,
    control,
    formState: { errors, isSubmitting },
  } = useForm<CreateEventRequest>({
    resolver: zodResolver(CreateEventRequestSchema) as never,
    defaultValues: {
      eventCode: '',
      name: '',
      description: '',
      category: 'MUSIC',
      eventDate: '',
      minimumAge: 0,
      venueCode: '',
    },
  })

  const descripcion = useWatch({ control, name: 'description' }) ?? ''
  const categoria = useWatch({ control, name: 'category' })
  const sede = useWatch({ control, name: 'venueCode' })
  const sedes = useMemo(() => sedesQuery.data ?? [], [sedesQuery.data])

  const opcionesSedes = useMemo(
    () => sedes.map((sede) => ({ valor: sede.code, etiqueta: `${sede.name} (${sede.city})` })),
    [sedes],
  )

  async function onSubmit(values: CreateEventRequest) {
    try {
      const payload: CreateEventRequest = {
        ...values,
        description: values.description?.trim() ? values.description.trim() : undefined,
      }
      const evento = await crear.mutateAsync(payload)
      guardarEventoCreado({
        eventCode: evento.eventCode,
        name: evento.name,
        status: evento.status,
      })
      mostrarAviso('Evento creado correctamente.')
      navigate(RUTAS.adminEvento(evento.eventCode))
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.status === 409) {
          setError('root', { message: 'Ya existe un evento con ese código.' })
        } else if (error.status === 400 && error.details) {
          Object.entries(error.details).forEach(([campo, mensaje]) => {
            if (
              campo === 'eventCode' ||
              campo === 'name' ||
              campo === 'description' ||
              campo === 'category' ||
              campo === 'eventDate' ||
              campo === 'minimumAge' ||
              campo === 'venueCode'
            ) {
              setError(campo as keyof CreateEventRequest, { message: mensaje })
            } else {
              setError('root', { message: mensaje })
            }
          })
        } else if (error.status === 403) {
          setError('root', { message: 'No tienes permisos para crear eventos.' })
        } else {
          setError('root', { message: mensajeDeError(error, 'No pudimos crear el evento.') })
        }
      } else {
        setError('root', { message: mensajeDeError(error, 'No pudimos crear el evento.') })
      }
    }
  }

  if (sedesQuery.isLoading) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <Cargando filas={2} mensaje="Cargando sedes…" />
      </div>
    )
  }

  if (sedesQuery.isError) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <EstadoError
          mensaje="No pudimos cargar las sedes activas."
          onReintentar={() => void sedesQuery.refetch()}
        />
      </div>
    )
  }

  const cargando = isSubmitting || crear.isPending

  return (
    <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6">
      <header>
        <p className="text-xs font-bold tracking-widest text-marca-600 uppercase dark:text-marca-300">
          Administración
        </p>
        <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Crear evento
        </h1>
      </header>

      <form className="mt-6 space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo
            etiqueta="Código"
            placeholder="JAZZ-2026"
            error={errors.eventCode?.message}
            {...register('eventCode')}
          />
          <Campo
            etiqueta="Nombre"
            placeholder="Noche de Jazz y Cultura"
            error={errors.name?.message}
            {...register('name')}
          />
        </div>

        <CampoTextoLargo
          etiqueta="Descripción"
          placeholder="Describe el evento…"
          maxLength={1000}
          ayuda={`${descripcion.length}/1000 caracteres`}
          error={errors.description?.message}
          {...register('description')}
        />

        <div className="grid gap-4 sm:grid-cols-2">
          <Selector
            etiqueta="Categoría"
            value={categoria}
            onChange={(evento) => register('category').onChange(evento)}
            opciones={CATEGORIAS_EVENTO.map((valor) => ({
              valor,
              etiqueta: etiquetaCategoria(valor),
            }))}
            error={errors.category?.message}
          />
          <Selector
            etiqueta="Sede"
            placeholder="Selecciona una sede"
            value={sede}
            onChange={(evento) => register('venueCode').onChange(evento)}
            opciones={opcionesSedes}
            error={errors.venueCode?.message}
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Campo
            etiqueta="Fecha y hora"
            type="datetime-local"
            error={errors.eventDate?.message}
            {...register('eventDate')}
          />
          <Campo
            etiqueta="Edad mínima"
            type="number"
            min={0}
            error={errors.minimumAge?.message}
            {...register('minimumAge', { valueAsNumber: true })}
          />
        </div>

        {errors.root?.message && (
          <p role="alert" className="rounded-xl bg-peligro-50 p-3 text-sm text-peligro-800 dark:bg-peligro-700/20 dark:text-peligro-100">
            {errors.root.message}
          </p>
        )}

        {sedes.length === 0 && (
          <p className="rounded-xl bg-aviso-50 p-3 text-sm text-aviso-800 dark:bg-aviso-700/20 dark:text-aviso-100">
            No hay sedes activas disponibles.
          </p>
        )}

        <div className="flex flex-wrap justify-end gap-3">
          <Boton type="submit" tamano="lg" cargando={cargando} disabled={sedes.length === 0}>
            Crear evento
          </Boton>
        </div>
      </form>
    </div>
  )
}
