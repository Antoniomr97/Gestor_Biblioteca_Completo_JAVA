# Tarea: Implementación de Gestión XML mediante DOM

## 1. Gestión de XML mediante DOM

### Creación de paquetes
Debes crear los siguientes paquetes:
* `biblioteca.fichero`
* `biblioteca.utilidades`

### Clase UtilidadesXML
En el paquete `biblioteca.utilidades`, crea la clase: `UtilidadesXML`.
Esta clase será la encargada de trabajar con documentos XML mediante DOM.

**Métodos obligatorios:**
* `Document xmlToDom(String ruta)`: Convierte un fichero XML en un árbol DOM.
* `void domToXml(Document dom, String ruta)`: Convierte un árbol DOM en un fichero XML.
* `Document crearDomVacio(String etiquetaRaiz)`: Crea un documento DOM vacío con la etiqueta raíz indicada.

---

## 2. Copia de seguridad (exportación a XML)
Se añadirá en la interfaz gráfica la opción: **“Hacer copia de seguridad”**.

Esta opción deberá:
1. Obtener los datos desde la base de datos (modelo actual).
2. Convertirlos a estructuras DOM.
3. Generar los siguientes ficheros XML:
   * `usuarios.xml`
   * `libros.xml`
   * `autores.xml`
   * `prestamos.xml`

---

## 3. Restauración (importación desde XML)
Se añadirá la opción: **“Cargar copia de seguridad”**.

Esta opción deberá:
1. Leer los ficheros XML.
2. Convertirlos a objetos del dominio.
3. Eliminar los datos actuales de la base de datos.
4. Insertar los nuevos datos desde los XML.

---

## 4. Adaptación de clases de negocio
Para cada clase que gestione colecciones (**Usuarios, Libros, Autores, Prestamos**), deberás añadir funcionalidad XML.

### Ejemplo adaptado para Usuarios:
* **Atributos:** Atributo instancia (Singleton).
* **Métodos obligatorios:**
  * `getInstancia()`
  * `comenzar()`: Llama a `leerXML()`.
  * `terminar()`: Llama a `escribirXML()`.
  * `Usuario elementToUsuario(Element elemento)`: Convierte un nodo XML en un objeto Usuario.
  * `void leerXML()`: Lee el XML e inserta los usuarios en la colección.
  * `Element usuarioToElement(Document dom, Usuario usuario)`: Convierte un Usuario en nodo XML.
  * `void escribirXML()`: Escribe todos los usuarios en el XML.

*Esto mismo se aplicará a: Libros, Autores y Prestamos.*

---

## 5. Estructura de ficheros XML
Se deben generar y gestionar los archivos:
* `Usuarios.xml`
* `Autores.xml`
* `Libros.xml`
* `Prestamos.xml`

---

## 6. Consideraciones importantes
* Se debe usar **obligatoriamente DOM**.
* No se permite usar librerías externas (Jackson, Gson, etc.).
* **Se debe validar:**
  * XML bien formado.
  * Datos correctos.
* **Respetar el orden de carga:**
  1. Autores
  2. Libros
  3. Usuarios
  4. Préstamos

---

## 7. Interfaz Gráfica
Añadir al menú **Archivo**:
* Hacer copia de seguridad.
* Cargar copia de seguridad.

**Componentes a usar:**
* `FileChooser`
* `Alert` (confirmación)
* Mensajes de éxito/error

---

## 8. Se valorará
* Uso correcto de DOM.
* Estructura XML bien definida.
* Correcta conversión objeto ↔ XML.
* Integración con base de datos.
* Uso del patrón Singleton.
* Separación de responsabilidades.
* Interfaz gráfica funcional.
* Control de errores.
