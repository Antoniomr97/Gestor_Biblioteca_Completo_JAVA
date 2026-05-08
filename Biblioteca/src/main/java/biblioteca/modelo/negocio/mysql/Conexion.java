package biblioteca.modelo.negocio.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // =======================
    // Constantes de conexión
    // =======================
    private static final String HOST = "localhost";
    private static final String ESQUEMA = "dbbiblioteca";
    private static final String USUARIO = "admin";
    private static final String CONTRASENA = "biblioteca-2026";

    // =======================
    // Singleton
    // =======================
    private static Conexion instancia; // Única instancia
    private Connection conexion;       // Conexión JDBC

    // Constructor privado para Singleton
    private Conexion() {
        try {
            String url = "jdbc:mysql://" + HOST + ":3306/" + ESQUEMA + "?serverTimezone=UTC";
            conexion = DriverManager.getConnection(url, USUARIO, CONTRASENA);
            System.out.println("Conexion a la base de datos establecida correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al establecer la conexión con la base de datos.");
        }
    }

    // Método para obtener la instancia única
    public static Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    // Método para obtener la conexión
    public Connection establecerConexion() {
        return conexion;
    }

    // Método para cerrar la conexión
    public void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexion cerrada correctamente.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al cerrar la conexion.");
        }
    }
}