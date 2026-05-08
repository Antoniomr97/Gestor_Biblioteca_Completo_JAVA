package biblioteca.modelo.dominio;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class Libro implements Comparable<Libro> {

    public static final String ISBN_PATTERN = "^\\d{13}$";

    private final StringProperty isbn = new SimpleStringProperty();
    private final StringProperty titulo = new SimpleStringProperty();
    private final IntegerProperty anio = new SimpleIntegerProperty();
    private final ObjectProperty<Categoria> categoria = new SimpleObjectProperty<>();

    /*
     * CORRECCIÓN 2: Eliminación de atributos fantasmas.
     * Originalmente teníamos aquí "private int unidadesDisponibles;", pero MySQL no
     * tiene esa columna.
     * Guardar estados volátiles en Java que no se reflejan en la BBDD destruye la
     * consistencia del sistema
     * (al reiniciar, todo el stock se perdía).
     * Se ha eliminado este atributo y todos sus métodos (tomarPrestado,
     * devolverUnidad) para que
     * la disponibilidad se calcule exclusivamente de forma dinámica mirando los
     * Préstamos activos.
     */

    private ArrayList<Autor> autores; // ahora usamos ArrayList

    // Constructor principal
    public Libro(String isbn, String titulo, int anio, Categoria categoria) {
        // Validamos que el ISBN no sea null y cumpla el patrón
        // Evita NullPointerException y asegura formato correcto (13 dígitos)
        if (isbn == null || !isbn.matches(ISBN_PATTERN)) {
            throw new IllegalArgumentException("ISBN inválido");
        }

        // Validamos que el título no sea null ni vacío (incluyendo espacios)
        // Evita errores en compareTo y garantiza datos consistentes
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }

        // Validamos que la categoría no sea null
        // Evita estados inválidos del objeto y posibles errores posteriores
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser null");
        }

        // Validación de año (ya la tenías, correcta)
        // Evita datos incoherentes
        if (anio <= 0) {
            throw new IllegalArgumentException("Año inválido");
        }

        this.isbn.set(isbn);
        this.titulo.set(titulo);
        this.anio.set(anio);
        this.categoria.set(categoria);

        this.autores = new ArrayList<>();
    }

    // Constructor copia
    public Libro(Libro libro) {
        this(libro.getIsbn(), libro.getTitulo(), libro.getAnio(), libro.getCategoria());
        if (libro.autores != null)
            this.autores.addAll(libro.autores); // copiamos autores del libro original
    }

    // Añadir un autor
    public void addAutor(Autor autor) {
        if (autor != null && !autores.contains(autor)) {
            autores.add(autor);
        }
    }

    // Obtener autores
    public ArrayList<Autor> getAutores() {
        return new ArrayList<>(autores); // devolvemos copia para no exponer la lista interna
    }

    // Convierte autores a cadena (Didáctico: Get para que la tabla pueda leerlo)
    public String getAutoresComoCadena() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < autores.size(); i++) {
            Autor a = autores.get(i);
            sb.append(a.toString()).append(" [").append(a.iniciales()).append("]");
            if (i < autores.size() - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    // Añadido para enlazamiento directo en la tabla
    public String getTipo() {
        return this instanceof Audiolibro ? "🎵 Audiolibro" : "📖 Libro";
    }

    // Getters básicos con Properties
    public String getIsbn() {
        return isbn.get();
    }
    public StringProperty isbnProperty() {
        return isbn;
    }

    public String getTitulo() {
        return titulo.get();
    }
    public StringProperty tituloProperty() {
        return titulo;
    }

    public Categoria getCategoria() {
        return categoria.get();
    }
    public ObjectProperty<Categoria> categoriaProperty() {
        return categoria;
    }

    public int getAnio() {
        return anio.get();
    }
    public IntegerProperty anioProperty() {
        return anio;
    }

    // Comparable: ordenamos por título alfabéticamente
    @Override
    public int compareTo(Libro otro) {
        return this.getTitulo().compareToIgnoreCase(otro.getTitulo());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Libro))
            return false;
        return this.getIsbn().equals(((Libro) o).getIsbn());
    }

    @Override
    public int hashCode() {
        return this.getIsbn().hashCode();
    }

    @Override
    public String toString() {
        return getTitulo() + " (" + getAnio() + ") - ISBN: " + getIsbn() +
                " - Autores: " + getAutoresComoCadena();
    }
}
