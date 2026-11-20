import type { Categoria } from '../types/Categoria'
import type { Publicacion, PublicacionRequest } from '../types/Publicacion'
import type { Sede } from '../types/Sede'
import { el, mensajesDeError, mostrarMensajes } from '../utils/dom'
import { quitarImagen, urlImagen } from '../api/publicaciones'

const MAX_BYTES_IMAGEN = 2 * 1024 * 1024 // comodidad; la garantía real es el servidor

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
 * @param alEnviar acción a ejecutar con los datos y la imagen elegida (o null);
 *                 si lanza, el error se muestra en el formulario
 */
export function crearFormularioPublicacion(
  sedes: Sede[],
  categorias: Categoria[],
  inicial: Publicacion | null,
  textoBoton: string,
  alEnviar: (datos: PublicacionRequest, imagen: File | null) => Promise<void>,
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

  const entradaImagen = el('input')
  entradaImagen.name = 'imagen'
  entradaImagen.type = 'file'
  entradaImagen.accept = 'image/jpeg,image/png,image/webp'
    // Imagen actual (solo al editar una publicación que ya tiene imagen)
  const bloqueActual = el('div', 'imagen-actual')
  if (inicial?.imagen) {
    const previa = el('img', 'imagen-publicacion')
    previa.src = urlImagen(inicial.imagen)
    previa.alt = 'Imagen actual de la publicación' // propiedad, no HTML

    const botonQuitar = el('button', 'boton-peligro', 'Quitar imagen')
    botonQuitar.type = 'button' // para que NO envíe el formulario
    botonQuitar.addEventListener('click', async () => {
      if (!confirm('¿Quitar la imagen de esta publicación?')) return
      botonQuitar.disabled = true
      try {
        await quitarImagen(inicial.id)
        bloqueActual.replaceChildren(el('p', 'aviso', 'Imagen eliminada.'))
      } catch (error) {
        mostrarMensajes(errores, mensajesDeError(error))
        botonQuitar.disabled = false
      }
    })
    bloqueActual.append(previa, botonQuitar)
  }

  const errores = el('div', 'errores')
  errores.setAttribute('role', 'alert')

  const boton = el('button', undefined, textoBoton)
  boton.type = 'submit'

  form.append(
  campo('Título', titulo),          // igual que antes
  campo('Descripción', descripcion),
  campo('Sede', sede),
  campo('Categoría', categoria),
  bloqueActual,                     // NUEVO: imagen actual + botón "Quitar imagen"
                                    // (vacío si la publicación no tiene imagen o es nueva)
  campo(
    // la etiqueta depende de si ya hay imagen
    inicial?.imagen ? 'Reemplazar imagen (opcional, máx. 2 MB)' : 'Imagen (opcional, máx. 2 MB)',
    entradaImagen,                  // el input de archivo
  ),
  errores,                          // igual que antes
  boton,                            // igual que antes
)

  form.addEventListener('submit', async (evento) => {
    evento.preventDefault()
    mostrarMensajes(errores, [])

    const archivo = entradaImagen.files?.[0] ?? null
    if (archivo && archivo.size > MAX_BYTES_IMAGEN) {
      mostrarMensajes(errores, ['La imagen no puede superar los 2 MB'])
      return
    }

    boton.disabled = true
    try {
      await alEnviar(
        {
          titulo: titulo.value.trim(),
          descripcion: descripcion.value.trim(),
          idSede: sede.value ? Number(sede.value) : null,
          idCategoria: categoria.value ? Number(categoria.value) : null,
        },
        archivo,
      )
    } catch (error) {
      mostrarMensajes(errores, mensajesDeError(error)) // 400 con detalles, 403 o 404
      boton.disabled = false
    }
    // Si alEnviar tuvo éxito la página navega a otra pantalla: el botón no se reactiva.
  })

  return form
}