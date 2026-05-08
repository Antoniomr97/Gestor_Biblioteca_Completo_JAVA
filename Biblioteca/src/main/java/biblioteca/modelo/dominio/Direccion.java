package biblioteca.modelo.dominio;

// Inicializamos la clase
public class Direccion {
    /*
     * Definimos la expresión regular para la direccion:
     * ^ haciendo que empiece desde el primer caracter
     * //d indica que tiene que haber un numero real (con /d podria ser un caracter
     * especial)
     * {5} exactamente 5 veces
     * $ indica el final de la cadena
     */
    public static final String CP_PATTERN = "^\\d{5}$";

    // Definimos los atributos
    private String via;
    private String numero;
    private String cp;
    private String localidad;

    // Creamos un constructor
    public Direccion(String via, String numero, String cp, String localidad) {
        /*
         * CORRECCIÓN 5: Constructores ignorando Setters.
         * Antes se asignaban campos directos como this.via = via, esto reducía la
         * robustez de la clase
         * al saltarse el filtro de seguridad de los métodos setVia(), setNumero() y
         * setLocalidad().
         */
        setCp(cp);
        setVia(via);
        setNumero(numero);
        setLocalidad(localidad);
    }

    // Getters y Setter, no tienen validadores los setters porque voy a intentar
    // ponerlos en las llamadas a estos.
    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        if (via == null || via.trim().isEmpty()) {
            throw new IllegalArgumentException("La vía no puede ser null ni vacía");
        }
        this.via = via;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("El número no puede ser null ni vacío");
        }
        this.numero = numero;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        if (cp == null || cp.trim().isEmpty()) {
            throw new IllegalArgumentException("El CP no puede ser null ni vacío");
        }
        // Añadimos validacion con expresion regular para el criterio 6h, añadimos un
        // error para parar el programa si se introduce mal
        if (!cp.matches(CP_PATTERN))
            throw new IllegalArgumentException("CP inválido");
        this.cp = cp;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        if (localidad == null || localidad.trim().isEmpty()) {
            throw new IllegalArgumentException("La localidad no puede ser null ni vacía");
        }
        this.localidad = localidad;
    }

    @Override
    public String toString() {
        return via + " " + numero + ", " + cp + " " + localidad;
    }
}
