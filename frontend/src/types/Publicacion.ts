/** Publicación tal como la devuelve el backend (PublicacionResponse). */
export type EstadoPublicacion = 'VIGENTE' | 'ATENDIDA' | 'FINALIZADA'

export interface Publicacion {
  id: number
  titulo: string
  descripcion: string
  imagen: string | null
  fechaPublicacion: string // ISO, por ejemplo "2026-11-10T09:00:00"
  estado: EstadoPublicacion
  categoriaId: number
  categoriaNombre: string
  sedeId: number
  sedeNombre: string
  autorNombre: string
}

/** Filtros opcionales del listado: GET /api/publicaciones */
export interface FiltrosPublicaciones {
  sede?: number
  categoria?: number
  desde?: string // AAAA-MM-DD
  hasta?: string // AAAA-MM-DD
  q?: string
}