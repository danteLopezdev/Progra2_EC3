import { obtenerCategorias } from '../api/categorias'
import { listarPublicaciones } from '../api/publicaciones'
import { obtenerSedes } from '../api/sedes'
import { crearFiltros } from '../components/filtros'
import { crearNavbar } from '../components/navbar'
import { crearTarjeta } from '../components/tarjetaPublicacion'
import type { FiltrosPublicaciones } from '../types/Publicacion'
import { el, mensajesDeError } from '../utils/dom'

/** Pantalla de inicio: listado público de publicaciones con filtros y búsqueda. */
export async function mostrarInicio(contenedor: HTMLElement): Promise<void> {
  // div raíz propio, anexado de inmediato: todo lo que se pinte después de un
  // await va aquí dentro, no en `contenedor`, que puede haber cambiado de pantalla.
  const raiz = el('div', 'pagina')
  contenedor.append(raiz)

  const resultados = el('section', 'resultados')
  raiz.append(crearNavbar(), el('h1', undefined, 'Publicaciones universitarias'))

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

  const cargando = aviso('Cargando…')
  raiz.append(cargando)
  try {
    const [sedes, categorias] = await Promise.all([obtenerSedes(), obtenerCategorias()])
    cargando.remove()
    raiz.append(crearFiltros(sedes, categorias, buscar), resultados)
  } catch (error) {
    cargando.replaceWith(aviso(mensajesDeError(error).join(' ')))
    return
  }
  await buscar({})
}

function aviso(texto: string): HTMLParagraphElement {
  return el('p', 'aviso', texto)
}