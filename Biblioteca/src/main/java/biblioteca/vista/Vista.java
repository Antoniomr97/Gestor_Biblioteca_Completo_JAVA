package biblioteca.vista;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.dominio.*;
// import biblioteca.utilidades.Entrada; // Comentado: el teclado ya no es esencial
import biblioteca.vista.gui.FxApplication;
import javafx.application.Application;

import java.util.ArrayList;

/**
 * La Vista ahora gestiona la interfaz gráfica JavaFX.
 */
public class Vista {

    private Controlador controlador;

    public void setControlador(Controlador controlador) {
        if (controlador != null) this.controlador = controlador;
    }

    /**
     * Metodo principal que arranca la vista.
     * Ahora lanza la interfaz gráfica de JavaFX.
     */
    public void comenzar() {
        /* Antes de lanzar la ventana, le pasamos nuestro controlador
        para que la interfaz pueda comunicarse con el núcleo de la aplicación. */
        FxApplication.setControlador(controlador);
        
        // Lanzamos la aplicación de JavaFX. Esto abrirá la ventana.
        new Thread(() -> Application.launch(FxApplication.class)).start();
        
        /* 
        // --- ANTIGUO CÓDIGO DE CONSOLA (Tarea Anterior) ---
        int opcionElegida;
        do {
            Consola.mostrarMenu();
            opcionElegida = Entrada.entero();

            if (opcionElegida >= 0 && opcionElegida < Opcion.values().length) {
                ejecutarOpcion(Opcion.values()[opcionElegida]);
            } else {
                System.out.println("Opcion no valida.");
            }
        } while (opcionElegida != Opcion.SALIR.ordinal());
        */
    }

    /*
    // --- ANTIGUA LOGICA DE EJECUCIÓN POR COMANDOS ---
    private void ejecutarOpcion(Opcion opcion) {
        try {
            switch (opcion) {
                case INSERTAR_USUARIO: insertarUsuario(); break;
                case BORRAR_USUARIO: borrarUsuario(); break;
                case MOSTRAR_USUARIO: mostrarUsuarios(); break;
                case INSERTAR_LIBRO: insertarLibro(); break;
                case BORRAR_LIBRO: borrarLibro(); break;
                case MOSTRAR_LIBRO: mostrarLibros(); break;
                case NUEVO_PRESTAMO: nuevoPrestamo(); break;
                case DEVOLVER_PRESTAMOS: devolverPrestamo(); break;
                case MOSTRAR_PRESTAMOS: mostrarPrestamos(); break;
                case MOSTRAR_PRESTAMOS_USUARIOS: mostrarPrestamosUsuario(); break;
                case SALIR: terminar(); break;
            }
        } catch (Exception e) {
            System.out.println("Error en la operación: " + e.getMessage());
        }
    }
    */

    public void terminar() {
        System.out.println("Termina Vista (GUI)");
        controlador.terminar();
    }

    /*
    // --- ANTIGUOS MÉTODOS DE USUARIOS (CONSOLA) ---
    private void insertarUsuario() {
        Usuario nuevoUsuario = Consola.nuevoUsuario(false);
        if (controlador.buscar(nuevoUsuario) != null) {
            System.out.println("Error: Ya existe un usuario con la ID " + nuevoUsuario.getId());
        } else {
            controlador.alta(nuevoUsuario);
            System.out.println("Usuario registrado con exito.");
        }
    }

    private void borrarUsuario() {
        Usuario usuarioBusqueda = Consola.nuevoUsuario(true);
        if (controlador.baja(usuarioBusqueda))
            System.out.println("Usuario borrado.");
        else
            System.out.println("No se encontro el usuario.");
    }

    private void mostrarUsuarios() {
        ArrayList<Usuario> listaUsuarios = new ArrayList<>();
        for (Usuario u : controlador.listadoUsuarios()) {
            listaUsuarios.add(u);
        }
        if (listaUsuarios.isEmpty()) {
            System.out.println("No hay usuarios.");
            return;
        }
        listaUsuarios.sort(null);
        for (Usuario usuario : listaUsuarios) {
            System.out.println(usuario);
        }
    }

    // --- ANTIGUOS MÉTODOS DE LIBROS (CONSOLA) ---
    private void insertarLibro() {
        Libro nuevoLibro = Consola.nuevoLibro(false);
        if (controlador.buscar(nuevoLibro) != null) {
            System.out.println("Error: No se puede insertar. El ISBN " + nuevoLibro.getIsbn() + " ya existe.");
        } else {
            boolean ok = controlador.alta(nuevoLibro);
            if (ok) System.out.println("Libro registrado con exito.");
            else System.out.println("Error al registrar el libro.");
        }
    }

    private void borrarLibro() {
        Libro libroBusqueda = Consola.nuevoLibro(true);
        if (controlador.baja(libroBusqueda))
            System.out.println("Libro eliminado.");
        else
            System.out.println("Libro no encontrado.");
    }

    private void mostrarLibros() {
        ArrayList<Libro> listaLibros = new ArrayList<>();
        for (Libro l : controlador.listadoLibros()) {
            listaLibros.add(l);
        }
        if (listaLibros.isEmpty()) {
            System.out.println("No hay libros.");
            return;
        }
        listaLibros.sort(null);
        for (Libro libro : listaLibros) {
            System.out.println(libro);
        }
    }

    // --- ANTIGUOS MÉTODOS DE PRÉSTAMOS (CONSOLA) ---
    private void nuevoPrestamo() {
        Libro libro = controlador.buscar(Consola.nuevoLibro(true));
        Usuario usuario = controlador.buscar(Consola.nuevoUsuario(true));
        if (libro != null && usuario != null) {
            boolean ok = controlador.prestar(libro, usuario, Consola.leerFecha());
            if (ok) {
                System.out.println("Prestamo realizado.");
            } else {
                System.out.println("No se pudo realizar el préstamo.");
            }
        } else {
            System.out.println("Libro o Usuario no encontrado.");
        }
    }

    private void devolverPrestamo() {
        Libro libro = controlador.buscar(Consola.nuevoLibro(true));
        Usuario usuario = controlador.buscar(Consola.nuevoUsuario(true));
        if (libro != null && usuario != null) {
            Prestamo prestamoActivo = null;
            for (Prestamo p : controlador.listadoPrestamos(usuario)) {
                if (p.getLibro().equals(libro) && !p.isDevuelto()) {
                    prestamoActivo = p;
                    break;
                }
            }
            boolean exito = controlador.devolver(libro, usuario, Consola.leerFecha());
            if (exito) {
                System.out.println("Devolución procesada.");
                if (prestamoActivo != null && prestamoActivo.estaVencido()) {
                    System.out.println("El prestamo estaba vencido.");
                    System.out.println("Días de retraso: " + prestamoActivo.diasRetraso());
                } else {
                    System.out.println("El préstamo se devolvio a tiempo.");
                }
            } else {
                System.out.println("No se encontro el prestamo a devolver.");
            }
        } else {
            System.out.println("Datos incorrectos.");
        }
    }

    private void mostrarPrestamos() {
        ArrayList<Prestamo> listaPrestamos = new ArrayList<>();
        for (Prestamo p : controlador.listadoPrestamos()) {
            listaPrestamos.add(p);
        }
        if (listaPrestamos.isEmpty()) {
            System.out.println("No hay prestamos.");
            return;
        }
        listaPrestamos.sort(null);
        for (Prestamo prestamo : listaPrestamos) {
            System.out.println(prestamo);
        }
    }

    private void mostrarPrestamosUsuario() {
        Usuario usuario = controlador.buscar(Consola.nuevoUsuario(true));
        if (usuario == null) {
            System.out.println("Usuario no encontrado.");
            return;
        }
        ArrayList<Prestamo> prestamosUsuario = new ArrayList<>();
        for (Prestamo p : controlador.listadoPrestamos(usuario)) {
            prestamosUsuario.add(p);
        }
        if (prestamosUsuario.isEmpty()) {
            System.out.println("Este usuario no tiene préstamos.");
            return;
        }
        prestamosUsuario.sort(null);
        for (Prestamo prestamo : prestamosUsuario) {
            System.out.println(prestamo);
        }
    }
    */
}
