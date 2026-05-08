package biblioteca.vista;

public enum Opcion {
    SALIR("Salir"),
    INSERTAR_USUARIO("Insertar Usuario"),
    BORRAR_USUARIO("Borrar Usuario"),
    MOSTRAR_USUARIO("Mostrar Usuarios"),
    INSERTAR_LIBRO("Insertar Libro"),
    BORRAR_LIBRO("Borrar Libro"),
    MOSTRAR_LIBRO("Mostrar Libros"),
    NUEVO_PRESTAMO("Nuevo Prestamo"),
    DEVOLVER_PRESTAMOS("Devolver Prestamos"),
    MOSTRAR_PRESTAMOS("Mostrar Prestamos"),
    MOSTRAR_PRESTAMOS_USUARIOS("Mostrar Prestamos de Usuario");

    private final String mensaje;

    Opcion(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return mensaje;
    }
}