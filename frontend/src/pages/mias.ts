import {
  cambiarEstadoPublicacion,
  eliminarPublicacion,
  listarMisPublicaciones,
} from '../api/publicaciones'
import { crearNavbar } from '../components/navbar'
import { crearTarjeta } from '../components/tarjetapublicacion'
import type { EstadoPublicacion, Publicacion } from '../types/Publicacion'
import { el, mensajesDeError, mostrarMensajes } from '../utils/dom'
import { obtenerSesion } from '../utils/sesion'

const ESTADOS: EstadoPublicacion[] = ['VIGENTE', 'ATENDIDA', 'FINALIZADA']

/** Pantalla "Mis publicaciones": cambiar estado, editar y eliminar las propias. */
export async function mostrarMias(contenedor: HTMLElement): Promise<void> {
  if (!obtenerSesion()) {
    location.hash = '#/login'
    return
  }

  // div raíz propio, anexado de inmediato: si el usuario navega antes de que
  // llegue la respuesta, lo que se pinta después queda en un nodo ya descartado.
  const raiz = el('div', 'pagina')
  contenedor.append(raiz)

  const errores = el('div', 'error')
  const lista = el('div', 'grilla')
  const vacio = el('p', 'meta', 'Aún no tienes publicaciones.')
  vacio.hidden = true
  raiz.append(
    crearNavbar(),
    el('h1', undefined, 'Mis publicaciones'),
    errores,
    lista,
    vacio,
  )

  /** Revisa si quedan elementos para mostrar u ocultar el mensaje de lista vacía. */
  const actualizarVacio = (): void => {
    vacio.hidden = lista.children.length > 0
  }

  /** Tarjeta + acciones (estado, editar, eliminar) de una publicación. */
  const crearItem = (p: Publicacion): HTMLElement => {
    const item = el('div', 'item-mia')
    item.append(crearTarjeta(p))

    const acciones = el('div', 'acciones')

    const selector = el('select')
    for (const estado of ESTADOS) {
      const opcion = el('option', undefined, estado)
      opcion.value = estado
      opcion.selected = estado === p.estado
      selector.append(opcion)
    }
    selector.addEventListener('change', async () => {
      mostrarMensajes(errores, [])
      selector.disabled = true
      try {
        const actualizada = await cambiarEstadoPublicacion(p.id, selector.value as EstadoPublicacion)
        item.replaceWith(crearItem(actualizada)) // refresca el badge de la tarjeta
      } catch (error) {
        selector.value = p.estado // vuelve al valor anterior
        selector.disabled = false
        mostrarMensajes(errores, mensajesDeError(error))
      }
    })

    const editar = el('a', 'boton', 'Editar')
    editar.href = `#/editar/${p.id}`

    const eliminar = el('button', 'boton-peligro', 'Eliminar')
    eliminar.type = 'button'
    eliminar.addEventListener('click', async () => {
      if (!confirm('¿Eliminar esta publicación? No se puede deshacer.')) return
      mostrarMensajes(errores, [])
      eliminar.disabled = true
      try {
        await eliminarPublicacion(p.id)
        item.remove()
        actualizarVacio()
      } catch (error) {
        eliminar.disabled = false
        mostrarMensajes(errores, mensajesDeError(error))
      }
    })

    acciones.append(selector, editar, eliminar)
    item.append(acciones)
    return item
  }

  lista.append(el('p', 'meta', 'Cargando…'))
  try {
    const publicaciones = await listarMisPublicaciones()
    lista.replaceChildren(...publicaciones.map(crearItem))
    actualizarVacio()
  } catch (error) {
    lista.replaceChildren()
    mostrarMensajes(errores, mensajesDeError(error))
  }
}