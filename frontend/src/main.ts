import './styles/auth.css'
import './styles/inicio.css'
import { mostrarDetalle } from './pages/detalle'
import { mostrarEditor } from './pages/editor'
import { mostrarInicio } from './pages/inicio'
import { mostrarLogin } from './pages/login'
import { mostrarRegistro } from './pages/registro'

const app = document.querySelector<HTMLDivElement>('#app')!

function enrutar(): void {
  const ruta = location.hash || '#/'
  app.replaceChildren()
  const detalle = ruta.match(/^#\/publicacion\/(\d{1,9})$/) // solo ids numéricos
  const editar = ruta.match(/^#\/editar\/(\d{1,9})$/)
  if (ruta === '#/login') mostrarLogin(app)
  else if (ruta === '#/registro') mostrarRegistro(app)
  else if (ruta === '#/nueva') void mostrarEditor(app)
  else if (editar) void mostrarEditor(app, Number(editar[1]))
  else if (detalle) void mostrarDetalle(app, Number(detalle[1]))
  else void mostrarInicio(app)
}

window.addEventListener('hashchange', enrutar) // se dispara al cambiar el # de la URL
enrutar()