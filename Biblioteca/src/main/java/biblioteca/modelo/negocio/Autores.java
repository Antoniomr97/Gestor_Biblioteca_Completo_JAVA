package biblioteca.modelo.negocio;

import biblioteca.fichero.RutasXML;
import biblioteca.modelo.dominio.Autor;
import biblioteca.modelo.negocio.mysql.Conexion;
import biblioteca.utilidades.UtilidadesXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de negocio que gestiona los Autores de la biblioteca.
 *
 * Aplicamos el patrón Singleton: solo existe UNA instancia de Autores
 * en toda la aplicación, y se obtiene con getInstancia().
 *
 * Responsabilidades:
 *  - Consultar/insertar/borrar autores en la base de datos MySQL.
 *  - Exportar autores a XML (escribirXML) para la copia de seguridad.
 *  - Importar autores desde XML (leerXML) para la restauración.
 */
public class Autores {

    // ── SINGLETON ──────────────────────────────────────────────
    // Variable estática que guarda la única instancia de la clase
    private static Autores instancia;

    // Conexión activa a MySQL; la obtenemos a través de la clase Conexion
    private Connection conexion;

    // El constructor es privado para que nadie pueda hacer "new Autores()" desde fuera
    private Autores() {}

    /**
     * Devuelve la única instancia de Autores (patrón Singleton).
     * Si todavía no se ha creado, la creamos aquí la primera vez.
     */
    public static Autores getInstancia() {
        if (instancia == null) {
            instancia = new Autores(); // Solo se ejecuta la primera vez que se llama
        }
        return instancia;
    }

    // ── CICLO DE VIDA ──────────────────────────────────────────

    /**
     * Abre la conexión a la base de datos cuando arranca la aplicación.
     * El Modelo llama a este método al iniciarse.
     */
    public void comenzar() {
        // Pedimos la conexión ya establecida al Singleton Conexion
        conexion = Conexion.getInstancia().establecerConexion();
        System.out.println("Autores: conexión abierta correctamente.");
    }

    /**
     * Cierra recursos al salir de la aplicación.
     * Por ahora la conexión la gestiona el Modelo de forma centralizada.
     */
    public void terminar() {
        System.out.println("Autores: guardado completo.");
    }

    // ── OPERACIONES CON BASE DE DATOS ──────────────────────────

    /**
     * Obtiene todos los autores registrados en la base de datos MySQL.
     *
     * @return Lista con todos los objetos Autor, o lista vacía si no hay ninguno.
     */
    public List<Autor> todos() {
        List<Autor> lista = new ArrayList<>();
        if (conexion == null) return lista; // Protección si no hay conexión

        String sql = "SELECT nombre, apellidos, nacionalidad FROM autor";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    // Construimos el objeto Autor con los datos de cada fila del ResultSet
                    lista.add(new Autor(
                            rs.getString("nombre"),
                            rs.getString("apellidos"),
                            rs.getString("nacionalidad")));
                } catch (IllegalArgumentException ex) {
                    // Si un autor tiene datos inválidos, lo ignoramos y continuamos
                    System.err.println("Autores.todos: Autor con datos inválidos ignorado → " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Inserta un Autor en la base de datos.
     * Si ya existe (mismo nombre + apellidos + nacionalidad), lo ignora sin error.
     *
     * @param autor El Autor a insertar.
     * @return El idAutor generado (o el existente si ya estaba).
     * @throws SQLException Si hay un error de comunicación con la BD.
     */
    public int alta(Autor autor) throws SQLException {
        // ON DUPLICATE KEY UPDATE evita error si el autor ya existe; simplemente no hace nada
        String sqlInsert = "INSERT INTO autor (nombre, apellidos, nacionalidad) VALUES (?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE nombre=nombre";
        try (PreparedStatement ps = conexion.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getApellidos());
            ps.setString(3, autor.getNacionalidad());
            ps.executeUpdate();

            // Intentamos obtener el id generado (solo si era nuevo)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        // Si el autor ya existía, buscamos su id por sus datos únicos
        String sqlSelect = "SELECT idAutor FROM autor WHERE nombre=? AND apellidos=? AND nacionalidad=?";
        try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getApellidos());
            ps.setString(3, autor.getNacionalidad());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("idAutor");
            }
        }
        throw new RuntimeException("No se pudo obtener idAutor para: " + autor.getNombre());
    }

    /**
     * Elimina TODOS los autores de la base de datos.
     * Solo lo usamos durante el proceso de restauración de copia de seguridad.
     * Antes de llamar a este método, deben haberse eliminado libro_autor y prestamos.
     *
     * @throws SQLException Si hay error de BD.
     */
    public void borrarTodos() throws SQLException {
        String sql = "DELETE FROM autor";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.executeUpdate();
        }
        System.out.println("Autores.borrarTodos: tabla autor vaciada.");
    }

    // ── EXPORTACIÓN XML (Objeto → XML) ─────────────────────────

    /**
     * Convierte un objeto Autor en un nodo XML (Element) listo para
     * añadir a un árbol DOM. La estructura generada es:
     *
     * <pre>
     *   &lt;autor&gt;
     *     &lt;nombre&gt;Gabriel&lt;/nombre&gt;
     *     &lt;apellidos&gt;García Márquez&lt;/apellidos&gt;
     *     &lt;nacionalidad&gt;Colombiana&lt;/nacionalidad&gt;
     *   &lt;/autor&gt;
     * </pre>
     *
     * @param dom   El Document padre al que pertenece el nodo (necesario para crear elementos).
     * @param autor El objeto Autor que queremos convertir.
     * @return El Element XML con todos los datos del autor.
     */
    public Element autorToElement(Document dom, Autor autor) {
        // Creamos el nodo <autor>
        Element elem = dom.createElement("autor");

        // Añadimos cada campo del autor como subnodo de texto
        Element nombre = dom.createElement("nombre");
        nombre.setTextContent(autor.getNombre());
        elem.appendChild(nombre);

        Element apellidos = dom.createElement("apellidos");
        apellidos.setTextContent(autor.getApellidos());
        elem.appendChild(apellidos);

        Element nacionalidad = dom.createElement("nacionalidad");
        nacionalidad.setTextContent(autor.getNacionalidad());
        elem.appendChild(nacionalidad);

        return elem;
    }

    /**
     * Lee todos los autores de la BD y los guarda en el fichero Autores.xml
     * dentro de la carpeta indicada. Usamos UtilidadesXML para la serialización.
     *
     * @param carpeta Ruta de la carpeta donde se creará el fichero.
     */
    public void escribirXML(String carpeta) {
        // Obtenemos todos los autores actuales desde la BD
        List<Autor> lista = todos();

        // Creamos un DOM vacío con la etiqueta raíz <autores>
        Document dom = UtilidadesXML.crearDomVacio("autores");
        Element raiz = dom.getDocumentElement();

        // Por cada autor, creamos su nodo XML y lo añadimos a la raíz
        for (Autor a : lista) {
            raiz.appendChild(autorToElement(dom, a));
        }

        // Guardamos el árbol DOM en disco como fichero XML
        String ruta = carpeta + java.io.File.separator + RutasXML.AUTORES;
        UtilidadesXML.domToXml(dom, ruta);
        System.out.println("Autores.escribirXML: " + lista.size() + " autores exportados → " + ruta);
    }

    // ── IMPORTACIÓN XML (XML → Objeto) ─────────────────────────

    /**
     * Convierte un nodo XML (Element) en un objeto Autor de dominio.
     * Es el proceso inverso a autorToElement.
     *
     * @param elemento El nodo &lt;autor&gt; del árbol DOM.
     * @return El objeto Autor construido, o null si los datos son inválidos.
     */
    public Autor elementToAutor(Element elemento) {
        try {
            // Leemos cada campo de texto del nodo XML
            String nombre       = getTexto(elemento, "nombre");
            String apellidos    = getTexto(elemento, "apellidos");
            String nacionalidad = getTexto(elemento, "nacionalidad");

            // Creamos el objeto Autor con los datos leídos del XML
            return new Autor(nombre, apellidos, nacionalidad);

        } catch (IllegalArgumentException e) {
            // Si los datos no son válidos (vacíos, null...), avisamos y devolvemos null
            System.err.println("Autores.elementToAutor: Datos inválidos → " + e.getMessage());
            return null;
        }
    }

    /**
     * Lee el fichero Autores.xml desde la carpeta indicada e inserta
     * cada autor en la base de datos. Se usa durante la restauración.
     *
     * @param carpeta Ruta de la carpeta que contiene el fichero XML.
     */
    public void leerXML(String carpeta) {
        // Construimos la ruta completa del fichero
        String ruta = carpeta + java.io.File.separator + RutasXML.AUTORES;

        // Pedimos a UtilidadesXML que lea el fichero y nos devuelva el árbol DOM
        Document dom = UtilidadesXML.xmlToDom(ruta);
        if (dom == null) {
            System.err.println("Autores.leerXML: No se pudo leer el fichero → " + ruta);
            return;
        }

        // Obtenemos todos los nodos <autor> del árbol
        NodeList nodos = dom.getElementsByTagName("autor");
        int insertados = 0;

        for (int i = 0; i < nodos.getLength(); i++) {
            Element elem = (Element) nodos.item(i);
            Autor autor = elementToAutor(elem); // XML → objeto Java
            if (autor != null) {
                try {
                    alta(autor); // Insertamos en la BD
                    insertados++;
                } catch (SQLException e) {
                    System.err.println("Autores.leerXML: Error al insertar → " + e.getMessage());
                }
            }
        }
        System.out.println("Autores.leerXML: " + insertados + " autores importados desde " + ruta);
    }

    // ── UTILIDADES INTERNAS ────────────────────────────────────

    /**
     * Método auxiliar: devuelve el texto del primer subnodo con el tag indicado.
     * Por ejemplo, getTexto(elem, "nombre") devuelve "Gabriel" de &lt;nombre&gt;Gabriel&lt;/nombre&gt;.
     */
    private String getTexto(Element padre, String tag) {
        NodeList nl = padre.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            return nl.item(0).getTextContent().trim();
        }
        return ""; // Si el tag no existe, devolvemos cadena vacía para evitar null
    }
}
