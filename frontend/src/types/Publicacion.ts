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

/** Cuerpo de POST y PUT /api/publicaciones. Autor, estado y fecha los decide el servidor. */
export interface PublicacionRequest {
  titulo: string
  descripcion: string
  idSede: number | null // null si no eligió: el servidor responde 400 "La sede es obligatoria"
  idCategoria: number | null
}