import type { FiltrosPublicaciones, Publicacion } from '../types/Publicacion'
import { errorDesdeRespuesta } from './ApiError'

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