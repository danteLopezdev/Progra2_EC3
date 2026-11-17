export type EstadoReporte = 'PENDIENTE' | 'REVISADO' | 'DESCARTADO'

/** Resumen de la publicación reportada (no es el DTO completo de publicación). */
export interface PublicacionReportada {
  idPublicacion: number
  titulo: string
  estado: string
  autor: string
}

/** Refleja ReporteResponse del backend campo por campo. */
export interface Reporte {
  idReporte: number
  motivo: string
  estado: EstadoReporte
  fechaReporte: string
  reportadoPor: string
  publicacion: PublicacionReportada
}