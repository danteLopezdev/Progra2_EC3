import type { EstadoReporte, Reporte } from '../types/Reporte'
import { errorDesdeRespuesta } from './ApiError'
import { fetchAutenticado } from './cliente'

/** Lista los reportes de moderación, con filtro opcional por estado. */
export async function listarReportes(estado?: EstadoReporte): Promise<Reporte[]> {
  const consulta = estado ? `?${new URLSearchParams({ estado })}` : ''
  const respuesta = await fetchAutenticado(`/api/admin/reportes${consulta}`)
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
  return (await respuesta.json()) as Reporte[]
}

/** Marca un reporte como REVISADO o DESCARTADO. 200, o 400/404 como ApiError. */
export async function resolverReporte(
  id: number,
  estado: Exclude<EstadoReporte, 'PENDIENTE'>,
): Promise<Reporte> {
  const respuesta = await fetchAutenticado(`/api/admin/reportes/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ estado }),
  })
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
  return (await respuesta.json()) as Reporte
}

/** Retira (elimina) una publicación. Sus reportes se borran en cascada. 204. */
export async function retirarPublicacion(id: number): Promise<void> {
  const respuesta = await fetchAutenticado(`/api/admin/publicaciones/${id}`, { method: 'DELETE' })
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)
}

export type RecursoExportable = 'publicaciones' | 'reportes'
export type FormatoExportacion = 'pdf' | 'xlsx'

/**
 * Descarga una exportación. Un enlace simple no envía el header Authorization,
 * por eso se pide con fetchAutenticado, se recibe como blob y se guarda desde JS.
 * El nombre del archivo lo fija el cliente con valores de una lista cerrada
 * (nunca se toma de la respuesta del servidor).
 */
export async function descargarExportacion(
  recurso: RecursoExportable,
  formato: FormatoExportacion,
): Promise<void> {
  const respuesta = await fetchAutenticado(`/api/admin/exportar/${recurso}?formato=${formato}`)
  if (!respuesta.ok) throw await errorDesdeRespuesta(respuesta)

  const blob = await respuesta.blob()
  const url = URL.createObjectURL(blob)
  const enlace = document.createElement('a')
  enlace.href = url
  enlace.download = `${recurso}.${formato}`
  document.body.append(enlace)
  enlace.click()
  enlace.remove()
  URL.revokeObjectURL(url)
}