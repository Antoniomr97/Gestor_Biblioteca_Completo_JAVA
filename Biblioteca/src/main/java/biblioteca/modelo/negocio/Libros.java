package biblioteca.modelo.negocio;

import biblioteca.fichero.RutasXML;
import biblioteca.modelo.dominio.*;
import biblioteca.modelo.negocio.mysql.Conexion;
import biblioteca.utilidades.UtilidadesXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de negocio Libros.
 * Singleton.
 * Persiste los datos en MySQL usando la clase Conexion.
 */
public class Libros {

    private static Libros instancia;
    private Connection conexion;

    private Libros() {
        // Constructor privado
    }

    public static Libros getInstancia() {
        if (instancia == null) {
            instancia = new Libros();
        }
        return instancia;
    }

    // Abrir conexión
    public void comenzar() {
        conexion = Conexion.getInstancia().establecerConexion();
        System.out.println("Libros: conexión abierta correctamente.");
    }

    // Cerrar conexión
    public void terminar() {
        System.out.println("Libros: guardado completo.");
    }

    // =======================
    // Alta de libro o audiolibro
    // =======================
    public void alta(Libro libro) {
        if (libro == null)
            throw new IllegalArgumentException("No se puede registrar un libro null");

        try {
            // Insertar libro
            String sqlLibro = "INSERT INTO libro (isbn, titulo, anio, categoria) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psLibro = conexion.prepareStatement(sqlLibro)) {
                psLibro.setString(1, libro.getIsbn());
                psLibro.setString(2, libro.getTitulo());
                psLibro.setInt(3, libro.getAnio());
                psLibro.setString(4, libro.getCategoria().toString());
                psLibro.executeUpdate();
            }

            // Si es audiolibro, insertar en audiolibro
            if (libro instanceof Audiolibro audio) {
                String sqlAudio = "INSERT INTO audiolibro (isbn, duracion_segundos, formato) VALUES (?, ?, ?)";
                try (PreparedStatement psAudio = conexion.prepareStatement(sqlAudio)) {
                    psAudio.setString(1, libro.getIsbn());
                    psAudio.setLong(2, audio.getDuracion().toSeconds());
                    psAudio.setString(3, audio.getFormato());
                    psAudio.executeUpdate();
                }
            }

            // Insertar autores y relación libro_autor
            String sqlAutor = "INSERT INTO autor (nombre, apellidos, nacionalidad) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE nombre=nombre";
            String sqlLibroAutor = "INSERT INTO libro_autor (isbn, idAutor) VALUES (?, ?)";
            for (Autor autor : libro.getAutores()) {
                int idAutor;
                try (PreparedStatement psAutor = conexion.prepareStatement(sqlAutor, Statement.RETURN_GENERATED_KEYS)) {
                    psAutor.setString(1, autor.getNombre());
                    psAutor.setString(2, autor.getApellidos());
                    psAutor.setString(3, autor.getNacionalidad());
                    psAutor.executeUpdate();

                    try (ResultSet rs = psAutor.getGeneratedKeys()) {
                        if (rs.next()) {
                            idAutor = rs.getInt(1);
                        } else {
                            // Si ya existe, obtener id del autor
                            String sqlGetId = "SELECT idAutor FROM autor WHERE nombre=? AND apellidos=? AND nacionalidad=?";
                            try (PreparedStatement psGet = conexion.prepareStatement(sqlGetId)) {
                                psGet.setString(1, autor.getNombre());
                                psGet.setString(2, autor.getApellidos());
                                psGet.setString(3, autor.getNacionalidad());
                                try (ResultSet rsGet = psGet.executeQuery()) {
                                    if (rsGet.next()) {
                                        idAutor = rsGet.getInt("idAutor");
                                    } else {
                                        throw new RuntimeException(
                                                "No se pudo obtener idAutor para " + autor.getNombre());
                                    }
                                }
                            }
                        }
                    }
                }

                // Insertar relación libro_autor
                try (PreparedStatement psLA = conexion.prepareStatement(sqlLibroAutor)) {
                    psLA.setString(1, libro.getIsbn());
                    psLA.setInt(2, idAutor);
                    psLA.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar el libro en la base de datos");
        }
    }

    // =======================
    // Baja libro
    // =======================
    public boolean baja(String isbn) {
        if (isbn == null)
            return false;
        try {
            // 1. Comprobar si el libro tiene préstamos activos (sin devolver)
            String sqlCheck = "SELECT COUNT(*) FROM prestamo WHERE isbn = ? AND devuelto = false";
            try (PreparedStatement psCheck = conexion.prepareStatement(sqlCheck)) {
                psCheck.setString(1, isbn);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new RuntimeException("No se puede eliminar: el libro tiene préstamos activos.");
                    }
                }
            }

            // 2. Borrar los préstamos históricos (ya devueltos) para evitar FK error
            String sqlDelPrestamos = "DELETE FROM prestamo WHERE isbn = ?";
            try (PreparedStatement psDelP = conexion.prepareStatement(sqlDelPrestamos)) {
                psDelP.setString(1, isbn);
                psDelP.executeUpdate();
            }

            // 3. Borrar de libro_autor
            String sqlLA = "DELETE FROM libro_autor WHERE isbn=?";
            try (PreparedStatement psLA = conexion.prepareStatement(sqlLA)) {
                psLA.setString(1, isbn);
                psLA.executeUpdate();
            }

            // 4. Borrar de audiolibro (solo si es audiolibro; si no lo es, no hace nada)
            String sqlAudio = "DELETE FROM audiolibro WHERE isbn=?";
            try (PreparedStatement psAudio = conexion.prepareStatement(sqlAudio)) {
                psAudio.setString(1, isbn);
                psAudio.executeUpdate();
            }

            // 5. Borrar el libro principal
            String sqlLibro = "DELETE FROM libro WHERE isbn=?";
            try (PreparedStatement psLibro = conexion.prepareStatement(sqlLibro)) {
                psLibro.setString(1, isbn);
                int filas = psLibro.executeUpdate();
                return filas > 0;
            }

        } catch (RuntimeException re) {
            // Propagamos la excepción de negocio tal cual para que la UI la muestre
            throw re;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el libro en la base de datos: " + e.getMessage());
        }
    }

    // =======================
    // Buscar libro por ISBN
    // =======================
    public Libro buscar(String isbn) {
        if (isbn == null)
            return null;
        try {
            String sql = "SELECT * FROM libro WHERE isbn=?";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, isbn);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Libro libro;
                        try {
                            libro = new Libro(
                                    rs.getString("isbn"),
                                    rs.getString("titulo"),
                                    rs.getInt("anio"),
                                    Categoria.valueOf(rs.getString("categoria")));
                        } catch (IllegalArgumentException ex) {
                            System.err.println("Aviso: Se ignoró el libro con ISBN " + isbn + " por datos inválidos: " + ex.getMessage());
                            return null;
                        }
                        // Obtener autores
                        String sqlAutores = "SELECT a.nombre, a.apellidos, a.nacionalidad " +
                                "FROM autor a INNER JOIN libro_autor la ON a.idAutor = la.idAutor " +
                                "WHERE la.isbn=?";
                        try (PreparedStatement psA = conexion.prepareStatement(sqlAutores)) {
                            psA.setString(1, isbn);
                            try (ResultSet rsA = psA.executeQuery()) {
                                while (rsA.next()) {
                                    Autor autor = new Autor(
                                            rsA.getString("nombre"),
                                            rsA.getString("apellidos"),
                                            rsA.getString("nacionalidad"));
                                    libro.addAutor(autor);
                                }
                            }
                        }
                        // Revisar si es audiolibro
                        String sqlAudio = "SELECT * FROM audiolibro WHERE isbn=?";
                        try (PreparedStatement psAudio = conexion.prepareStatement(sqlAudio)) {
                            psAudio.setString(1, isbn);
                            try (ResultSet rsAudio = psAudio.executeQuery()) {
                                if (rsAudio.next()) {
                                    long durSeg = rsAudio.getLong("duracion_segundos");
                                    String formato = rsAudio.getString("formato");
                                    Audiolibro audiolibro = new Audiolibro(
                                            libro.getIsbn(),
                                            libro.getTitulo(),
                                            libro.getAnio(),
                                            libro.getCategoria(),
                                            Duration.ofSeconds(durSeg),
                                            formato);
                                    /*
                                     * CORRECCIÓN 3: Mala reconstruccion del Audiolibro.
                                     * Originalmente, el Audiolibro salía en blanco y perdía todos los autores
                                     * que sí se habían cargado en la variable 'libro' base.
                                     * Ahora nos aseguramos explícitamente de volcar esa información
                                     * para mantener un diseño coherente entre lo relacional y la herencia de
                                     * objetos.
                                     */
                                    for (Autor autor : libro.getAutores()) {
                                        audiolibro.addAutor(autor);
                                    }
                                    return audiolibro;
                                }
                            }
                        }
                        return libro;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar libro en la base de datos");
        }
        return null;
    }

    // =======================
    // Listar todos los libros
    // =======================
    public List<Libro> todos() {
        List<Libro> lista = new ArrayList<>();
        if (conexion == null) {
            System.err.println("Error: No se puede obtener libros porque la conexión a BD es nula.");
            return lista;
        }
        try {
            String sql = "SELECT isbn FROM libro";
            try (PreparedStatement ps = conexion.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Libro l = buscar(rs.getString("isbn"));
                    if (l != null) lista.add(l);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // =======================
    // BORRAR TODOS (usado en la restauración)
    // =======================
    /**
     * Borramos todos los libros, audiolibros y relaciones libro-autor.
     * Los préstamos deben haberse eliminado antes (desde Usuarios.borrarTodos).
     * Orden: libro_autor → audiolibro → libro (respetamos las FK).
     */
    public void borrarTodos() throws SQLException {
        if (conexion == null) comenzar();
        try (Statement st = conexion.createStatement()) {
            st.executeUpdate("DELETE FROM libro_autor"); // Primero la tabla relacional
            st.executeUpdate("DELETE FROM audiolibro");  // Luego los datos extra del audiolibro
            st.executeUpdate("DELETE FROM libro");        // Por último el libro base
        }
        System.out.println("Libros.borrarTodos: tablas vaciadas.");
    }

    // =======================
    // XML → DOM (EXPORTACIÓN)
    // =======================

    /**
     * Convierte un objeto Libro (o Audiolibro) en un nodo Element XML.
     * Estructura:
     * <pre>
     * &lt;libro&gt;
     *   &lt;isbn&gt;...&lt;/isbn&gt;
     *   &lt;titulo&gt;...&lt;/titulo&gt;
     *   &lt;anio&gt;...&lt;/anio&gt;
     *   &lt;categoria&gt;...&lt;/categoria&gt;
     *   &lt;tipo&gt;normal | audiolibro&lt;/tipo&gt;
     *   [Si audiolibro:
     *   &lt;duracion&gt;segundos&lt;/duracion&gt;
     *   &lt;formato&gt;...&lt;/formato&gt;]
     *   &lt;autores&gt;
     *     &lt;autor&gt;
     *       &lt;nombre&gt;...&lt;/nombre&gt;
     *       &lt;apellidos&gt;...&lt;/apellidos&gt;
     *       &lt;nacionalidad&gt;...&lt;/nacionalidad&gt;
     *     &lt;/autor&gt;
     *   &lt;/autores&gt;
     * &lt;/libro&gt;
     * </pre>
     *
     * @param dom   El Document al que pertenecerá el Element.
     * @param libro El objeto Libro a convertir.
     * @return El Element XML resultante.
     */
    public Element libroToElement(Document dom, Libro libro) {
        // Creamos el nodo raiz <libro>
        Element elem = dom.createElement("libro");

        // Campos comunes a todos los libros
        crearHijo(dom, elem, "isbn",      libro.getIsbn());
        crearHijo(dom, elem, "titulo",    libro.getTitulo());
        crearHijo(dom, elem, "anio",      String.valueOf(libro.getAnio()));
        crearHijo(dom, elem, "categoria", libro.getCategoria().toString());

        // Usamos pattern matching (instanceof) para detectar si es Audiolibro
        if (libro instanceof Audiolibro audio) {
            crearHijo(dom, elem, "tipo",     "audiolibro");
            crearHijo(dom, elem, "duracion", String.valueOf(audio.getDuracion().toSeconds()));
            crearHijo(dom, elem, "formato",  audio.getFormato());
        } else {
            crearHijo(dom, elem, "tipo", "normal"); // Libro convencional
        }

        // Añadimos el subnodo <autores> con los autores del libro
        Element autoresElem = dom.createElement("autores");
        Autores autoresNG = Autores.getInstancia();
        for (Autor a : libro.getAutores()) {
            autoresElem.appendChild(autoresNG.autorToElement(dom, a)); // Reutilizamos método de Autores
        }
        elem.appendChild(autoresElem);

        return elem;
    }

    /**
     * Escribe todos los libros en el fichero Libros.xml dentro de la carpeta dada.
     *
     * @param carpeta Ruta de la carpeta destino.
     */
    public void escribirXML(String carpeta) {
        List<Libro> lista = todos();
        Document dom = UtilidadesXML.crearDomVacio("libros");
        Element raiz = dom.getDocumentElement();

        for (Libro l : lista) {
            raiz.appendChild(libroToElement(dom, l));
        }

        String ruta = carpeta + java.io.File.separator + RutasXML.LIBROS;
        UtilidadesXML.domToXml(dom, ruta);
        System.out.println("Libros.escribirXML: completado → " + ruta);
    }

    // =======================
    // DOM → XML (IMPORTACIÓN)
    // =======================

    /**
     * Convierte un nodo Element XML en un objeto Libro (o Audiolibro).
     *
     * @param elemento El nodo &lt;libro&gt; del DOM.
     * @return El objeto Libro, o null si los datos son inválidos.
     */
    public Libro elementToLibro(Element elemento) {
        try {
            String isbn      = getTexto(elemento, "isbn");
            String titulo    = getTexto(elemento, "titulo");
            int anio         = Integer.parseInt(getTexto(elemento, "anio"));
            // Al guardar el XML usamos toString() ("Novela"), pero valueOf necesita el nombre exacto ("NOVELA")
            Categoria cat    = Categoria.valueOf(getTexto(elemento, "categoria").toUpperCase());
            String tipo      = getTexto(elemento, "tipo");

            Libro libro;
            if ("audiolibro".equalsIgnoreCase(tipo)) {
                long durSeg  = Long.parseLong(getTexto(elemento, "duracion"));
                String fmt   = getTexto(elemento, "formato");
                libro = new Audiolibro(isbn, titulo, anio, cat, Duration.ofSeconds(durSeg), fmt);
            } else {
                libro = new Libro(isbn, titulo, anio, cat);
            }

            // Autores embebidos en el XML
            NodeList autoresNL = elemento.getElementsByTagName("autor");
            Autores autoresNG = Autores.getInstancia();
            for (int i = 0; i < autoresNL.getLength(); i++) {
                Autor a = autoresNG.elementToAutor((Element) autoresNL.item(i));
                if (a != null) libro.addAutor(a);
            }

            return libro;
        } catch (Exception e) {
            System.err.println("Libros.elementToLibro: Datos inválidos → " + e.getMessage());
            return null;
        }
    }

    /**
     * Lee el fichero Libros.xml desde la carpeta indicada
     * e inserta los libros en la base de datos.
     *
     * @param carpeta Ruta de la carpeta origen.
     */
    public void leerXML(String carpeta) {
        if (conexion == null) comenzar();
        String ruta = carpeta + java.io.File.separator + RutasXML.LIBROS;
        Document dom = UtilidadesXML.xmlToDom(ruta);
        if (dom == null) {
            System.err.println("Libros.leerXML: No se pudo parsear → " + ruta);
            return;
        }

        NodeList nodos = dom.getElementsByTagName("libro");
        int insertados = 0;
        for (int i = 0; i < nodos.getLength(); i++) {
            Element elem = (Element) nodos.item(i);
            Libro l = elementToLibro(elem);
            if (l != null) {
                try {
                    alta(l);
                    insertados++;
                } catch (RuntimeException e) {
                    System.err.println("Libros.leerXML: No se pudo insertar libro → " + e.getMessage());
                }
            }
        }
        System.out.println("Libros.leerXML: " + insertados + " libros importados desde " + ruta);
    }

    // Extrae el texto de un tag hijo; si no existe devuelve cadena vacía (nunca null)
    private String getTexto(Element padre, String tag) {
        NodeList nl = padre.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            return nl.item(0).getTextContent().trim();
        }
        return "";
    }

    // Crea un subnodo <tag>valor</tag> y lo añade al Element padre
    private void crearHijo(Document dom, Element padre, String tag, String valor) {
        Element hijo = dom.createElement(tag);
        hijo.setTextContent(valor != null ? valor : "");
        padre.appendChild(hijo);
    }
}