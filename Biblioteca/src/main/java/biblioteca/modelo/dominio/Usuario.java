package biblioteca.modelo.dominio;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Inicializamos la clase con sus expresiones regulares y atributos. Añadiendo Comparable
public class Usuario implements Comparable<Usuario> {

    /*
     * La expresion regular de la ID
     * ^ Comienza desde el principio de la cadena
     * [A-Z] puedes poner caracteres de A - Z y requiere 2
     * \\d numero real
     * {3} 3 veces
     * $ Fin de la cadena
     */
    public static final String ID_PATTERN = "^[A-Z]{2}\\d{3}$"; // Ejemplo: AB123

    /*
     * La expresion regular de la ID
     * ^ Comienza la cadena
     * [\w.-] cualquier letra, numero o guion bajo, . y -
     * + uno o mas carcateres del conjunto
     * 
     * @ tiene que haber un @
     * \\. cualquier caracter
     * \\w+ uno o mas caracteres de tipo letra, numero o guion bajo
     */
    public static final String EMAIL_BASIC = "^[\\w.-]+@[\\w.-]+\\.\\w+$";

    // Iniciamos los atributos como JavaFX Properties (Corrección de arquitectura FXML)
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final ObjectProperty<Direccion> direccion = new SimpleObjectProperty<>();

    // Creamos el constructor principal
    public Usuario(String id, String nombre, String email, Direccion direccion) {
        setId(id);
        setEmail(email);
        /*
         * CORRECCIÓN 5: Constructores ignorando Setters.
         * Antes se asignaban las variables directamente (this.nombre = nombre), y eso
         * es peligroso
         * porque puentea cualquier validación de seguridad (ej. if nombre != null) que
         * hubiéramos programado abajo.
         */
        setNombre(nombre);
        setDireccion(direccion);
    }

    // Constructor copia
    public Usuario(Usuario usuario) {
        this(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getDireccion());
    }

    // Getter y Setter aplicando las expresiones regulares
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID no puede ser null ni vacío");
        }
        if (!id.matches(ID_PATTERN)) {
            throw new IllegalArgumentException("ID inválido");
        }
        this.id.set(id);
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser null ni vacío");
        }
        this.nombre.set(nombre);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede ser null ni vacío");
        }
        if (!email.matches(EMAIL_BASIC)) {
            throw new IllegalArgumentException("Email invalido");
        }
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public Direccion getDireccion() {
        return direccion.get();
    }

    public void setDireccion(Direccion direccion) {
        if (direccion == null) {
            throw new IllegalArgumentException("La direccion no puede ser null");
        }
        this.direccion.set(direccion);
    }

    public ObjectProperty<Direccion> direccionProperty() {
        return direccion;
    }

    // Comparable: ordenamos alfabéticamente por nombre ignorando
    // mayúsculas/minúsculas
    @Override
    public int compareTo(Usuario otro) {
        return this.getNombre().compareToIgnoreCase(otro.getNombre());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Usuario))
            return false;
        return this.getId().equals(((Usuario) o).getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public String toString() {
        return getNombre() + " (ID: " + getId() + ", Email: " + getEmail() + ", Dirección: " + getDireccion() + ")";
    }
}
