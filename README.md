# 🏋️ PowerFit

> Sistema web para la gestión de un gimnasio desarrollado como proyecto académico del curso **Marco de Desarrollo Web**.

PowerFit permite gestionar usuarios, membresías, clases, productos y otras operaciones relacionadas con la administración de un gimnasio.

---

## 🚀 Puesta en marcha del proyecto

Esta sección explica cómo ejecutar PowerFit en una computadora nueva.

### 📋 Requisitos previos

Antes de iniciar, asegúrate de tener instalado:

- ☕ Java JDK 25
- 🐬 MySQL Server
- 🟢 Node.js y npm
- 🧰 Git
- 💻 Visual Studio Code o IntelliJ IDEA

El proyecto utiliza **Maven Wrapper**, por lo que no es necesario instalar Maven de forma independiente.

---

## 1️⃣ Clonar el repositorio

Clona el proyecto desde GitHub:

```bash
git clone <URL-DEL-REPOSITORIO>
```

Luego ingresa a la carpeta del proyecto:

```bash
cd Proyecto_Desarrollo_Web
```

Si trabajas sobre una rama específica:

```bash
git checkout Verastegui
```

---

## 2️⃣ Crear la base de datos

Los scripts necesarios se encuentran en:

```text
database/
```

Para una instalación nueva, ejecutar en MySQL Workbench en el siguiente orden:

```text
00_crear_bd.sql
01_estructura.sql
02_datos_iniciales.sql
03_datos_demo.sql
```

### 📌 Función de cada archivo

| Archivo | Descripción |
|---|---|
| `00_crear_bd.sql` | Crea la base de datos `powerfit` |
| `01_estructura.sql` | Crea tablas, relaciones, claves primarias y foráneas |
| `02_datos_iniciales.sql` | Inserta roles, planes, productos, clases e instructores |
| `03_datos_demo.sql` | Inserta horarios de demostración |
| `04_consultas_verificacion.sql` | Consultas para comprobar la instalación |
| `05_agregar_genero_bd_actual.sql` | Migración para bases antiguas |
| `99_reset_bd.sql` | Elimina completamente la base de datos |

> ⚠️ `99_reset_bd.sql` elimina toda la información de PowerFit. Utilizarlo únicamente cuando sea necesario reconstruir la base desde cero.

Para comprobar la instalación puede ejecutarse:

```sql
USE powerfit;
SHOW TABLES;
```

---

## 3️⃣ Configurar la conexión a MySQL

Dentro de:

```text
src/main/resources/
```

crear el archivo:

```text
application.properties
```

Puedes tomar como referencia:

```text
application-example.properties
```

Configuración básica:

```properties
spring.application.name=powerfit

spring.datasource.url=jdbc:mysql://localhost:3306/powerfit?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=TU_CONTRASENA
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.thymeleaf.cache=false

spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=3MB
```

> 🔐 `application.properties` está ignorado por Git para evitar publicar las credenciales locales de MySQL.

---

## 4️⃣ Instalar Tailwind CSS

PowerFit utiliza Tailwind CSS compilado mediante npm.

Instala las dependencias:

```bash
npm install
```

Para trabajar durante el desarrollo:

```bash
npm run tailwind:watch
```

Este comando observa los cambios realizados en los archivos HTML y CSS y actualiza automáticamente:

```text
src/main/resources/static/css/powerfit.css
```

Para generar manualmente la versión compilada:

```bash
npm run tailwind:build
```

---

## 5️⃣ Ejecutar Spring Boot

### 🪟 Windows PowerShell

```powershell
.\mvnw spring-boot:run
```

También puede ejecutarse directamente desde Visual Studio Code o IntelliJ utilizando:

```text
PowerfitApplication.java
```

Si la aplicación inicia correctamente, aparecerá un mensaje similar a:

```text
Started PowerfitApplication
```

---

## 6️⃣ Abrir PowerFit

Con Spring Boot ejecutándose, ingresar desde el navegador a:

```text
http://localhost:8080
```

La aplicación ya debería conectarse automáticamente a la base de datos `powerfit`.

---

# 🧩 Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| ☕ Java 25 | Lenguaje principal |
| 🍃 Spring Boot 4.1.1 | Backend |
| 🌐 Spring Web MVC | Controladores y navegación |
| 🗃️ Spring Data JPA | Persistencia |
| 🐬 MySQL | Base de datos |
| 🧾 Thymeleaf | Renderizado de vistas |
| 🎨 Tailwind CSS | Diseño de interfaz |
| 🟢 Node.js / npm | Compilación de Tailwind |
| 📦 Maven | Gestión de dependencias |
| 🔐 BCrypt | Protección de contraseñas |

---

# 🏗️ Arquitectura del proyecto

El proyecto utiliza una arquitectura basada en MVC.

```text
src/main/java/com/powerfit/powerfit/
│
├── config/
│   └── Configuración de la aplicación
│
├── controller/
│   └── Controladores Spring MVC
│
├── model/
│   └── Entidades JPA
│
├── repository/
│   └── Acceso a la base de datos
│
└── PowerfitApplication.java
```

Las vistas se encuentran en:

```text
src/main/resources/templates/
```

Los recursos estáticos:

```text
src/main/resources/static/
├── css/
└── imagenes/
```

---

# 🗄️ Modelo de base de datos

Actualmente PowerFit cuenta con las siguientes tablas principales:

```text
roles
usuarios
clientes
seguimiento_fisico
planes_membresia
membresias
pagos_membresia
instructores
clases
horarios_clase
reservas
productos
pedidos
detalle_pedido
```

Las relaciones son gestionadas mediante claves foráneas y entidades JPA.

---

# 🔐 Usuarios y autenticación

PowerFit utiliza un único sistema de autenticación.

Los roles considerados actualmente son:

```text
CLIENTE
ADMINISTRADOR
```

Cuando una persona se registra desde la aplicación recibe automáticamente el rol:

```text
CLIENTE
```

Las contraseñas no se almacenan directamente en la base de datos. Se protegen utilizando **BCrypt**.

La sesión del usuario se mantiene mediante:

```text
HttpSession
```

---

# 👤 Perfil del cliente

El cliente puede actualmente:

- Consultar sus datos personales.
- Editar nombres y apellidos.
- Editar teléfono.
- Editar fecha de nacimiento.
- Editar género.
- Cambiar su fotografía de perfil.
- Consultar su membresía.
- Consultar próximas clases.
- Consultar historial de pagos.
- Desactivar su cuenta.

Las fotografías subidas por los usuarios se almacenan localmente en:

```text
uploads/perfiles/
```

La carpeta:

```text
uploads/
```

no se almacena en GitHub.

---

# 💳 Membresías

Los planes actuales son:

| Plan | Precio |
|---|---:|
| Básico | S/ 59.00 |
| Premium | S/ 89.00 |
| Elite | S/ 129.00 |

El módulo de membresías permite actualmente realizar las operaciones:

```text
CREATE  → Solicitar una membresía
READ    → Consultar la solicitud
UPDATE  → Cambiar el plan solicitado
DELETE  → Cancelar una solicitud pendiente
```

Una solicitud nueva se registra con estado:

```text
PENDIENTE
```

Posteriormente podrá ser aprobada o rechazada desde el módulo administrativo.

---

# 📂 Archivos que no se suben a GitHub

Por seguridad y para evitar archivos innecesarios, `.gitignore` excluye:

```text
application.properties
node_modules/
uploads/
.vscode/
target/
```

---

# 🔄 Flujo rápido para otro desarrollador

Después de descargar el repositorio:

```text
1. Crear la base de datos con los scripts de /database
             ↓
2. Crear application.properties
             ↓
3. Ejecutar npm install
             ↓
4. Ejecutar npm run tailwind:build
             ↓
5. Ejecutar .\mvnw spring-boot:run
             ↓
6. Abrir localhost:8080
```

---

# 📌 Estado del proyecto

### ✅ Implementado

- Registro de clientes.
- Inicio y cierre de sesión.
- Contraseñas con BCrypt.
- Sesiones de usuario.
- Perfil dinámico.
- Edición de perfil.
- Fotografía de perfil.
- Desactivación de cuenta.
- Planes dinámicos.
- CRUD de membresías.
- Tienda conectada a MySQL.
- Clases y horarios dinámicos.
- Tailwind CSS organizado.
- Base de datos estructurada mediante scripts.

### 🚧 En desarrollo

- Carrito de compras funcional.
- CRUD de pedidos.
- Reservas de clases.
- Seguimiento físico.
- Panel administrativo.

---

# 🎓 Información académica

**Proyecto:** PowerFit  
**Curso:** Marco de Desarrollo Web  
**Tipo:** Proyecto académico universitario  
**Arquitectura:** Spring Boot MVC  
**Base de datos:** MySQL  

---

## 👥 Equipo de desarrollo

Proyecto desarrollado de manera colaborativa mediante Git y GitHub.

---

<p align="center">
  <strong>POWERFIT</strong><br>
  Sistema Web de Gestión para Gimnasios
</p>