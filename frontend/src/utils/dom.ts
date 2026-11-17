import { ApiError } from '../api/ApiError'



export function mostrarMensajes(caja: HTMLElement, mensajes: string[]): void {
  caja.replaceChildren()
  for (const mensaje of mensajes) {
    const linea = document.createElement('p')
    linea.textContent = mensaje // textContent, nunca innerHTML con datos externos
    caja.appendChild(linea)
  }
}

/** Crea un elemento con clase y texto (siempre textContent, nunca innerHTML). */
export function el<K extends keyof HTMLElementTagNameMap>(
  etiqueta: K,
  clase?: string,
  texto?: string,
): HTMLElementTagNameMap[K] {
  const nodo = document.createElement(etiqueta)
  if (clase) nodo.className = clase
  if (texto !== undefined) nodo.textContent = texto
  return nodo
}

export function mensajesDeError(error: unknown): string[] {
  if (error instanceof ApiError) {
    // 502/503/504: el proxy (o un balanceador) no pudo llegar al backend
    if (error.status === 502 || error.status === 503 || error.status === 504) {
      return ['No se pudo conectar con el servidor']
    }
    // 400: la lista de campos inválidos. 401 y 409: el mensaje del servidor
    return error.detalles.length > 0 ? error.detalles : [error.message]
  }
  // Si fetch falla (backend apagado, sin red) no hay ApiError
  return ['No se pudo conectar con el servidor']
}