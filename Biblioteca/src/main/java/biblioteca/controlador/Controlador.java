package biblioteca.controlador;

import biblioteca.modelo.Modelo;
import biblioteca.modelo.dominio.*;
import biblioteca.vista.Vista;
import java.time.LocalDate;

/**
 * Nosotros somos el Controlador. Nuestra misión es ser el "puente"
 * que une lo que el usuario ve (Vista) con lo que el programa hace (Modelo).
 *
 * Cuando el usuario pulsa un botón en la pantalla, la Vista nos llama a nosotros,
 * y nosotros le decimos al Modelo qué tiene que hacer.
 */
public class Controlador {
    // Guardamos aquí el modelo y la vista para tenerlos a mano
    private Modelo modelo;
    private Vista vista;

    /**
     * Para crearnos, necesitamos que nos pasen un modelo y una vista ya hechos.
     */
    public Controlador(Modelo modelo, Vista vista) {
        if (modelo == null || vista == null) {
            throw new IllegalArgumentException("No podemos trabajar sin modelo o vista.");
        }
        this.modelo = modelo;
        this.vista = vista;
    }

    /**
     * Aquí es donde arranca la magia. Pedimos al modelo que se prepare
     * y a la vista que se muestre en pantalla.
     */
    public void comenzar() {
        try {
            modelo.comenzar(); // Preparamos los datos y conexiones
        } catch (Exception e) {
            System.err.println("Error al arrancar el modelo: " + e.getMessage());
        }
        vista.comenzar(); // Abrimos la ventana para el usuario
    }

    /**
     * Cuando cerramos el programa, nos encargamos de que el modelo guarde todo.
     */
    public void terminar() {
        System.out.println("Cerrando el sistema de biblioteca...");
        modelo.terminar();
    }

    // --- MÉTODOS QUE LE PASAMOS AL MODELO ---
    // Simplemente hacemos de mensajeros entre la pantalla y los datos

    // Usuarios
    public boolean alta(Usuario u) { return modelo.alta(u); }
    public Usuario buscar(Usuario u) { return modelo.buscar(u); }
    public boolean baja(Usuario u) { return modelo.baja(u); }
    public Usuario[] listadoUsuarios() { return modelo.listadoUsuarios(); }

    // Libros
    public boolean alta(Libro l) { return modelo.alta(l); }
    public Libro buscar(Libro l) { return modelo.buscar(l); }
    public boolean baja(Libro l) { return modelo.baja(l); }
    public Libro[] listadoLibros() { return modelo.listadoLibros(); }

    // Préstamos
    public boolean prestar(Libro l, Usuario u, LocalDate f) { return modelo.prestar(l, u, f); }
    public boolean devolver(Libro l, Usuario u, LocalDate f) { return modelo.devolver(l, u, f); }
    public Prestamo[] listadoPrestamos() { return modelo.listadoPrestamos(); }
    public Prestamo[] listadoPrestamos(Usuario u) { return modelo.listadoPrestamos(u); }

    // ============================================================
    // XML - COPIAS DE SEGURIDAD
    // ============================================================

    /**
     * Le pedimos al modelo que guarde todos los datos en XML en la carpeta elegida.
     */
    public void hacerCopiaSeguridad(String carpeta) {
        modelo.hacerCopiaSeguridad(carpeta);
    }

    /**
     * Le pedimos al modelo que borre todo y cargue los datos desde los XML.
     */
    public void cargarCopiaSeguridad(String carpeta) {
        modelo.cargarCopiaSeguridad(carpeta);
    }
}