import type { LoginResponse, Rol } from '../types/Auth'

const CLAVE = 'sesion'

export function guardarSesion(sesion: LoginResponse): void {
  localStorage.setItem(CLAVE, JSON.stringify(sesion))
}

export function obtenerSesion(): LoginResponse | null {
  const texto = localStorage.getItem(CLAVE)
  if (!texto) return null
  try {
    return JSON.parse(texto) as LoginResponse
  } catch {
    localStorage.removeItem(CLAVE)
    return null
  }
}

export function obtenerToken(): string | null {
  return obtenerSesion()?.token ?? null
}

export function obtenerRol(): Rol | null {
  return obtenerSesion()?.rol ?? null
}

export function cerrarSesion(): void {
  localStorage.removeItem(CLAVE)
}

export function esAdministrador(): boolean {
  return obtenerRol() === 'ADMINISTRADOR'
}