# Gestor de Biblioteca Completo (Java)

Sistema integral de gestión de bibliotecas desarrollado en Java, que combina una interfaz gráfica moderna en JavaFX con una robusta lógica de negocio y persistencia dual en MySQL y XML.

## 🚀 Características Principales

### 📚 Gestión de Libros y Autores
*   **Inventario Completo**: Registro de libros y audiolibros con soporte para múltiples autores.
*   **Búsqueda Avanzada**: Localización rápida por ISBN o ID.
*   **Categorización**: Clasificación de obras por categorías (Novela, Poesía, Ensayo, etc.).

### 👤 Administración de Usuarios
*   **Registro Detallado**: Gestión de usuarios con información de contacto y dirección física.
*   **Historial de Préstamos**: Seguimiento individualizado de la actividad de cada socio.

### 🔄 Sistema de Préstamos
*   **Préstamos y Devoluciones**: Proceso simplificado de salida y retorno de libros.
*   **Control de Vencimientos**: Cálculo automático de días de retraso y fechas de devolución.
*   **Reglas de Negocio**: Validación de disponibilidad y restricciones de usuario.

### 💾 Persistencia y Seguridad
*   **Base de Datos MySQL**: Almacenamiento principal robusto para operaciones en tiempo real.
*   **Copias de Seguridad XML**: Sistema de exportación e importación total (Backup/Restore) utilizando procesamiento DOM/SAX.
*   **Integridad Referencial**: Garantía de coherencia de datos entre entidades mediante una capa DAO bien estructurada.

## 🛠️ Tecnologías Utilizadas
*   **Lenguaje**: Java 17+
*   **Interfaz Gráfica**: JavaFX (FXML + CSS)
*   **Base de Datos**: MySQL (JDBC)
*   **Gestión de Dependencias**: Gradle
*   **Formato de Intercambio**: XML (DOM API)

## 🏗️ Arquitectura
El proyecto sigue el patrón **MVC (Modelo-Vista-Controlador)** para asegurar una separación clara de responsabilidades:
*   **Modelo**: Capa de negocio con un patrón *Fachada* (`Modelo.java`) para simplificar el acceso a los datos.
*   **Vista**: Interfaz dual. Una GUI moderna con JavaFX y un sistema de fallback por Consola.
*   **Controlador**: Orquestador de la comunicación entre la interfaz de usuario y el núcleo del sistema.

## 🔧 Instalación y Configuración

### Requisitos Previos
*   **JDK 17** o superior.
*   **MySQL Server** en ejecución.
*   **Gradle** (incluido a través del wrapper `gradlew`).

### Configuración de la Base de Datos
1.  Crear un esquema llamado `dbbiblioteca`.
2.  El sistema utiliza las siguientes credenciales por defecto (configurables en `Conexion.java`):
    *   **Usuario**: `admin`
    *   **Contraseña**: `biblioteca-2026`

### Ejecución
1.  Clonar el repositorio:
    ```bash
    git clone https://github.com/Antoniomr97/Gestor_Biblioteca_Completo_JAVA.git
    ```
2.  Acceder al directorio del proyecto:
    ```bash
    cd "Gestor de Biblioteca completo/Biblioteca"
    ```
3.  Compilar y ejecutar:
    ```bash
    ./gradlew run
    ```

## 📂 Estructura del Proyecto
*   `src/main/java/biblioteca/modelo`: Lógica de negocio y entidades de dominio.
*   `src/main/java/biblioteca/vista/gui`: Controladores de la interfaz JavaFX.
*   `src/main/resources/biblioteca/gui`: Archivos FXML y hojas de estilo CSS.
*   `src/main/java/biblioteca/fichero`: Utilidades para la gestión de archivos XML.

---
*Desarrollado como un proyecto integral para la gestión eficiente de recursos bibliotecarios.*
