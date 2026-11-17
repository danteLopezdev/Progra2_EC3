import {
  descargarExportacion,
  listarReportes,
  resolverReporte,
  retirarPublicacion,
  type FormatoExportacion,
  type RecursoExportable,
} from '../api/admin'
import { crearNavbar } from '../components/navbar'
import type { EstadoReporte, Reporte } from '../types/Reporte'
import { el, mensajesDeError, mostrarMensajes } from '../utils/dom'
import { esAdministrador, obtenerSesion } from '../utils/sesion'

const ESTADOS: EstadoReporte[] = ['PENDIENTE', 'REVISADO', 'DESCARTADO']

/**
 * Panel de administración: reportes de moderación (filtrar, revisar, descartar,
 * retirar publicación) y exportación a PDF y Excel.
 * Ocultar la opción según el rol es solo comodidad: la autorización real la hace
 * el servidor (/api/admin/** responde 403 a un estudiante).
 */
export async function mostrarAdmin(contenedor: HTMLElement): Promise<void> {
  if (!obtenerSesion()) {
    location.hash = '#/login'
    return
  }
  if (!esAdministrador()) {
    location.hash = '#/'
    return
  }

  const raiz = el('div', 'pagina')
  contenedor.append(raiz)

  const errores = el('div', 'error')
  const lista = el('div', 'lista-reportes')
  const vacio = el('p', 'meta', 'No hay reportes con ese filtro.')
  vacio.hidden = true

  // ---- Exportación ----
  const exportar = el('section', 'exportar')
  exportar.append(el('h2', undefined, 'Exportar'))
  const crearBotonExport = (
    texto: string,
    recurso: RecursoExportable,
    formato: FormatoExportacion,
  ): HTMLButtonElement => {
    const boton = el('button', 'boton', texto)
    boton.type = 'button'
    boton.addEventListener('click', async () => {
      mostrarMensajes(errores, [])
      boton.disabled = true
      try {
        await descargarExportacion(recurso, formato)
      } catch (error) {
        mostrarMensajes(errores, mensajesDeError(error))
      } finally {
        boton.disabled = false
      }
    })
    return boton
  }
  exportar.append(
    crearBotonExport('Publicaciones (PDF)', 'publicaciones', 'pdf'),
    crearBotonExport('Publicaciones (Excel)', 'publicaciones', 'xlsx'),
    crearBotonExport('Reportes (PDF)', 'reportes', 'pdf'),
    crearBotonExport('Reportes (Excel)', 'reportes', 'xlsx'),
  )

  // ---- Filtro por estado ----
  const filtro = el('select')
  const todos = el('option', undefined, 'Todos los estados')
  todos.value = ''
  filtro.append(todos)
  for (const estado of ESTADOS) {
    const opcion = el('option', undefined, estado)
    opcion.value = estado
    filtro.append(opcion)
  }
  filtro.value = 'PENDIENTE'

  const barraFiltro = el('div', 'acciones')
  barraFiltro.append(el('label', undefined, 'Estado: '), filtro)

  raiz.append(
    crearNavbar(),
    el('h1', undefined, 'Panel de administración'),
    exportar,
    el('h2', undefined, 'Reportes de moderación'),
    barraFiltro,
    errores,
    lista,
    vacio,
  )

  // ---- Una fila de reporte ----
  const crearFila = (r: Reporte): HTMLElement => {
    const fila = el('article', 'tarjeta')

    const estado = el('span', `estado estado-${r.estado.toLowerCase()}`, r.estado)
    const titulo = el('h3')
    const enlace = el('a', undefined, r.publicacion.titulo)
    enlace.href = `#/publicacion/${r.publicacion.idPublicacion}`
    titulo.append(enlace)

    const motivo = el('p', 'descripcion', `Motivo: ${r.motivo}`)
    const fecha = new Date(r.fechaReporte).toLocaleString('es-PE')
    const meta = el(
      'p',
      'meta',
      `Reportado por ${r.reportadoPor} · ${fecha} · publicación de ${r.publicacion.autor}`,
    )

    const acciones = el('div', 'acciones')

    const marcar = (texto: string, nuevo: 'REVISADO' | 'DESCARTADO'): HTMLButtonElement => {
      const boton = el('button', 'boton', texto)
      boton.type = 'button'
      boton.disabled = r.estado === nuevo
      boton.addEventListener('click', async () => {
        mostrarMensajes(errores, [])
        boton.disabled = true
        try {
          await resolverReporte(r.idReporte, nuevo)
          await cargar() // si el filtro es PENDIENTE, el reporte sale de la lista
        } catch (error) {
          boton.disabled = false
          mostrarMensajes(errores, mensajesDeError(error))
        }
      })
      return boton
    }

    const retirar = el('button', 'boton-peligro', 'Retirar publicación')
    retirar.type = 'button'
    retirar.addEventListener('click', async () => {
      const ok = confirm(
        'Se eliminará la publicación y todos sus reportes. No se puede deshacer. ¿Continuar?',
      )
      if (!ok) return
      mostrarMensajes(errores, [])
      retirar.disabled = true
      try {
        await retirarPublicacion(r.publicacion.idPublicacion)
        await cargar() // los reportes de esa publicación ya no existen (cascada)
      } catch (error) {
        retirar.disabled = false
        mostrarMensajes(errores, mensajesDeError(error))
      }
    })

    acciones.append(marcar('Marcar revisado', 'REVISADO'), marcar('Descartar', 'DESCARTADO'), retirar)
    fila.append(estado, titulo, motivo, meta, acciones)
    return fila
  }

  // ---- Carga (el contador evita que una respuesta lenta pise a una más reciente) ----
  let peticion = 0
  async function cargar(): Promise<void> {
    const actual = ++peticion
    lista.replaceChildren(el('p', 'meta', 'Cargando…'))
    vacio.hidden = true
    try {
      const estado = filtro.value ? (filtro.value as EstadoReporte) : undefined
      const reportes = await listarReportes(estado)
      if (actual !== peticion) return
      lista.replaceChildren(...reportes.map(crearFila))
      vacio.hidden = reportes.length > 0
    } catch (error) {
      if (actual !== peticion) return
      lista.replaceChildren()
      mostrarMensajes(errores, mensajesDeError(error))
    }
  }

  filtro.addEventListener('change', () => {
    mostrarMensajes(errores, [])
    void cargar()
  })
  await cargar()
}