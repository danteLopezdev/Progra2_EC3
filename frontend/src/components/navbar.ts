import { cerrarSesion, esAdministrador, obtenerSesion } from '../utils/sesion'

/** Barra de navegación. Se irán sumando enlaces según existan las pantallas. */
export function crearNavbar(): HTMLElement {
  const barra = document.createElement('nav')
  barra.className = 'barra'

  const inicio = document.createElement('a')
  inicio.href = '#/'
  inicio.textContent = 'Inicio'
  barra.append(inicio)

  const sesion = obtenerSesion()
  if (sesion) {
    const mias = document.createElement('a')
    mias.href = '#/mias'
    mias.textContent = 'Mis publicaciones'
    barra.append(mias)
    // Los enlaces a #/mias, #/nueva y #/admin se agregan cuando existan esas pantallas.
    const nueva = document.createElement('a')
    nueva.href = '#/nueva'
    nueva.textContent = 'Nueva publicación'
    barra.append(nueva)
    if (esAdministrador()) {
      const admin = document.createElement('a')
      admin.href = '#/admin'
      admin.textContent = 'Administración'
      barra.append(admin)
    }
    const saludo = document.createElement('span')
    saludo.className = 'saludo'
    saludo.textContent = `Hola, ${sesion.nombre} (${sesion.rol})` // nombre escrito por un usuario
    const salir = document.createElement('button')
    salir.textContent = 'Cerrar sesión'
    salir.addEventListener('click', () => {
      cerrarSesion()
      location.hash = '#/login'
    })
    barra.append(saludo, salir)
  } else {
    const login = document.createElement('a')
    login.href = '#/login'
    login.textContent = 'Iniciar sesión'
    const registro = document.createElement('a')
    registro.href = '#/registro'
    registro.textContent = 'Registrarse'
    barra.append(login, registro)
  }
  return barra
}