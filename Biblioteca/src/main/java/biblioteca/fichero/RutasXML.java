package biblioteca.fichero;

/**
 * Clase de constantes que centraliza los nombres de los ficheros XML
 * que usamos para las copias de seguridad de la biblioteca.
 *
 * Separamos aquí los nombres para que si algún día queremos cambiarlos,
 * solo tengamos que hacerlo en un único lugar del código.
 */
public class RutasXML {

    // Nombre del fichero XML donde guardamos los autores
    public static final String AUTORES   = "Autores.xml";

    // Nombre del fichero XML donde guardamos los libros (y audiolibros)
    public static final String LIBROS    = "Libros.xml";

    // Nombre del fichero XML donde guardamos los usuarios de la biblioteca
    public static final String USUARIOS  = "Usuarios.xml";

    // Nombre del fichero XML donde guardamos los préstamos realizados
    public static final String PRESTAMOS = "Prestamos.xml";

    // Hacemos el constructor privado para que nadie cree instancias de esta clase;
    // es solo una clase de constantes, como una lista de nombres guardada
    private RutasXML() {}
}
