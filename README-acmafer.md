# 🔥 ACMAFER — Guía de Instalación y Ejecución
### Sistema de Gestión Industrial · Spring Boot + SQL Server
### SENA CIMM Sogamoso · Grupo 3 · 2026

---

## 📋 REQUISITOS PREVIOS

Instala estas herramientas **antes** de comenzar:

| Herramienta | Versión | Descarga |
|---|---|---|
| Java JDK | 17 o superior | https://adoptium.net |
| Apache Maven | 3.8 o superior | https://maven.apache.org/download.cgi |
| SQL Server | 2019 o superior | https://www.microsoft.com/es-es/sql-server/sql-server-downloads |
| SQL Server Management Studio | Cualquier versión | https://aka.ms/ssmsfullsetup |
| VS Code | Cualquier versión | https://code.visualstudio.com |

---

## 🗄️ PASO 1 — CREAR LA BASE DE DATOS (SQL Server)

> ⚠️ Debes ejecutar este paso UNA SOLA VEZ antes de ejecutar el proyecto.

### Opción A — Usando SQL Server Management Studio (SSMS)
1. Abre SSMS y conéctate a tu servidor SQL Server
2. Ve a **File → Open → File...**
3. Selecciona el archivo `acmafer_db_sqlserver.sql` (está en la raíz del ZIP)
4. Presiona **F5** o el botón **Execute**
5. Verás el mensaje: `Base de datos ACMAFER (SQL Server) creada correctamente`

### Opción B — Desde la terminal (sqlcmd)
```bash
sqlcmd -S localhost -E -i acmafer_db_sqlserver.sql
```
> Si tu SQL Server tiene usuario/contraseña específicos:
> ```bash
> sqlcmd -S localhost -U sa -P "TuPassword" -i acmafer_db_sqlserver.sql
> ```

### ¿Qué crea el script?
- Base de datos `acmafer_db`
- Usuario `acmafer_user` con contraseña `Acmafer2026*`
- **13 tablas** con sus relaciones
- Datos de prueba: 5 usuarios, 5 categorías, 13 productos, 1 pedido

---

## ⚙️ PASO 2 — CONFIGURAR LA CONEXIÓN

Abre el archivo:
```
acmafer/src/main/resources/application.properties
```

Por defecto está configurado para SQL Server en `localhost:1433`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=acmafer_db;encrypt=true;trustServerCertificate=true
spring.datasource.username=acmafer_user
spring.datasource.password=Acmafer2026*
```

**Si tu SQL Server usa un nombre de instancia** (ej. `SQLEXPRESS`):
```properties
spring.datasource.url=jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=acmafer_db;encrypt=true;trustServerCertificate=true
```

**Si usas autenticación de Windows** (en lugar de usuario/contraseña):
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=acmafer_db;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=nativeAuthentication
# Omite username y password
```

---

## 🚀 PASO 3 — EJECUTAR EL PROYECTO

### Desde VS Code (recomendado)
1. Abre VS Code
2. Instala las extensiones recomendadas:
   - **Extension Pack for Java** (Microsoft)
   - **Spring Boot Extension Pack** (VMware)
3. Abre la carpeta `acmafer/` en VS Code
4. En el terminal integrado:
   ```bash
   mvn spring-boot:run
   ```
5. Espera ver el mensaje:
   ```
   Started AcmaferApplication in X.XXX seconds
   ```
6. Abre el navegador en: **http://localhost:8080**

### Desde cualquier terminal
```bash
cd acmafer
mvn spring-boot:run
```

### Desde el Spring Boot Dashboard en VS Code
1. Click en el ícono de Spring Boot en la barra lateral izquierda
2. Click en ▶️ junto a `acmafer-web`

---

## 🔐 PASO 4 — INGRESAR AL SISTEMA

Una vez que el servidor esté corriendo, ve a **http://localhost:8080**

Verás la **pantalla de video introductoria** con el logo ACMAFER.
Puedes esperar a que termine o hacer clic en **"Saltar intro ›"**.

### Usuarios de demostración
> Todos tienen la misma contraseña: `acmafer2026`

| Rol | Correo | Acceso |
|---|---|---|
| **Administrador** | admin@acmafer.com | Todo el sistema |
| **Supervisor** | supervisor@acmafer.com | Dashboard, reportes, tareas |
| **Trabajador** | trabajador@acmafer.com | Tareas asignadas |
| **Vendedor** | vendedor@acmafer.com | Catálogo, pedidos |
| **Cliente** | cliente@acmafer.com | Catálogo, mis pedidos |

---

## 📁 ESTRUCTURA DEL PROYECTO

```
acmafer/
├── pom.xml                                    ← dependencias Maven
├── src/main/
│   ├── java/com/acmafer/
│   │   ├── AcmaferApplication.java            ← clase principal
│   │   ├── comun/                             ← infraestructura compartida
│   │   │   ├── seguridad/SecurityConfig       ← Spring Security + BCrypt
│   │   │   ├── configuracion/WebMvcConfig     ← rutas estáticas
│   │   │   ├── controlador/HomeController     ← ruta "/"
│   │   │   ├── controlador/GlobalErrorController
│   │   │   ├── controlador/GlobalModelAdvice  ← datos globales en vistas
│   │   │   ├── excepcion/BusinessException
│   │   │   ├── excepcion/GlobalExceptionHandler
│   │   │   └── servicio/EmailService          ← stub (log en consola)
│   │   └── modulos/                           ← organización por dominio
│   │       ├── productos/
│   │       │   ├── entidad/  Producto, Categoria, HistorialProducto
│   │       │   ├── dto/      ProductoDTO
│   │       │   ├── repositorio/ ProductoRepository, CategoriaRepository
│   │       │   ├── servicio/ ProductoService
│   │       │   └── controlador/ ProductoController
│   │       ├── usuarios/
│   │       │   ├── entidad/  Usuario, Rol (enum), TokenRecuperacion
│   │       │   ├── dto/      UsuarioRegistroDTO
│   │       │   ├── repositorio/ UsuarioRepository, TokenRecuperacionRepository
│   │       │   ├── servicio/ UsuarioService, UsuarioDetailsService, PasswordService
│   │       │   └── controlador/ AuthController, UsuarioController, PerfilController
│   │       ├── pedidos/
│   │       │   ├── entidad/  Pedido, DetallePedido, HistorialPedido
│   │       │   ├── dto/      PedidoDTO
│   │       │   ├── repositorio/ PedidoRepository, HistorialPedidoRepository
│   │       │   ├── servicio/ PedidoService
│   │       │   ├── controlador/ PedidoController
│   │       │   └── util/    NumeroPedidoUtil
│   │       ├── tareas/
│   │       │   ├── entidad/  Tarea, AsignacionTarea, ComentarioTarea
│   │       │   ├── dto/      TareaDTO
│   │       │   ├── repositorio/ TareaRepository, AsignacionTareaRepository...
│   │       │   ├── servicio/ TareaService
│   │       │   └── controlador/ TareaController, RendimientoController
│   │       ├── notificaciones/
│   │       │   ├── entidad/  Notificacion
│   │       │   ├── repositorio/ NotificacionRepository
│   │       │   └── controlador/ NotificacionController
│   │       ├── reportes/
│   │       │   ├── servicio/ ReporteService (PDF + Excel)
│   │       │   └── controlador/ ReporteController
│   │       ├── dashboard/
│   │       │   ├── dto/      DashboardKpiDTO
│   │       │   ├── servicio/ DashboardService
│   │       │   └── controlador/ DashboardController
│   │       └── chatbot/
│   │           ├── servicio/ ChatbotService
│   │           └── controlador/ ChatbotController
│   └── resources/
│       ├── application.properties             ← configuración SQL Server
│       ├── templates/                         ← vistas Thymeleaf (.html)
│       │   ├── index.html                     ← landing page + video intro
│       │   ├── layout/  navbar.html, main.html
│       │   ├── auth/    login.html, registro.html, recuperar.html
│       │   ├── dashboard/ index.html + roles
│       │   ├── productos/ catalogo.html, form.html, detalle.html
│       │   ├── pedidos/   crear.html, mis-pedidos.html, todos.html
│       │   ├── tareas/    admin.html, mis-tareas.html, form.html
│       │   ├── reportes/  index.html (PDF/Excel + gráficos)
│       │   ├── usuarios/  lista.html, form.html
│       │   ├── notificaciones/ lista.html
│       │   └── error/     404.html, 500.html, 403.html
│       └── static/
│           ├── css/acmafer.css               ← diseño completo
│           ├── js/acmafer.js                 ← toda la interactividad
│           ├── js/particles.js               ← partículas del fondo
│           ├── img/acmafer-logo.png          ← logo ACMAFER
│           └── video/acmafer-intro.mp4       ← video de intro
└── src/test/                                 ← pruebas unitarias JUnit 5
    ├── java/com/acmafer/
    │   └── modulos/
    │       ├── productos/servicio/ProductoServiceTest
    │       ├── usuarios/servicio/UsuarioServiceTest
    │       ├── pedidos/servicio/PedidoServiceTest
    │       ├── pedidos/util/NumeroPedidoUtilTest
    │       └── chatbot/servicio/ChatbotServiceTest
    └── resources/application.properties     ← H2 in-memory para tests
```

---

## 🧪 PASO 5 — EJECUTAR PRUEBAS UNITARIAS

Las pruebas usan H2 en memoria, **no necesitan SQL Server**.

```bash
cd acmafer
mvn test
```

Resultado esperado:
```
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 🎨 FUNCIONALIDADES DEL DISEÑO

### Landing Page (`/`)
- **Video intro a pantalla completa** con logo ACMAFER superpuesto
- Botón "Saltar intro ›" para usuarios que ya lo vieron
- Se recuerda por sesión (no se vuelve a mostrar hasta cerrar el navegador)
- **Sección "Productos Destacados"**: mazo de tarjetas apiladas
  - Autoplay cada 3.5 segundos (ritmo moderado)
  - Clic en la tarjeta del frente → pausa + panel de detalle lateral
  - Botón X o clic fuera → cierra el detalle y reanuda autoplay
- Fondo de micro-partículas disolventes (efecto fundición)
- **Modo oscuro/claro** (toggle en la navbar, persiste en `localStorage`)

### Sistema (páginas internas)
- **Dashboard** con KPIs animados, carousel de top vendidos, gráficos
- **Chatbot AcmaBot** en todas las páginas autenticadas (esquina inferior derecha)
- Cards de productos con efecto tilt 3D al pasar el ratón
- Modal de producto con icono flotante animado
- Notificaciones en tiempo real en el navbar
- Barra de color molten animada en el footer

### Reportes (`/reportes`)
- **4 gráficos** con gradientes de fuego, animaciones y tooltips personalizados:
  - Estado de pedidos (barras con gradiente)
  - Productos por categoría (donut)
  - Top 6 productos más vendidos (barras horizontales)
  - Stock actual vs mínimo (barras comparativas)
- Descarga PDF: Dashboard ejecutivo, Pedidos, Inventario
- Descarga/Upload Excel: exportar productos, plantilla, carga masiva

---

## 🔧 SOLUCIÓN DE PROBLEMAS FRECUENTES

### "Cannot connect to SQL Server"
```
Caused by: com.microsoft.sqlserver.jdbc.SQLServerException: 
The TCP/IP connection to the host localhost, port 1433 has failed.
```
**Causa**: SQL Server no está corriendo o el puerto 1433 no está habilitado.

**Solución**:
1. Abre el **SQL Server Configuration Manager**
2. Verifica que el servicio **SQL Server (MSSQLSERVER)** esté corriendo
3. En **SQL Server Network Configuration → Protocols for MSSQLSERVER**:
   - Habilita **TCP/IP**
   - Click derecho → Properties → IP Addresses → verifica que el puerto sea `1433`
4. Reinicia el servicio SQL Server
5. Si usas instancia con nombre: agrega `\\NOMBRE_INSTANCIA` a la URL

---

### "Login failed for user 'acmafer_user'"
**Causa**: El usuario no se creó correctamente o SQL Server usa solo autenticación de Windows.

**Solución**:
1. En SSMS, click derecho en el servidor → Properties → Security
2. Cambia a **"SQL Server and Windows Authentication mode"**
3. Reinicia el servicio SQL Server
4. Vuelve a ejecutar el script `acmafer_db_sqlserver.sql`

---

### "No me deja ingresar al sistema" (credenciales incorrectas)
Si modificaste el script SQL y cambiaste el hash de contraseña, genera uno nuevo:

```python
# Requiere: pip install bcrypt
import bcrypt
h = bcrypt.hashpw(b"tu_nueva_contrasena", bcrypt.gensalt(10))
print(h.decode())
```

Pega el resultado en el script SQL reemplazando el campo `clave`.

---

### "Port 8080 already in use"
Cambia el puerto en `application.properties`:
```properties
server.port=9090
```
O mata el proceso que usa el puerto:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

---

### El video de intro no se reproduce
Los navegadores modernos bloquean el autoplay con audio. El video ya está configurado con `muted` para evitar esto, pero si aún no reproduce:
- Verifica que el archivo `acmafer-intro.mp4` esté en `src/main/resources/static/video/`
- El botón "Saltar intro ›" siempre funciona como alternativa

---

## 🗃️ BASE DE DATOS — DESCRIPCIÓN DE TABLAS

| Tabla | Descripción |
|---|---|
| `categoria` | Categorías de productos |
| `usuario` | Usuarios del sistema con roles y control de acceso |
| `producto` | Inventario con stock, precios y ventas acumuladas |
| `pedido` | Órdenes de compra con estados y método de pago |
| `detalle_pedido` | Líneas de cada pedido (producto + cantidad + precio) |
| `historial_producto` | Auditoría de cambios en productos |
| `historial_pedido` | Auditoría de cambios de estado en pedidos |
| `notificacion` | Notificaciones internas por usuario |
| `tarea` | Tareas de trabajo con prioridad y estado |
| `asignacion_tarea` | Asignación de tareas a empleados con fechas/horas |
| `comentario_tarea` | Comentarios en tareas |
| `token_recuperacion` | Tokens para recuperación de contraseña |

---

## 🔗 RUTAS DEL SISTEMA

| URL | Descripción | Acceso |
|---|---|---|
| `/` | Landing page (video intro + productos) | Público |
| `/auth/login` | Inicio de sesión | Público |
| `/auth/registro` | Registro de cuenta | Público |
| `/dashboard` | Panel principal con KPIs | Autenticado |
| `/productos/catalogo` | Catálogo con filtros | Autenticado |
| `/pedidos/crear` | Nuevo pedido | Autenticado |
| `/pedidos/mis-pedidos` | Mis pedidos | Autenticado |
| `/pedidos/todos` | Todos los pedidos | Admin/Supervisor |
| `/tareas` | Gestión de tareas | Admin/Supervisor/Trabajador |
| `/reportes` | Reportes PDF/Excel y gráficos | Admin/Supervisor |
| `/usuarios` | Gestión de usuarios | Admin |
| `/notificaciones` | Centro de notificaciones | Autenticado |
| `/perfil` | Mi perfil y cambio de contraseña | Autenticado |
| `/chatbot` | API del chatbot AcmaBot | Autenticado |

---

## 📦 TECNOLOGÍAS UTILIZADAS

| Capa | Tecnología | Versión |
|---|---|---|
| Backend | Spring Boot | 3.2.5 |
| Lenguaje | Java | 17 |
| ORM | Spring Data JPA + Hibernate | 6.x |
| Seguridad | Spring Security + BCrypt | 6.x |
| Base de datos | SQL Server | 2019+ |
| Plantillas | Thymeleaf + Spring Security Extras | 3.x |
| Estilos | CSS3 + Bootstrap | 5.3.3 |
| Scripts | JavaScript nativo (ES2022) | — |
| Fuentes | Orbitron + Rajdhani + Share Tech Mono | Google Fonts |
| Iconos | Bootstrap Icons | 1.11.3 |
| Gráficos | Chart.js | 4.4.2 |
| PDF | iText | 5.5.13.3 |
| Excel | Apache POI | 5.2.5 |
| Build | Apache Maven | 3.8+ |
| Tests | JUnit 5 + Mockito + AssertJ | — |

---

*Proyecto de grado — SENA CIMM Sogamoso · Grupo 3 · 2026*
