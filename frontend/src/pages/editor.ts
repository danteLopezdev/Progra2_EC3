import { crearPublicacion, editarPublicacion, obtenerPublicacion } from '../api/publicaciones'
import { obtenerCategorias } from '../api/categorias'
import { obtenerSedes } from '../api/sedes'
import { crearFormularioPublicacion } from '../components/formularioPublicacion'
import { crearNavbar } from '../components/navbar'
import { el, mensajesDeError } from '../utils/dom'
import { obtenerSesion } from '../utils/sesion'

/**
 * Pantalla de crear (#/nueva) o editar (#/editar/{id}) una publicación.
 * Exige sesión. Si se edita una publicación ajena, el servidor responde 403
 * al enviar y el formulario muestra el mensaje.
 */
export async function mostrarEditor(contenedor: HTMLElement, id?: number): Promise<void> {
  if (!obtenerSesion()) {
    location.hash = '#/login'
    return
  }

  const raiz = el('div')
  contenedor.append(crearNavbar(), raiz)
  raiz.append(el('p', 'aviso', 'Cargando…'))

  try {
    const [sedes, categorias, existente] = await Promise.all([
      obtenerSedes(),
      obtenerCategorias(),
      id === undefined ? Promise.resolve(null) : obtenerPublicacion(id),
    ])

    const formulario = crearFormularioPublicacion(
      sedes,
      categorias,
      existente,
      id === undefined ? 'Publicar' : 'Guardar cambios',
      async (datos) => {
        const guardada =
          id === undefined ? await crearPublicacion(datos) : await editarPublicacion(id, datos)
        location.hash = `#/publicacion/${guardada.id}`
      },
    )

    const volver = el('a', 'volver', '← Cancelar')
    volver.href = id === undefined ? '#/' : `#/publicacion/${id}`

    raiz.replaceChildren(
      volver,
      el('h1', undefined, id === undefined ? 'Nueva publicación' : 'Editar publicación'),
      formulario,
    )
  } catch (error) {
    raiz.replaceChildren(el('p', 'aviso', mensajesDeError(error).join(' ')))
  }
}