import type { Publicacion } from '../types/Publicacion'

/** Tarjeta de una publicación. Todo dato del servidor entra con textContent. */
export function crearTarjeta(p: Publicacion): HTMLElement {
  const tarjeta = document.createElement('article')
  tarjeta.className = 'tarjeta'

  const estado = document.createElement('span')
  estado.className = `estado estado-${p.estado.toLowerCase()}`
  estado.textContent = p.estado

  const titulo = document.createElement('h3')
  const enlace = document.createElement('a')
  enlace.href = `#/publicacion/${p.id}`
  enlace.textContent = p.titulo
  titulo.append(enlace)

  const descripcion = document.createElement('p')
  descripcion.className = 'descripcion'
  descripcion.textContent =
    p.descripcion.length > 140 ? `${p.descripcion.slice(0, 140)}…` : p.descripcion

  const meta = document.createElement('p')
  meta.className = 'meta'
  const fecha = new Date(p.fechaPublicacion).toLocaleDateString('es-PE')
  meta.textContent = `${p.sedeNombre} · ${p.categoriaNombre} · ${fecha} · por ${p.autorNombre}`

  tarjeta.append(estado, titulo, descripcion, meta)
  return tarjeta
}