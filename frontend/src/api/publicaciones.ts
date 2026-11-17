import type { EstadoPublicacion, FiltrosPublicaciones, Publicacion, PublicacionRequest } from '../types/Publicacion'
import { errorDesdeRespuesta } from './ApiError'
import { fetchAutenticado } from './cliente'

/**
 * Lista publicaciones con filtros opcionales.
 * Es una ruta pública: se usa fetch normal (sin token), así un token vencido
 * no redirige al login solo por mirar el listado.
 */
export async function listarPublicaciones(
  filtros: FiltrosPublicaciones = {},
): Promise<Publicacion[]> {
  const params = new URLSearchParams()
  if (filtros.sede) params.set('sede', String(filtros.sede))
  if (filtros.categoria) params.set('categoria', String(filtros.categoria))
  if (filtros.desde) params.set('desde', filtros.desde)
  if (filtros.hasta) params.set('hasta', filtros.hasta)
  if (filtros.q?.trim()) params.set('q', filtros.q.trim())

  const consulta = params.toString()
  const respuesta = await fetch(`/api/publicaciones${consulta ? `?${consulta}` : ''}`)
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
  return (await respuesta.json()) as Publicacion[]
}

/** Detalle de una publicación. Ruta pública: 404 si no existe. */
export async function obtenerPublicacion(id: number): Promise<Publicacion> {
  const respuesta = await fetch(`/api/publicaciones/${id}`)
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
  return (await respuesta.json()) as Publicacion
}

/** Reporta una publicación (requiere sesión). 201, o 400/404/409 como ApiError. */
export async function reportarPublicacion(id: number, motivo: string): Promise<void> {
  const respuesta = await fetchAutenticado(`/api/publicaciones/${id}/reportes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ motivo }),
  })
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
}


/** Crea una publicación (requiere sesión). 201, o 400/404 como ApiError. */
export async function crearPublicacion(datos: PublicacionRequest): Promise<Publicacion> {
  const respuesta = await fetchAutenticado('/api/publicaciones', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos),
  })
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
  return (await respuesta.json()) as Publicacion
}

/** Edita una publicación propia. 200, o 400/403/404 como ApiError. */
export async function editarPublicacion(id: number, datos: PublicacionRequest): Promise<Publicacion> {
  const respuesta = await fetchAutenticado(`/api/publicaciones/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos),
  })
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
  return (await respuesta.json()) as Publicacion
}

/** Publicaciones del usuario autenticado (más recientes primero). */
export async function listarMisPublicaciones(): Promise<Publicacion[]> {
  const respuesta = await fetchAutenticado('/api/publicaciones/mias')
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
  return (await respuesta.json()) as Publicacion[]
}

/** Cambia el estado de una publicación propia. 200, o 400/403/404 como ApiError. */
export async function cambiarEstadoPublicacion(
  id: number,
  estado: EstadoPublicacion,
): Promise<Publicacion> {
  const respuesta = await fetchAutenticado(`/api/publicaciones/${id}/estado`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ estado }),
  })
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
  return (await respuesta.json()) as Publicacion
}

/** Elimina una publicación propia. 204 sin cuerpo, o 403/404 como ApiError. */
export async function eliminarPublicacion(id: number): Promise<void> {
  const respuesta = await fetchAutenticado(`/api/publicaciones/${id}`, { method: 'DELETE' })
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
}