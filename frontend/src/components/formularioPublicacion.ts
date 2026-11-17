import type { Categoria } from '../types/Categoria'
import type { Publicacion, PublicacionRequest } from '../types/Publicacion'
import type { Sede } from '../types/Sede'
import { el, mensajesDeError, mostrarMensajes } from '../utils/dom'

function crearSelect(
  nombre: string,
  opciones: { id: number; nombre: string }[],
  seleccionado: number | null,
): HTMLSelectElement {
  const select = el('select')
  select.name = nombre
  const vacia = el('option', undefined, 'Selecciona…')
  vacia.value = ''
  select.append(vacia)
  for (const o of opciones) {
    const opcion = el('option', undefined, o.nombre)
    opcion.value = String(o.id)
    select.append(opcion)
  }
  select.value = seleccionado === null ? '' : String(seleccionado)
  return select
}

function campo(etiqueta: string, control: HTMLElement): HTMLLabelElement {
  const label = el('label')
  label.append(`${etiqueta} `, control)
  return label
}

/**
 * Formulario compartido por "crear" y "editar".
 * @param inicial publicación a precargar (edición) o null (creación)
 * @param alEnviar acción a ejecutar con los datos; si lanza, el error se muestra en el formulario
 */
export function crearFormularioPublicacion(
  sedes: Sede[],
  categorias: Categoria[],
  inicial: Publicacion | null,
  textoBoton: string,
  alEnviar: (datos: PublicacionRequest) => Promise<void>,
): HTMLFormElement {
  const form = el('form', 'formulario-publicacion')
  form.noValidate = true // el error que se muestra es el del servidor

  const titulo = el('input')
  titulo.name = 'titulo'
  titulo.type = 'text'
  titulo.maxLength = 150 // comodidad; la validación real es la del servidor
  titulo.value = inicial?.titulo ?? ''

  const descripcion = el('textarea')
  descripcion.name = 'descripcion'
  descripcion.rows = 6
  descripcion.maxLength = 2000
  descripcion.value = inicial?.descripcion ?? ''

  const sede = crearSelect('sede', sedes, inicial?.sedeId ?? null)
  const categoria = crearSelect('categoria', categorias, inicial?.categoriaId ?? null)

  const errores = el('div', 'errores')
  errores.setAttribute('role', 'alert')

  const boton = el('button', undefined, textoBoton)
  boton.type = 'submit'

  form.append(
    campo('Título', titulo),
    campo('Descripción', descripcion),
    campo('Sede', sede),
    campo('Categoría', categoria),
    errores,
    boton,
  )

  form.addEventListener('submit', async (evento) => {
    evento.preventDefault()
    mostrarMensajes(errores, [])
    boton.disabled = true
    try {
      await alEnviar({
        titulo: titulo.value.trim(),
        descripcion: descripcion.value.trim(),
        idSede: sede.value ? Number(sede.value) : null,
        idCategoria: categoria.value ? Number(categoria.value) : null,
      })
    } catch (error) {
      mostrarMensajes(errores, mensajesDeError(error)) // 400 con detalles, 403 o 404
      boton.disabled = false
    }
    // Si alEnviar tuvo éxito la página navega a otra pantalla: el botón no se reactiva.
  })

  return form
}