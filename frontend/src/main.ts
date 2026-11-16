/*import { obtenerSedes } from './api/sedes'

const app = document.querySelector<HTMLDivElement>('#app')!

async function cargarSedes() {
  const titulo = document.createElement('h1')
  titulo.textContent = 'Sedes'
  const lista = document.createElement('ul')
  app.replaceChildren(titulo, lista)

  try {
    const sedes = await obtenerSedes()
    for (const sede of sedes) {
      const item = document.createElement('li')
      item.textContent = `${sede.nombre} - ${sede.descripcion ?? ''}`
      lista.appendChild(item)
    }
  } catch (error) {
    const aviso = document.createElement('p')
    aviso.textContent = 'No se pudieron cargar las sedes.'
    app.appendChild(aviso)
    console.error(error)
  }
}

cargarSedes()*/

import './styles/auth.css'
import './styles/inicio.css'
import { mostrarInicio } from './pages/inicio'
import { mostrarLogin } from './pages/login'
import { mostrarRegistro } from './pages/registro'

const app = document.querySelector<HTMLDivElement>('#app')!

function enrutar(): void {
  const ruta = location.hash || '#/'
  app.replaceChildren()
  if (ruta === '#/login') mostrarLogin(app)
  else if (ruta === '#/registro') mostrarRegistro(app)
  else mostrarInicio(app)
}

window.addEventListener('hashchange', enrutar) // se dispara al cambiar el # de la URL
enrutar()
