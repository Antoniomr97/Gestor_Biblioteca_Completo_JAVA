package biblioteca.modelo.negocio;

import biblioteca.fichero.RutasXML;
import biblioteca.modelo.dominio.Libro;
import biblioteca.modelo.dominio.Prestamo;
import biblioteca.modelo.dominio.Usuario;
import biblioteca.modelo.negocio.mysql.Conexion;
import biblioteca.utilidades.UtilidadesXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class Prestamos {

    private static Prestamos instancia;

    private ArrayList<Prestamo> prestamos;
    private Connection conexion;

    private Prestamos() {
        prestamos = new ArrayList<>();
    }

    public static Prestamos getInstancia() {
        if (instancia == null)
            instancia = new Prestamos();
        return instancia;
    }

    // ==========================
    // INICIO
    // ==========================
    public void comenzar() {
        conexion = Conexion.getInstancia().establecerConexion();
        cargarPrestamosBD();
        System.out.println("Prestamos: conexión abierta y datos cargados.");
    }

    public void terminar() {
        System.out.println("Prestamos: guardado completo.");
    }

    // ==========================
    // PRESTAR
    // ==========================
    public boolean prestar(Libro libro, Usuario usuario, LocalDate fecha) {

        if (libro == null || usuario == null)
            throw new IllegalArgumentException("Libro o usuario null");

        /*
         * CORRECCIÓN: Disponibilidad dinámica.
         * En vez de usar el conflictivo 'libro.tomarPrestado()', que dependía de un
         * stock volátil
         * que MySQL no retenía, ahora consultamos nuestro histórico vivo de préstamos.
         * Si el libro ya está apuntado con '!isDevuelto()', rompemos la ejecución.
         */

        for (Prestamo p : prestamos) {
            if (!p.isDevuelto() && p.getLibro().equals(libro)) {
                throw new IllegalStateException("No hay unidades disponibles (el libro ya está prestado)");
            }
        }

        Prestamo p = new Prestamo(libro, usuario, fecha);
        prestamos.add(p);

        String sql = "INSERT INTO prestamo(dni, isbn, fInicio, fLimite, devuelto) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getId());
            ps.setString(2, libro.getIsbn());
            ps.setDate(3, Date.valueOf(fecha));
            ps.setDate(4, Date.valueOf(fecha.plusDays(15)));
            ps.setBoolean(5, false);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar préstamo");
        }

        return true;
    }

    // ==========================
    // DEVOLVER
    // ==========================
    public boolean devolver(Libro libro, Usuario usuario, LocalDate fecha) {

        for (Prestamo p : prestamos) {
            if (!p.isDevuelto()
                    && p.getLibro().equals(libro)
                    && p.getUsuario().equals(usuario)) {

                p.marcarDevuelto(fecha);

                String sql = "UPDATE prestamo SET devuelto=?, fDevolucion=? WHERE dni=? AND isbn=? AND fInicio=?";

                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setBoolean(1, true);
                    ps.setDate(2, Date.valueOf(fecha));
                    ps.setString(3, usuario.getId());
                    ps.setString(4, libro.getIsbn());
                    ps.setDate(5, Date.valueOf(p.getFechaPrestamo()));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error al devolver préstamo");
                }

                return true;
            }
        }

        throw new IllegalStateException("No existe préstamo activo");
    }

    // ==========================
    // LISTADOS
    // ==========================
    public Prestamo[] historico() {
        return prestamos.toArray(new Prestamo[0]);
    }

    public Prestamo[] prestamosUsuario(Usuario usuario) {
        ArrayList<Prestamo> lista = new ArrayList<>();

        for (Prestamo p : prestamos) {
            if (p.getUsuario().equals(usuario)) {
                lista.add(p);
            }
        }

        return lista.toArray(new Prestamo[0]);
    }

    // ==========================
    // ELIMINAR PRÉSTAMOS DE CACHÉ
    // ==========================
    public void eliminarPrestamosUsuarioCache(Usuario usuario) {
        prestamos.removeIf(p -> p.getUsuario().equals(usuario));
    }

    // ==========================
    // CARGA DESDE BD (CORRECTA)
    // ==========================
    private void cargarPrestamosBD() {
        if (conexion == null) {
            System.err.println("Error: No se pudo cargar préstamos porque la conexión a BD es nula.");
            return;
        }

        String sql = "SELECT * FROM prestamo";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            prestamos.clear();

            while (rs.next()) {

                String dni = rs.getString("dni");
                String isbn = rs.getString("isbn");

                LocalDate fInicio = rs.getDate("fInicio").toLocalDate();
                // LocalDate fLimite = rs.getDate("fLimite").toLocalDate();
                boolean devuelto = rs.getBoolean("devuelto");

                Date fDevSQL = rs.getDate("fDevolucion");
                LocalDate fDevolucion = (fDevSQL != null) ? fDevSQL.toLocalDate() : null;

                // Este fallo es el que estaba dando problemas
                /*
                 * CORRECCIÓN 1: Integridad del objeto al recuperar de MySQL.
                 * Anteriormente, se creaban "new Usuario" y "new Libro" falsos con datos vacíos
                 * porque el ResultSet solo devolvía un dni y un isbn, corrompiendo la Vista de
                 * Java.
                 * Ahora usamos sus DNI originales para encontrar EXACTAMENTE los objetos que
                 * la app tiene ya cargados en las memorias de Usuarios y Libros, compartiendo
                 * su misma referencia RAM.
                 */
                Usuario usuario = Usuarios.getInstancia().buscar(dni);
                Libro libro = Libros.getInstancia().buscar(isbn);

                if (usuario == null || libro == null)
                    continue;

                Prestamo p = new Prestamo(libro, usuario, fInicio);

                // Ajustar estado
                if (devuelto) {
                    p.marcarDevuelto(fDevolucion);
                }

                prestamos.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error cargando préstamos");
        }
    }

    // =======================
    // XML → DOM (EXPORTACIÓN)
    // =======================

    /**
     * Convierte un objeto Prestamo en un nodo Element XML.
     * Estructura:
     * <pre>
     * &lt;prestamo&gt;
     *   &lt;usuario&gt;DNI&lt;/usuario&gt;
     *   &lt;libro&gt;ISBN&lt;/libro&gt;
     *   &lt;fechaInicio&gt;YYYY-MM-DD&lt;/fechaInicio&gt;
     *   &lt;fechaLimite&gt;YYYY-MM-DD&lt;/fechaLimite&gt;
     *   &lt;devuelto&gt;true|false&lt;/devuelto&gt;
     *   &lt;fechaDevolucion&gt;YYYY-MM-DD | &lt;/fechaDevolucion&gt; (vacío si no devuelto)
     * &lt;/prestamo&gt;
     * </pre>
     *
     * @param dom      El Document al que pertenecerá el Element.
     * @param prestamo El objeto Prestamo a convertir.
     * @return El Element XML resultante.
     */
    public Element prestamoToElement(Document dom, Prestamo prestamo) {
        // Creamos el nodo raiz <prestamo>
        Element elem = dom.createElement("prestamo");

        // Guardamos el DNI del usuario y el ISBN del libro (claves foráneas)
        crearHijo(dom, elem, "usuario",    prestamo.getUsuario().getId());
        crearHijo(dom, elem, "libro",      prestamo.getLibro().getIsbn());
        // Guardamos las fechas en formato estándar ISO (YYYY-MM-DD) mediante toString()
        crearHijo(dom, elem, "fechaInicio",     prestamo.getFechaPrestamo().toString());
        crearHijo(dom, elem, "fechaLimite",     prestamo.getfLimite().toString());
        crearHijo(dom, elem, "devuelto",        String.valueOf(prestamo.isDevuelto()));
        // Si aún no fue devuelto, guardamos cadena vacía en lugar de null
        crearHijo(dom, elem, "fechaDevolucion",
                prestamo.getfDevolucion() != null ? prestamo.getfDevolucion().toString() : "");

        return elem;
    }

    /**
     * Escribe todos los préstamos en el fichero Prestamos.xml dentro de la carpeta dada.
     *
     * @param carpeta Ruta de la carpeta destino.
     */
    public void escribirXML(String carpeta) {
        // Creamos un DOM vacío con la etiqueta raíz <prestamos>
        Document dom = UtilidadesXML.crearDomVacio("prestamos");
        Element raiz = dom.getDocumentElement();

        // Por cada préstamo en caché, generamos su nodo y lo añadimos a la raíz
        for (Prestamo p : prestamos) {
            raiz.appendChild(prestamoToElement(dom, p));
        }

        // Guardamos el árbol DOM en el fichero Prestamos.xml
        String ruta = carpeta + java.io.File.separator + RutasXML.PRESTAMOS;
        UtilidadesXML.domToXml(dom, ruta);
        System.out.println("Prestamos.escribirXML: " + prestamos.size() + " préstamos exportados → " + ruta);
    }

    // =======================
    // DOM → XML (IMPORTACIÓN)
    // =======================

    /**
     * Convierte un nodo Element XML en un objeto Prestamo.
     * Necesita que Usuarios y Libros ya estén cargados en BD/caché.
     *
     * @param elemento El nodo &lt;prestamo&gt; del DOM.
     * @return El objeto Prestamo, o null si los datos son inválidos.
     */
    public Prestamo elementToPrestamo(Element elemento) {
        try {
            // Leemos los identificadores clave: DNI del usuario e ISBN del libro
            String dniUsuario = getTexto(elemento, "usuario");
            String isbnLibro  = getTexto(elemento, "libro");
            // Parseamos las fechas desde el formato ISO-8601 (YYYY-MM-DD)
            LocalDate fInicio = LocalDate.parse(getTexto(elemento, "fechaInicio"));
            boolean devuelto  = Boolean.parseBoolean(getTexto(elemento, "devuelto"));
            String fDevStr    = getTexto(elemento, "fechaDevolucion");

            // Buscamos los objetos reales en caché: el préstamo necesita referencias reales, no solo IDs
            Usuario usuario = Usuarios.getInstancia().buscar(dniUsuario);
            Libro libro     = Libros.getInstancia().buscar(isbnLibro);

            if (usuario == null || libro == null) {
                // Si el usuario o libro no existen en la BD actual, no podemos crear el préstamo
                System.err.println("Prestamos.elementToPrestamo: Usuario o libro no encontrado. " +
                        "DNI=" + dniUsuario + " ISBN=" + isbnLibro);
                return null;
            }

            // Creamos el préstamo y, si ya fue devuelto, lo marcamos con su fecha
            Prestamo p = new Prestamo(libro, usuario, fInicio);
            if (devuelto && fDevStr != null && !fDevStr.isEmpty()) {
                p.marcarDevuelto(LocalDate.parse(fDevStr));
            }
            return p;

        } catch (Exception e) {
            System.err.println("Prestamos.elementToPrestamo: Datos inválidos → " + e.getMessage());
            return null;
        }
    }

    /**
     * Lee el fichero Prestamos.xml desde la carpeta indicada
     * e inserta los préstamos en la base de datos.
     * Prerrequisito: Usuarios y Libros ya deben estar cargados.
     *
     * @param carpeta Ruta de la carpeta origen.
     */
    public void leerXML(String carpeta) {
        if (conexion == null) comenzar();
        String ruta = carpeta + java.io.File.separator + RutasXML.PRESTAMOS;
        Document dom = UtilidadesXML.xmlToDom(ruta);
        if (dom == null) {
            System.err.println("Prestamos.leerXML: No se pudo parsear → " + ruta);
            return;
        }

        // Recargar usuarios y libros en caché para que elementToPrestamo los encuentre
        Usuarios.getInstancia().comenzar();

        NodeList nodos = dom.getElementsByTagName("prestamo");
        int insertados = 0;
        for (int i = 0; i < nodos.getLength(); i++) {
            Element elem = (Element) nodos.item(i);
            Prestamo p = elementToPrestamo(elem);
            if (p != null) {
                // Insertar directamente en BD
                String sql = "INSERT INTO prestamo(dni, isbn, fInicio, fLimite, devuelto, fDevolucion) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setString(1, p.getUsuario().getId());
                    ps.setString(2, p.getLibro().getIsbn());
                    ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
                    ps.setDate(4, Date.valueOf(p.getfLimite()));
                    ps.setBoolean(5, p.isDevuelto());
                    ps.setDate(6, p.getfDevolucion() != null ? Date.valueOf(p.getfDevolucion()) : null);
                    ps.executeUpdate();
                    prestamos.add(p);
                    insertados++;
                } catch (SQLException e) {
                    System.err.println("Prestamos.leerXML: Error al insertar préstamo → " + e.getMessage());
                }
            }
        }
        System.out.println("Prestamos.leerXML: " + insertados + " préstamos importados desde " + ruta);
    }

    // =======================
    // Utilidad interna
    // =======================
    private String getTexto(Element padre, String tag) {
        NodeList nl = padre.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            return nl.item(0).getTextContent().trim();
        }
        return "";
    }

    private void crearHijo(Document dom, Element padre, String tag, String valor) {
        Element hijo = dom.createElement(tag);
        hijo.setTextContent(valor != null ? valor : "");
        padre.appendChild(hijo);
    }
}