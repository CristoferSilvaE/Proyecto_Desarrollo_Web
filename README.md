# 🏋️ PowerFit

Sistema web para la gestión de un gimnasio, desarrollado como proyecto académico del curso **Marco de Desarrollo Web**.

PowerFit permite gestionar usuarios, membresías, clases, horarios, reservas y compras de productos mediante una aplicación web desarrollada con Spring Boot, Thymeleaf y MySQL.

---

## 📌 1. Requisitos previos

Antes de iniciar, asegúrate de tener instalado:

- ☕ Java JDK 25.
- 🐬 MySQL Server y MySQL Workbench.
- 🟢 Node.js y npm.
- 🔧 Git.
- 💻 Visual Studio Code o IntelliJ IDEA.

El proyecto incluye Maven Wrapper, por lo que no necesitas instalar Maven por separado.

## 📥 2. Clonar el repositorio

```bash
git clone https://github.com/CristoferSilvaE/Proyecto_Desarrollo_Web.git
cd Proyecto_Desarrollo_Web
```

Para trabajar con la rama `verastegui`:

```bash
git switch -c verastegui --track origin/verastegui
```

Si ya tienes creada la rama local:

```bash
git switch verastegui
```

## 🗄️ 3. Configurar la base de datos

Los scripts SQL están ubicados en la carpeta `database/`.

Para una instalación desde cero, abre MySQL Workbench y ejecuta los siguientes archivos en este orden:

| Orden | Script | Descripción |
|---|---|---|
| 1 | `00_crear_bd.sql` | Crea la base de datos PowerFit. |
| 2 | `01_estructura.sql` | Crea las tablas, relaciones y restricciones. |
| 3 | `02_datos_iniciales.sql` | Registra roles, planes, clases, instructores y productos. |
| 4 | `03_datos_demo.sql` | Registra horarios de demostración. |
| 5 | `07_actualizar_sedes.sql` | Crea las sedes y relaciona pedidos con ellas. |
| 6 | `10_agregar_usuario.sql` | Agrega el nombre de usuario y su restricción UNIQUE. |

Para verificar la instalación:

```sql
USE powerfit;
SHOW TABLES;
```

También puedes ejecutar `04_consultas_verificacion.sql` para comprobar las tablas y los datos registrados.

### ⚠️ Importante sobre los demás scripts

- Los archivos `05`, `06`, `08` y `09` son migraciones de versiones anteriores. No deben ejecutarse en una instalación nueva, porque sus cambios ya están incorporados en `01_estructura.sql`.
- Los scripts `07` y `10` sí son necesarios para una instalación nueva.
- `99_reset_bd.sql` elimina toda la base de datos y su información. Utilízalo únicamente cuando quieras empezar desde cero.

No ejecutes dos veces los scripts de creación o migración.

## 🔌 4. Configurar la conexión a MySQL

Copia el archivo de ejemplo:

**Windows PowerShell:**

```powershell
Copy-Item src/main/resources/application-example.properties src/main/resources/application.properties
```

**Linux, macOS o Git Bash:**

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

Edita `src/main/resources/application.properties`:

```properties
spring.application.name=powerfit

spring.datasource.url=jdbc:mysql://localhost:3306/powerfit?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=TU_CONTRASENA
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.thymeleaf.cache=false

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=11MB
```

Reemplaza `TU_CONTRASENA` por la contraseña local de MySQL.

> 🔐 El archivo `application.properties` está excluido de Git para evitar publicar las credenciales personales de cada integrante.

## 👤 5. Cuenta inicial de administrador

El proyecto cuenta con un inicializador automático ubicado en:

`src/main/java/com/powerfit/powerfit/config/AdminInitializer.java`

Este componente comprueba si existe la cuenta administrativa y, si no existe, la crea automáticamente al iniciar Spring Boot.

La configuración se encuentra en:

`src/main/resources/admin.properties`

**Credenciales de acceso:**

| Campo | Credencial |
|---|---|
| 📧 Correo | `admin@powerfit.local` |
| 🔑 Contraseña | `admin123` |
| 🛡️ Perfil | ADMINISTRADOR |

Para que se cree correctamente:

1. Ejecutar los scripts SQL indicados anteriormente.
2. Verificar que exista el rol `ADMINISTRADOR`.
3. Mantener `powerfit.admin.enabled=true` en `admin.properties`.
4. Iniciar la aplicación.

La contraseña se almacena mediante BCrypt. Si el administrador ya existe, el inicializador no crea otra cuenta ni modifica su contraseña.

> ⚠️ Estas credenciales son exclusivamente para demostración y desarrollo académico.

## 🎨 6. Instalar y compilar Tailwind CSS

Desde la carpeta raíz del proyecto ejecuta:

```bash
npm install
npm run tailwind:build
```

Para recompilar automáticamente los estilos durante el desarrollo:

```bash
npm run tailwind:watch
```

El archivo CSS compilado se encuentra en:

`src/main/resources/static/css/base/powerfit.css`

## 🚀 7. Ejecutar Spring Boot

**Windows PowerShell:**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux, macOS o Git Bash:**

```bash
./mvnw spring-boot:run
```

También puedes iniciar la aplicación desde el IDE ejecutando:

`PowerfitApplication.java`

Cuando la aplicación inicie correctamente, ingresa desde el navegador:

```text
http://localhost:8080
```

## 🧩 8. Funcionalidades implementadas

### 🔐 Autenticación y usuarios

- Registro de clientes.
- Inicio de sesión mediante correo o nombre de usuario.
- Validación de DNI, correo y usuario duplicados.
- Contraseñas protegidas mediante BCrypt.
- Control de acceso por roles.
- Gestión administrativa de usuarios.
- Edición, baja lógica y reactivación de cuentas.
- Restablecimiento de contraseñas y cambio obligatorio de contraseña temporal.

### 👥 Clientes y perfil

- Consulta y edición de datos personales.
- Actualización de fotografía de perfil.
- Consulta de información relacionada con membresías y clases.
- Desactivación de cuenta.

### 💳 Membresías

- Visualización de planes desde MySQL.
- Solicitud de membresías.
- Consulta del historial.
- Edición y cancelación de solicitudes pendientes.
- Gestión administrativa de solicitudes.
- Aprobación, registro de pago y activación.
- Rechazo de solicitudes conservando el historial.

### 🏋️ Clases, instructores y horarios

- Consulta de clases y horarios disponibles.
- Gestión administrativa de clases e instructores.
- Creación y modificación de horarios.
- Activación y desactivación de horarios.
- Reserva y cancelación de clases.

### 🛒 Tienda y pedidos

- Catálogo de productos conectado a MySQL.
- Carrito de compras.
- Checkout con recojo en sede o delivery.
- Registro de pedidos y detalles.
- Actualización de stock al confirmar la compra.
- Consulta del historial de pedidos.
- Edición y cancelación de pedidos pendientes.

## 🛠️ 9. Tecnologías utilizadas

| Tecnología | Aplicación |
|---|---|
| ☕ Java 25 | Lenguaje principal |
| 🌱 Spring Boot 4.1.1 | Backend |
| 🌐 Spring Web MVC | Controladores |
| 🗃️ Spring Data JPA | Persistencia |
| 🐬 MySQL | Base de datos |
| 📄 Thymeleaf | Vistas |
| 🔒 BCrypt | Seguridad de contraseñas |
| 🎨 Tailwind CSS 3 | Diseño |
| 🟢 Node.js / npm | Compilación de estilos |
| 📦 Maven Wrapper | Construcción del proyecto |

## 🏗️ 10. Estructura del proyecto

```text
Proyecto_Desarrollo_Web/
├── database/                 # Scripts SQL
├── src/
│   ├── main/
│   │   ├── java/com/powerfit/powerfit/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       ├── admin.properties
│   │       └── application-example.properties
│   └── test/
├── uploads/
├── package.json
├── pom.xml
└── README.md
```

## ⚙️ 11. Consideraciones para levantar el programa

Antes de ejecutar PowerFit, verifica lo siguiente:

- MySQL Server debe estar iniciado.
- La base de datos `powerfit` debe estar creada y contener los scripts correspondientes.
- Las credenciales de conexión deben coincidir con las de tu instalación de MySQL.
- Node.js y npm deben estar instalados para compilar los estilos.
- La cuenta administrativa se genera automáticamente durante el inicio de la aplicación, si todavía no existe.
- La carpeta `uploads/` almacena localmente las imágenes cargadas por los usuarios.

### 📋 Orden rápido de ejecución

1. Clonar el repositorio.
2. Ejecutar los scripts SQL.
3. Configurar `application.properties`.
4. Ejecutar `npm install`.
5. Ejecutar `npm run tailwind:build`.
6. Iniciar Spring Boot.
7. Abrir `http://localhost:8080`.

---

<p align="center">
  <strong>🏋️ POWERFIT</strong><br>
  Sistema Web de Gestión para Gimnasios
</p>