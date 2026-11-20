import { obtenerPublicacion, reportarPublicacion, urlImagen } from '../api/publicaciones'
import { crearNavbar } from '../components/navbar'
import type { Publicacion } from '../types/Publicacion'
import { mensajesDeError, mostrarMensajes } from '../utils/dom'
import { obtenerSesion } from '../utils/sesion'

/** Crea un elemento con clase y texto (siempre textContent, nunca innerHTML). */
function el<K extends keyof HTMLElementTagNameMap>(
  etiqueta: K,
  clase?: string,
  texto?: string,
): HTMLElementTagNameMap[K] {
  const nodo = document.createElement(etiqueta)
  if (clase) nodo.className = clase
  if (texto !== undefined) nodo.textContent = texto
  return nodo
}

/** Pantalla de detalle (#/publicacion/{id}). Pública; el reporte exige sesión. */
export async function mostrarDetalle(contenedor: HTMLElement, id: number): Promise<void> {
  // Raíz propia: si el usuario navega a otra pantalla antes de que llegue la
  // respuesta, lo que se pinte después cae en un nodo ya desmontado.
  const raiz = el('div')
  contenedor.append(crearNavbar(), raiz)
  raiz.append(el('p', 'aviso', 'Cargando…'))

  try {
    const publicacion = await obtenerPublicacion(id)
    raiz.replaceChildren(crearContenido(publicacion))
  } catch (error) {
    raiz.replaceChildren(el('p', 'aviso', mensajesDeError(error).join(' ')), crearVolver())
  }
}

function crearVolver(): HTMLAnchorElement {
  const volver = el('a', 'volver', '← Volver al listado')
  volver.href = '#/'
  return volver
}

function crearContenido(p: Publicacion): HTMLElement {
  const articulo = el('article', 'detalle')

  const estado = el('span', `estado estado-${p.estado.toLowerCase()}`, p.estado)
  const titulo = el('h1', undefined, p.titulo)
  const fecha = new Date(p.fechaPublicacion).toLocaleString('es-PE')
  const meta = el('p', 'meta', `${p.sedeNombre} · ${p.categoriaNombre} · ${fecha} · por ${p.autorNombre}`)
  const descripcion = el('p', 'descripcion-completa', p.descripcion)

  articulo.append(crearVolver(), estado, titulo)

  if (p.imagen) {
    const img = el('img', 'imagen-detalle')
    img.src = urlImagen(p.imagen)
    img.alt = p.titulo // propiedad, no HTML
    articulo.append(img)
  }

  articulo.append(meta, descripcion, crearSeccionReporte(p.id))
  return articulo
}

function crearSeccionReporte(idPublicacion: number): HTMLElement {
  const seccion = el('section', 'reporte')
  seccion.append(el('h2', undefined, 'Reportar publicación'))

  if (!obtenerSesion()) {
    const enlace = el('a', undefined, 'Inicia sesión')
    enlace.href = '#/login'
    const parrafo = el('p')
    parrafo.append(enlace, ' para reportar esta publicación.')
    seccion.append(parrafo)
    return seccion
  }

  const form = el('form')
  form.noValidate = true // el error que se muestra es el del servidor
  const etiqueta = el('label', undefined, 'Motivo ')
  const motivo = el('textarea')
  motivo.name = 'motivo'
  motivo.rows = 3
  motivo.maxLength = 255 // comodidad; la validación real es la del servidor
  etiqueta.append(motivo)
  const errores = el('div', 'errores')
  errores.setAttribute('role', 'alert')
  const boton = el('button', undefined, 'Enviar reporte')
  boton.type = 'submit'
  form.append(etiqueta, errores, boton)

  form.addEventListener('submit', async (evento) => {
    evento.preventDefault()
    mostrarMensajes(errores, [])
    boton.disabled = true
    try {
      await reportarPublicacion(idPublicacion, motivo.value.trim())
      seccion.replaceChildren(
        el('h2', undefined, 'Reportar publicación'),
        el('p', 'exito', 'Gracias. Tu reporte fue enviado y un administrador lo revisará.'),
      )
    } catch (error) {
      mostrarMensajes(errores, mensajesDeError(error)) // 400, 404 o 409 ("Ya reportaste...")
      boton.disabled = false
    }
  })

  seccion.append(form)
  return seccion
}