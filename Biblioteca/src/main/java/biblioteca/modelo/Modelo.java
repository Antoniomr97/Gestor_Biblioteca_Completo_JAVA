package biblioteca.modelo;

import biblioteca.modelo.dominio.*;
import biblioteca.modelo.negocio.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Esta es nuestra "Fachada". Es la clase que da la cara ante el controlador
 * y organiza todo el trabajo sucio. En lugar de que el controlador hable
 * con 4 o 5 clases distintas, solo habla con nosotros.
 *
 * Aquí coordinamos los Libros, Usuarios, Préstamos y Autores.
 */
public class Modelo {
    // Estas son nuestras herramientas de trabajo (las clases de negocio)
    private Libros libros;
    private Usuarios usuarios;
    private Prestamos prestamos;

    /**
     * Arrancamos el modelo. Aquí es donde damos vida a nuestras clases
     * de negocio y les pedimos que abran sus conexiones a la base de datos.
     */
    public void comenzar() {
        // Inicializamos Libros y abrimos su conexión a MySQL
        this.libros = Libros.getInstancia();
        this.libros.comenzar();

        // Lo mismo para Usuarios
        this.usuarios = Usuarios.getInstancia();
        this.usuarios.comenzar();

        // Y para los Préstamos
        this.prestamos = Prestamos.getInstancia();
        this.prestamos.comenzar();

        // No nos olvidamos de los Autores, que los necesitamos para el XML
        Autores.getInstancia().comenzar();

        System.out.println("Modelo: ¡Todo listo y conectado!");
    }

    /**
     * Cerramos todo de forma ordenada antes de salir para no dejar
     * conexiones abiertas que puedan dar problemas.
     */
    public void terminar() {
        if (libros != null) libros.terminar();
        if (usuarios != null) usuarios.terminar();
        if (prestamos != null) prestamos.terminar();
        Autores.getInstancia().terminar();
    }

    // --- MÉTODOS DE USUARIOS ---
    public boolean alta(Usuario u) {
        try {
            usuarios.alta(u);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public Usuario buscar(Usuario u) { return usuarios.buscar(u.getId()); }
    public boolean baja(Usuario u) { return usuarios.baja(u); }
    public Usuario[] listadoUsuarios() {
        return usuarios.todos();
    }

    // --- MÉTODOS DE LIBROS ---
    public boolean alta(Libro l) {
        try {
            libros.alta(l);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public Libro buscar(Libro l) { return libros.buscar(l.getIsbn()); }
    public boolean baja(Libro l) { return libros.baja(l.getIsbn()); }
    public Libro[] listadoLibros() {
        return libros.todos().toArray(new Libro[0]);
    }

    // --- MÉTODOS DE PRÉSTAMOS ---
    public boolean prestar(Libro l, Usuario u, LocalDate f) {
        try {
            return prestamos.prestar(l, u, f);
        } catch (Exception e) {
            return false;
        }
    }
    public boolean devolver(Libro l, Usuario u, LocalDate f) {
        try {
            return prestamos.devolver(l, u, f);
        } catch (Exception e) {
            return false;
        }
    }
    public Prestamo[] listadoPrestamos() {
        return prestamos.historico();
    }
    public Prestamo[] listadoPrestamos(Usuario usuario) {
        if (prestamos == null || usuario == null) return new Prestamo[0];
        return prestamos.prestamosUsuario(usuario);
    }

    // ============================================================
    // SECCIÓN DE COPIA DE SEGURIDAD (NUEVO)
    // ============================================================

    /**
     * Exportamos todos nuestros datos a ficheros XML.
     * Le pedimos a cada clase de negocio que guarde lo suyo en la carpeta que nos digan.
     */
    public void hacerCopiaSeguridad(String carpeta) {
        System.out.println("Modelo: Iniciando exportación completa...");
        // Seguimos este orden: Autores -> Libros -> Usuarios -> Préstamos
        Autores.getInstancia().escribirXML(carpeta);
        libros.escribirXML(carpeta);
        usuarios.escribirXML(carpeta);
        prestamos.escribirXML(carpeta);
        System.out.println("Modelo: ¡Copia guardada con éxito!");
    }

    /**
     * Restauramos los datos desde los XML.
     * CUIDADO: Primero borramos todo lo que hay en la base de datos
     * para que no haya líos de datos duplicados o antiguos.
     */
    public void cargarCopiaSeguridad(String carpeta) {
        System.out.println("Modelo: Iniciando restauración total...");
        try {
            // 1. Limpiamos la casa antes de meter los muebles nuevos
            // Borramos en orden inverso a como se crearon para no romper nada
            usuarios.borrarTodos();   // Borra préstamos, direcciones y usuarios
            libros.borrarTodos();     // Borra relaciones libro-autor, audiolibros y libros
            Autores.getInstancia().borrarTodos(); // Borra autores

            System.out.println("Modelo: Base de datos vaciada.");

            // 2. Traemos los datos de los XML en el orden correcto
            Autores.getInstancia().leerXML(carpeta);
            libros.leerXML(carpeta);
            usuarios.leerXML(carpeta);
            prestamos.leerXML(carpeta);

            System.out.println("Modelo: ¡Restauración terminada!");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error crítico limpiando la BD: " + e.getMessage());
        }
    }
}