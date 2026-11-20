# Plataforma de Publicaciones Universitarias (Progra2_EC3)

Aplicación web para la comunidad de la Universidad Científica del Sur (sedes **Villa, Norte, Aramburú y Ate**). Los estudiantes crean y consultan publicaciones (emprendimientos, ventas, eventos, actividades, anuncios y búsqueda de objetos), organizadas por **sede** y **categoría**, con un **estado de vigencia** (`VIGENTE`, `ATENDIDA`, `FINALIZADA`).

Proyecto del curso **Lenguajes de Programación II** (EC3, ciclo 2026-2).

**Equipo:** Dante, Dostin, Lutber y Diego Condori.

---

## Funcionalidades

**Estudiante**
- Registro e inicio de sesión (JWT).
- Crear, editar y eliminar sus publicaciones, con imagen opcional (JPG, PNG o WebP, máx. 2 MB).
- Cambiar el estado de sus publicaciones.
- Listado público con filtros por sede, categoría y fecha, y búsqueda por palabras clave (FULLTEXT).
- Reportar publicaciones que infrinjan las normas (un reporte por usuario y publicación).

**Administrador**
- Ver reportes de moderación (filtro por estado), marcarlos como revisados o descartados.
- Retirar publicaciones.
- Exportar publicaciones y reportes a **PDF** o **Excel**.

> En el proyecto hay dos tipos de "reporte": el **reporte de moderación** (un estudiante denuncia una publicación) y el **reporte exportable** (PDF o Excel que genera el administrador).

---

## Arquitectura y tecnologías

API REST con frontend separado. Se reemplazó Thymeleaf por una SPA en TypeScript (aprobado por el profesor).

```
Navegador → Frontend (TypeScript) → Controller REST → Service → Repository (Spring Data JPA) → MySQL
```

| Capa | Tecnología |
|---|---|
| Backend | Java, Spring Boot 3, Spring Data JPA, Spring Security 6, Validation, Lombok, jjwt, OpenPDF, Apache POI |
| Frontend | HTML, CSS y TypeScript con Vite (plantilla `vanilla-ts`) |
| Base de datos | MySQL 8 (InnoDB, utf8mb4) |
| Despliegue | Railway (MySQL, backend y frontend con nginx) |
| Herramientas | IntelliJ IDEA, VS Code, MySQL Workbench, Postman, Git y GitHub |

Relación con MVC: **Modelo** = `entity/`, `repository/` y DTOs; **Vista** = frontend (`pages/` y `components/`); **Controlador** = `controller/` con la lógica en `service/`.

---

## Estructura del repositorio

```
Progra2_EC3/
├── backend/                  Proyecto Spring Boot (Maven)
│   └── src/main/
│       ├── java/pe/cientifica/backend/
│       │   ├── config/       CorsConfig, SecurityConfig, AdminInicializador
│       │   ├── controller/   Auth, Sede, Categoria, Publicacion, PublicacionGestion,
│       │   │                 PublicacionImagen, Reporte, Admin, Exportacion
│       │   ├── dto/          Objetos de petición y respuesta
│       │   ├── entity/       Usuario, Publicacion, Categoria, Sede, Reporte y enums
│       │   ├── exception/    GlobalExceptionHandler, ErrorResponse
│       │   ├── repository/   Interfaces de Spring Data JPA
│       │   ├── security/     JwtService, JwtAuthFilter
│       │   └── service/      Lógica de negocio
│       └── resources/
│           ├── application.properties          (va a Git)
│           └── application-local.properties    (NO va a Git: credenciales)
├── frontend/
│   └── src/
│       ├── api/              Llamadas fetch a la API
│       ├── components/       Navbar, tarjeta, filtros, formulario
│       ├── pages/            inicio, detalle, editor, mias, admin, login, registro
│       ├── types/            Interfaces TypeScript (contrato con los DTOs)
│       ├── utils/            Sesión y utilidades del DOM
│       └── styles/           CSS
├── docs/                     Script SQL (publicaciones.sql), diagramas UML y wireframes
└── README.md
```

---

## Cómo ejecutarlo en local

### Requisitos

- JDK 17 o superior (el despliegue usa Java 21)
- Maven (o el wrapper `mvnw` incluido)
- Node.js 20 o superior y npm
- MySQL 8 (o XAMPP/MariaDB) y MySQL Workbench
- Git

### 1. Clonar el repositorio

```bash
git clone https://github.com/danteLopezdev/Progra2_EC3.git
cd Progra2_EC3
```

### 2. Crear la base de datos

1. Levanta MySQL (si usas XAMPP, inicia el módulo MySQL; **el backend no arranca si la base no está activa**).
2. En MySQL Workbench, abre `docs/publicaciones.sql` y ejecútalo completo.
3. Verifica:

```sql
USE publicaciones;
SHOW TABLES;              -- sede, categoria, usuario, publicacion, reporte
SELECT * FROM sede;       -- 4 sedes
SELECT * FROM categoria;  -- 6 categorías
```

El `DROP DATABASE IF EXISTS publicaciones;` del script está **comentado** para no borrar datos por accidente. Descoméntalo solo si necesitas empezar de cero.

### 3. Crear `application-local.properties`

En `backend/src/main/resources/` crea el archivo `application-local.properties`. **No se sube a Git** (está en `.gitignore`); cada integrante tiene el suyo.

```properties
# Conexión a MySQL (tus datos locales)
spring.datasource.url=jdbc:mysql://localhost:3306/publicaciones
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA

# JWT: genera tu propia clave (ver abajo)
app.jwt.secret=PEGA_AQUI_LA_CLAVE_GENERADA
app.jwt.expiration-ms=3600000

# Administrador inicial (se crea al arrancar si no existe)
app.admin.correo=admin@ejemplo.com
app.admin.contrasena=UnaContraseñaLarga123
app.admin.nombre=Admin
app.admin.apellido=Sistema
```

Genera la clave del JWT (en Git Bash):

```bash
openssl rand -base64 32
```

> No definas `spring.jpa.hibernate.ddl-auto` en este archivo. El valor correcto (`validate`) ya está en `application.properties`: la base la crea el script y Hibernate solo comprueba que las entidades coincidan con las tablas.

### 4. Iniciar el backend (puerto 8080)

Abre `backend/` en IntelliJ IDEA y ejecuta `BackendApplication`, o desde la terminal:

```bash
cd backend
./mvnw spring-boot:run
```

En la consola debe aparecer `Started BackendApplication` sin errores de `Schema-validation`. Prueba en el navegador: `http://localhost:8080/api/sedes`.

### 5. Iniciar el frontend (puerto 5173)

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Vite reenvía `/api` al backend en el 8080 (proxy en `vite.config.ts`), por eso en local no hace falta configurar CORS.

### 6. Comprobar que todo funciona

- [ ] `http://localhost:8080/api/sedes` devuelve JSON.
- [ ] `http://localhost:5173` carga y permite registrarse e iniciar sesión.
- [ ] Puedes iniciar sesión con el administrador definido en `app.admin.*` (el saludo dice `(ADMINISTRADOR)`).

### Administrador inicial

El administrador **no se registra por la API**: `AdminInicializador` lo crea al arrancar con los datos de `app.admin.*`. Si el correo ya existe, no hace nada. Desde la sesión de administrador aparece el enlace **Administración** (`#/admin`).

---

## API REST

Rutas en plural con prefijo `/api`. Los errores salen con un formato uniforme (`ErrorResponse`: `timestamp`, `status`, `error`, `mensaje`, `ruta`, `detalles`).

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/registro` | Público |
| POST | `/api/auth/login` | Público |
| GET | `/api/sedes` · `/api/categorias` | Público |
| GET | `/api/publicaciones?sede&categoria&desde&hasta&q` | Público |
| GET | `/api/publicaciones/{id}` | Público |
| GET | `/api/imagenes/{nombre}` | Público |
| GET | `/api/publicaciones/mias` | Token |
| POST | `/api/publicaciones` | Token |
| PUT · DELETE | `/api/publicaciones/{id}` | Token (autor) |
| PATCH | `/api/publicaciones/{id}/estado` | Token (autor) |
| POST · DELETE | `/api/publicaciones/{id}/imagen` | Token (autor) |
| POST | `/api/publicaciones/{id}/reportes` | Token |
| GET | `/api/admin/reportes?estado=` | Administrador |
| PATCH | `/api/admin/reportes/{id}` | Administrador |
| DELETE | `/api/admin/publicaciones/{id}` | Administrador |
| GET | `/api/admin/exportar/{publicaciones\|reportes}?formato=pdf\|xlsx` | Administrador |

Para rutas protegidas se envía `Authorization: Bearer <token>` (el token se obtiene en `/api/auth/login` y vence en 1 hora).

Ejemplo de publicación (respuesta):

```json
{
  "id": 12,
  "titulo": "Vendo laptop",
  "descripcion": "Casi nueva, con cargador.",
  "imagen": "3f2a...c1.jpg",
  "fechaPublicacion": "2026-11-10T09:00:00",
  "estado": "VIGENTE",
  "categoriaId": 2,
  "categoriaNombre": "Ventas",
  "sedeId": 1,
  "sedeNombre": "Villa",
  "autorNombre": "Ana Quispe"
}
```

---

## Seguridad

- Contraseñas con **BCrypt** (límite de 72 caracteres).
- **JWT** sin estado (`STATELESS`); el rol viaja en el token firmado y nunca se acepta desde el cliente.
- Autorización en el servidor: solo el autor edita o elimina su publicación (403); `/api/admin/**` solo para `ADMINISTRADOR`.
- Mensaje genérico en el login (401) para evitar la enumeración de usuarios; correo normalizado a minúsculas.
- Entradas validadas con `@Valid`; consultas con parámetros (la búsqueda `q` es resistente a inyección SQL).
- Respuestas con DTOs: nunca se expone la contraseña ni el correo de otros usuarios.
- Imágenes: tipo validado por la firma de los bytes (no por la extensión), nombre generado por el servidor (UUID), máximo 2 MB, protección contra *path traversal* y cabecera `nosniff`.
- Frontend: todo dato externo se muestra con `textContent`; `innerHTML` solo con HTML fijo propio.
- Secretos fuera del repositorio (`application-local.properties` y variables de entorno).

**Limitaciones conocidas**
- El token se guarda en `localStorage` (accesible por JavaScript si hubiera XSS). Mejora futura: cookie `HttpOnly` con `Secure` y `SameSite`.
- El filtro JWT confía en los claims y no consulta la BD en cada petición: si se borra un usuario o cambia su rol, su token sigue válido hasta que expire (1 hora).
- El *payload* del JWT está codificado, no cifrado.
- Los 401 y 403 de la cadena de seguridad no usan el formato `ErrorResponse`.
- La búsqueda FULLTEXT de MySQL ignora palabras de menos de 3 caracteres y las muy comunes.
- Al eliminar una publicación, su archivo de imagen queda en `uploads/`.

---

## Pruebas

Se probaron con Postman y desde el navegador: autenticación, autorización por autor y por rol, validaciones (400), duplicados (409), filtros y búsqueda (incluido un intento de inyección SQL), exportaciones y subida de imágenes (tipo falso, tamaño excedido, sin token y ruta maliciosa). El detalle está en el informe del proyecto y en los documentos de avance (Steps 6 a 11).

---

## Despliegue (Railway)

```
Navegador → Frontend (nginx) --/api--> Backend (Spring Boot) → MySQL
```

Tres servicios desde el mismo repositorio (monorepo):

| Servicio | Detalle |
|---|---|
| MySQL | Base administrada por Railway (red privada). |
| Backend | `Dockerfile` (Maven + Java 21). Variables: `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`, `FRONTEND_URL`, `SPRING_PROFILES_ACTIVE=railway`, `APP_JWT_SECRET`, `APP_JWT_EXPIRATION_MS`, `APP_ADMIN_*`. Volume montado en `/app/uploads` para las imágenes. |
| Frontend | Root Directory `/frontend`, `Dockerfile` (Node 20 + nginx). Variable `BACKEND_URL`. Nginx sirve el build de Vite y reenvía `/api/` al backend. |

**URLs de producción**
- Aplicación: https://frontend-production-791ec9f6.up.railway.app
- API: https://progra2ec3-production.up.railway.app/api/categorias

Notas del despliegue:
- `FRONTEND_URL` debe coincidir con el dominio real del frontend (si no, el backend rechaza los `POST` por CORS).
- El índice FULLTEXT no lo crea Hibernate; en una base nueva hay que crearlo:
  ```sql
  ALTER TABLE publicacion ADD FULLTEXT INDEX ft_publicacion_titulo_descripcion (titulo, descripcion);
  ```
- Los secretos (JWT, contraseña del administrador y de MySQL) viven solo en las variables de Railway; nunca en el repositorio.

---

## Flujo de trabajo con Git

- `main` está protegida por un ruleset: los colaboradores trabajan en ramas y abren Pull Request.
- Commits con el formato `tipo(alcance): descripción` (`feat`, `fix`, `docs`, `test`, `refactor`, `style`).
- Antes de `git add`, revisar `git status`: no deben aparecer `application-local.properties`, `uploads/`, `target/`, `node_modules/` ni `.idea/`.

---

## Documentación adicional

En `docs/` están el script `publicaciones.sql`, los diagramas UML (casos de uso del estudiante y del administrador) y los wireframes.
