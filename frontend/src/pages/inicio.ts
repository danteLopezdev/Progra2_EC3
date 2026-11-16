import { listarPublicaciones } from '../api/publicaciones'
import { obtenerCategorias } from '../api/categorias'
import { obtenerSedes } from '../api/sedes'
import { crearFiltros } from '../components/filtros'
import { crearNavbar } from '../components/navbar'
import { crearTarjeta } from '../components/tarjetapublicacion'
import type { FiltrosPublicaciones } from '../types/Publicacion'
import { mensajesDeError } from '../utils/dom'

/** Pantalla de inicio: listado público de publicaciones con filtros y búsqueda. */
export async function mostrarInicio(contenedor: HTMLElement): Promise<void> {
  const titulo = document.createElement('h1')
  titulo.textContent = 'Publicaciones universitarias'

  const resultados = document.createElement('section')
  resultados.className = 'resultados'

  contenedor.append(crearNavbar(), titulo)

  let peticion = 0 // evita que una respuesta lenta pise a una más reciente

  async function buscar(filtros: FiltrosPublicaciones): Promise<void> {
    const actual = ++peticion
    resultados.replaceChildren(aviso('Cargando…'))
    try {
      const lista = await listarPublicaciones(filtros)
      if (actual !== peticion) return
      if (lista.length === 0) {
        resultados.replaceChildren(aviso('No se encontraron publicaciones.'))
        return
      }
      resultados.replaceChildren(...lista.map(crearTarjeta))
    } catch (error) {
      if (actual !== peticion) return
      resultados.replaceChildren(aviso(mensajesDeError(error).join(' ')))
    }
  }

  try {
    const [sedes, categorias] = await Promise.all([obtenerSedes(), obtenerCategorias()])
    contenedor.append(crearFiltros(sedes, categorias, buscar), resultados)
  } catch (error) {
    contenedor.append(aviso(mensajesDeError(error).join(' ')))
    return
  }
  await buscar({})
}

function aviso(texto: string): HTMLParagraphElement {
  const p = document.createElement('p')
  p.className = 'aviso'
  p.textContent = texto
  return p
}