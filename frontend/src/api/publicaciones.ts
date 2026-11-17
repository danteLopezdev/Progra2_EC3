import type { FiltrosPublicaciones, Publicacion } from '../types/Publicacion'
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

import type { PublicacionRequest } from '../types/Publicacion'
// (súmalo al import de tipos que ya tienes arriba)

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