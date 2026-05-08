package biblioteca.modelo.negocio;

import biblioteca.fichero.RutasXML;
import biblioteca.modelo.dominio.Direccion;
import biblioteca.modelo.dominio.Usuario;
import biblioteca.modelo.negocio.mysql.Conexion;
import biblioteca.utilidades.UtilidadesXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Clase de negocio Usuarios.
 * Gestiona todos los usuarios de la biblioteca.
 * Patrón Singleton.
 * Persiste los datos en MySQL usando la clase Conexion.
 */
public class Usuarios {

    // =======================
    // Singleton
    // =======================
    private static Usuarios instancia;

    // =======================
    // Lista de usuarios en memoria (cache opcional)
    // =======================
    private ArrayList<Usuario> usuarios;

    // =======================
    // Conexión a MySQL
    // =======================
    private Connection conexion;

    // =======================
    // Constructor privado
    // =======================
    private Usuarios() {
        usuarios = new ArrayList<>();
    }

    // =======================
    // Método para obtener la instancia Singleton
    // =======================
    public static Usuarios getInstancia() {
        if (instancia == null) {
            instancia = new Usuarios();
        }
        return instancia;
    }

    // =======================
    // Abrir conexión a MySQL
    // =======================
    public void comenzar() {
        if (conexion == null) {
            conexion = Conexion.getInstancia().establecerConexion();
            System.out.println("Usuarios: conexion abierta correctamente.");
        }
        // Cargar usuarios existentes desde la base de datos
        cargarUsuarios();
    }

    // =======================
    // Cerrar conexión a MySQL
    // =======================
    public void terminar() {
        conexion = null;
        System.out.println("Usuarios: guardado completo.");
    }

    // =======================
    // Registrar un nuevo usuario
    // =======================
    public void alta(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("No se puede registrar un usuario null");
        }

        if (buscar(usuario) != null) {
            throw new IllegalArgumentException("El usuario ya esta registrado");
        }

        // Abrimos la conexión automáticamente si no está abierta
        if (conexion == null) {
            comenzar();
        }

        // Guardamos en memoria
        usuarios.add(usuario);

        // Guardar en MySQL
        String sqlUsuario = "INSERT INTO usuario(dni, nombre, email) VALUES (?, ?, ?)";
        String sqlDireccion = "INSERT INTO direccion(dni, via, numero, cp, localidad) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement psUsuario = conexion.prepareStatement(sqlUsuario);
                PreparedStatement psDireccion = conexion.prepareStatement(sqlDireccion)) {

            // Datos tabla usuario
            psUsuario.setString(1, usuario.getId());
            psUsuario.setString(2, usuario.getNombre());
            psUsuario.setString(3, usuario.getEmail());
            psUsuario.executeUpdate();

            // Datos tabla direccion
            Direccion dir = usuario.getDireccion();
            psDireccion.setString(1, usuario.getId());
            psDireccion.setString(2, dir.getVia());
            psDireccion.setString(3, dir.getNumero());
            psDireccion.setString(4, dir.getCp());
            psDireccion.setString(5, dir.getLocalidad());
            psDireccion.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar el usuario en la base de datos");
        }
    }

    // =======================
    // Eliminar un usuario
    // =======================
    public boolean baja(Usuario usuario) {
        if (usuario == null)
            return false;

        if (conexion == null) {
            comenzar();
        }

        try {
            // 1. Comprobar si tiene préstamos sin devolver
            String sqlCheck = "SELECT COUNT(*) FROM prestamo WHERE dni = ? AND devuelto = false";
            try (PreparedStatement psCheck = conexion.prepareStatement(sqlCheck)) {
                psCheck.setString(1, usuario.getId());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new RuntimeException("No se puede eliminar, aquí ningún deudor se escapa.");
                    }
                }
            }

            // 2. Borrar sus préstamos (ya están devueltos si llegamos aquí) para evitar
            // Foreign Key error
            String sqlDelPrestamos = "DELETE FROM prestamo WHERE dni = ?";
            try (PreparedStatement psDelPrestamos = conexion.prepareStatement(sqlDelPrestamos)) {
                psDelPrestamos.setString(1, usuario.getId());
                psDelPrestamos.executeUpdate();
            }

            // 3. Borrar su dirección (para evitar otro posible Foreign Key error)
            String sqlDelDireccion = "DELETE FROM direccion WHERE dni = ?";
            try (PreparedStatement psDelDireccion = conexion.prepareStatement(sqlDelDireccion)) {
                psDelDireccion.setString(1, usuario.getId());
                psDelDireccion.executeUpdate();
            }

            // 4. Borrar el usuario de MySQL
            String sql = "DELETE FROM usuario WHERE dni = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, usuario.getId());
                ps.executeUpdate();
            }

            // 5. Borrar de la caché de Préstamos para mantener coherencia
            Prestamos.getInstancia().eliminarPrestamosUsuarioCache(usuario);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar el usuario en la base de datos");
        }

        // Borrar de memoria
        return usuarios.remove(usuario);
    }

    // =======================
    // Buscar un usuario concreto
    // =======================
    public Usuario buscar(Usuario usuario) {
        if (usuario == null)
            return null;
        for (Usuario u : usuarios) {
            if (u.equals(usuario))
                return u;
        }
        return null;
    }

    // =======================
    // Buscar un usuario concreto por DNI (String)
    // =======================
    /*
     * CORRECCIÓN 1: Sobrecarga de método.
     * Al recuperar datos desde MySQL, solo tenemos el DNI (String).
     * Si no tuviéramos este método, tendríamos que inventarnos un Usuario "falso"
     * solo para buscar,
     * lo cual rompería el diseño. Aquí permitimos que el sistema encuentre el
     * objeto Usuario real
     * en memoria pasándole únicamente su cadena de DNI.
     */
    public Usuario buscar(String dni) {
        if (dni == null || dni.trim().isEmpty())
            return null;
        for (Usuario u : usuarios) {
            if (u.getId().equals(dni))
                return u;
        }
        return null;
    }

    // =======================
    // Devuelve todos los usuarios registrados
    // =======================
    public Usuario[] todos() {
        if (conexion == null) {
            comenzar();
        }
        Collections.sort(usuarios);
        return usuarios.toArray(new Usuario[0]);
    }

    // =======================
    // Cargar todos los usuarios desde la base de datos
    // =======================
    private void cargarUsuarios() {
        if (conexion == null) {
            System.err.println("Error: No se pudo cargar usuarios porque la conexión a BD es nula.");
            return;
        }
        usuarios.clear();
        String sql = "SELECT u.dni, u.nombre, u.email, d.via, d.numero, d.cp, d.localidad " +
                "FROM usuario u LEFT JOIN direccion d ON u.dni = d.dni";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String dni = rs.getString("dni");
                String nombre = rs.getString("nombre");
                String email = rs.getString("email");

                String via = rs.getString("via");
                String numero = rs.getString("numero");
                String cp = rs.getString("cp");
                String localidad = rs.getString("localidad");
                try {
                    Direccion direccion = new Direccion(via, numero, cp, localidad);
                    Usuario u = new Usuario(dni, nombre, email, direccion);
                    usuarios.add(u);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Aviso: Se ignoró el usuario con DNI " + dni + " por datos inválidos: " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al cargar usuarios desde la base de datos");
        }
    }

    // =======================
    // BORRAR TODOS (usado en la restauración)
    // =======================
    /**
     * Borramos todos los registros de la BD en el orden correcto para respetar
     * las claves foráneas: primero los préstamos y direcciones que dependen del usuario,
     * y por último los usuarios en sí. Solo lo usamos antes de una restauración.
     */
    public void borrarTodos() throws SQLException {
        if (conexion == null) comenzar();
        try (Statement st = conexion.createStatement()) {
            st.executeUpdate("DELETE FROM prestamo");   // Primero borramos los préstamos (dependen de usuario y libro)
            st.executeUpdate("DELETE FROM direccion");  // Luego borramos las direcciones (dependen de usuario)
            st.executeUpdate("DELETE FROM usuario");    // Por último borramos los usuarios
        }
        // Limpiamos también la caché en memoria para que esté sincronizada con la BD
        usuarios.clear();
    }

    // =======================
    // XML → DOM (EXPORTACIÓN)
    // =======================

    /**
     * Convierte un objeto Usuario en un nodo Element XML.
     * Estructura:
     * <pre>
     * &lt;usuario&gt;
     *   &lt;dni&gt;...&lt;/dni&gt;
     *   &lt;nombre&gt;...&lt;/nombre&gt;
     *   &lt;email&gt;...&lt;/email&gt;
     *   &lt;direccion&gt;
     *     &lt;via&gt;...&lt;/via&gt;
     *     &lt;numero&gt;...&lt;/numero&gt;
     *     &lt;cp&gt;...&lt;/cp&gt;
     *     &lt;localidad&gt;...&lt;/localidad&gt;
     *   &lt;/direccion&gt;
     * &lt;/usuario&gt;
     * </pre>
     *
     * @param dom     El Document al que pertenecerá el Element.
     * @param usuario El objeto Usuario a convertir.
     * @return El Element XML resultante.
     */
    public Element usuarioToElement(Document dom, Usuario usuario) {
        // Creamos el nodo raíz <usuario>
        Element elem = dom.createElement("usuario");

        // Añadimos los campos simples del usuario como hijos de texto
        crearHijo(dom, elem, "dni",    usuario.getId());
        crearHijo(dom, elem, "nombre", usuario.getNombre());
        crearHijo(dom, elem, "email",  usuario.getEmail());

        // Creamos el subnodo <direccion> que agrupa los campos de la dirección
        Element dirElem = dom.createElement("direccion");
        Direccion dir = usuario.getDireccion();
        crearHijo(dom, dirElem, "via",       dir.getVia());
        crearHijo(dom, dirElem, "numero",    dir.getNumero());
        crearHijo(dom, dirElem, "cp",        dir.getCp());
        crearHijo(dom, dirElem, "localidad", dir.getLocalidad());
        elem.appendChild(dirElem); // Añadimos el bloque dirección dentro del nodo usuario

        return elem;
    }

    /**
     * Escribe todos los usuarios en el fichero Usuarios.xml dentro de la carpeta dada.
     *
     * @param carpeta Ruta de la carpeta destino.
     */
    public void escribirXML(String carpeta) {
        if (conexion == null) comenzar(); // Nos aseguramos de tener conexión activa

        // Creamos un DOM vacío con la etiqueta raiz <usuarios>
        Document dom = UtilidadesXML.crearDomVacio("usuarios");
        Element raiz = dom.getDocumentElement();

        // Por cada usuario de la caché, generamos su nodo XML y lo añadimos a la raíz
        for (Usuario u : usuarios) {
            raiz.appendChild(usuarioToElement(dom, u));
        }

        // Pedimos a UtilidadesXML que guarde el árbol DOM como fichero en disco
        String ruta = carpeta + java.io.File.separator + RutasXML.USUARIOS;
        UtilidadesXML.domToXml(dom, ruta);
        System.out.println("Usuarios.escribirXML: " + usuarios.size() + " usuarios exportados → " + ruta);
    }

    // =======================
    // DOM → XML (IMPORTACIÓN)
    // =======================

    /**
     * Convierte un nodo Element XML en un objeto Usuario.
     *
     * @param elemento El nodo &lt;usuario&gt; del DOM.
     * @return El objeto Usuario, o null si los datos son inválidos.
     */
    public Usuario elementToUsuario(Element elemento) {
        try {
            // Leemos los campos simples directamente del nodo XML
            String dni      = getTexto(elemento, "dni");
            String nombre   = getTexto(elemento, "nombre");
            String email    = getTexto(elemento, "email");

            // Buscamos el subnodo <direccion> que contiene los datos de la dirección
            NodeList dirNL = elemento.getElementsByTagName("direccion");
            if (dirNL.getLength() == 0) {
                System.err.println("Usuarios.elementToUsuario: Falta nodo <direccion> para DNI " + dni);
                return null; // No podemos crear un usuario sin dirección
            }
            // Extraemos los campos de la dirección del subnodo
            Element dirElem  = (Element) dirNL.item(0);
            String via       = getTexto(dirElem, "via");
            String numero    = getTexto(dirElem, "numero");
            String cp        = getTexto(dirElem, "cp");
            String localidad = getTexto(dirElem, "localidad");

            // Construimos el objeto Direccion y luego el Usuario completo
            Direccion dir = new Direccion(via, numero, cp, localidad);
            return new Usuario(dni, nombre, email, dir);

        } catch (IllegalArgumentException e) {
            // Si algún campo no supera la validación (DNI inválido, email mal, CP incorrecto...)
            System.err.println("Usuarios.elementToUsuario: Datos inválidos → " + e.getMessage());
            return null;
        }
    }

    /**
     * Lee el fichero Usuarios.xml desde la carpeta indicada
     * e inserta los usuarios en la base de datos.
     *
     * @param carpeta Ruta de la carpeta origen.
     */
    public void leerXML(String carpeta) {
        if (conexion == null) comenzar();
        String ruta = carpeta + java.io.File.separator + RutasXML.USUARIOS;
        Document dom = UtilidadesXML.xmlToDom(ruta);
        if (dom == null) {
            System.err.println("Usuarios.leerXML: No se pudo parsear → " + ruta);
            return;
        }

        NodeList nodos = dom.getElementsByTagName("usuario");
        int insertados = 0;
        for (int i = 0; i < nodos.getLength(); i++) {
            Element elem = (Element) nodos.item(i);
            Usuario u = elementToUsuario(elem);
            if (u != null) {
                try {
                    alta(u);
                    insertados++;
                } catch (RuntimeException e) {
                    System.err.println("Usuarios.leerXML: No se pudo insertar usuario → " + e.getMessage());
                }
            }
        }
        System.out.println("Usuarios.leerXML: " + insertados + " usuarios importados desde " + ruta);
    }

    // Utilidad interna: extrae el texto de un tag hijo del elemento padre
    // Por ejemplo: getTexto(elem, "dni") → "AB345"
    private String getTexto(Element padre, String tag) {
        NodeList nl = padre.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            return nl.item(0).getTextContent().trim();
        }
        return ""; // Devolvemos cadena vacía para evitar NullPointerException
    }

    // Utilidad interna: crea un nodo hijo con el texto dado y lo añade al padre
    // Por ejemplo: crearHijo(dom, elem, "dni", "AB345") → <dni>AB345</dni>
    private void crearHijo(Document dom, Element padre, String tag, String valor) {
        Element hijo = dom.createElement(tag);
        hijo.setTextContent(valor != null ? valor : ""); // Si el valor es null, ponemos cadena vacía
        padre.appendChild(hijo);
    }
}