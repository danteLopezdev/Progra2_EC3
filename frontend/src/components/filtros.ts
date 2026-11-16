import type { Categoria } from '../types/Categoria'
import type { Sede } from '../types/Sede'
import type { FiltrosPublicaciones } from '../types/Publicacion'

function campo(etiqueta: string, control: HTMLElement): HTMLLabelElement {
  const label = document.createElement('label')
  label.append(`${etiqueta} `, control)
  return label
}

function crearSelect(
  nombre: string,
  opciones: { id: number; nombre: string }[],
): HTMLSelectElement {
  const select = document.createElement('select')
  select.name = nombre
  const todas = document.createElement('option')
  todas.value = ''
  todas.textContent = 'Todas'
  select.append(todas)
  for (const o of opciones) {
    const opcion = document.createElement('option')
    opcion.value = String(o.id)
    opcion.textContent = o.nombre
    select.append(opcion)
  }
  return select
}

/** Formulario de filtros. Llama a alBuscar con los valores al enviar o al limpiar. */
export function crearFiltros(
  sedes: Sede[],
  categorias: Categoria[],
  alBuscar: (filtros: FiltrosPublicaciones) => void,
): HTMLFormElement {
  const form = document.createElement('form')
  form.className = 'filtros'

  const q = document.createElement('input')
  q.type = 'search'
  q.name = 'q'
  q.placeholder = 'Buscar palabras clave'

  const sede = crearSelect('sede', sedes)
  const categoria = crearSelect('categoria', categorias)

  const desde = document.createElement('input')
  desde.type = 'date'
  desde.name = 'desde'
  const hasta = document.createElement('input')
  hasta.type = 'date'
  hasta.name = 'hasta'

  const buscar = document.createElement('button')
  buscar.type = 'submit'
  buscar.textContent = 'Buscar'
  const limpiar = document.createElement('button')
  limpiar.type = 'button'
  limpiar.textContent = 'Limpiar'

  const leer = (): FiltrosPublicaciones => ({
    q: q.value || undefined,
    sede: sede.value ? Number(sede.value) : undefined,
    categoria: categoria.value ? Number(categoria.value) : undefined,
    desde: desde.value || undefined,
    hasta: hasta.value || undefined,
  })

  form.addEventListener('submit', (e) => {
    e.preventDefault()
    alBuscar(leer())
  })
  limpiar.addEventListener('click', () => {
    form.reset()
    alBuscar({})
  })

  form.append(
    campo('Búsqueda', q),
    campo('Sede', sede),
    campo('Categoría', categoria),
    campo('Desde', desde),
    campo('Hasta', hasta),
    buscar,
    limpiar,
  )
  return form
}