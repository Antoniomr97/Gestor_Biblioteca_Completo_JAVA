package biblioteca.modelo.dominio;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo implements Comparable<Prestamo> {
    // Atributos del préstamo usando Properties
    private final ObjectProperty<LocalDate> fInicio = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> fLimite = new SimpleObjectProperty<>();
    private final BooleanProperty devuelto = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDate> fDevolucion = new SimpleObjectProperty<>();
    private final ObjectProperty<Libro> libro = new SimpleObjectProperty<>();
    private final ObjectProperty<Usuario> usuario = new SimpleObjectProperty<>();

    // Constructor principal
    public Prestamo(Libro libro, Usuario usuario, LocalDate fInicio) {
        this.libro.set(libro);
        this.usuario.set(usuario);
        this.fInicio.set(fInicio);
        this.fLimite.set(fInicio.plusDays(15));
        this.devuelto.set(false);
        this.fDevolucion.set(null);
    }

    // --- GETTERS ---
    public Libro getLibro() {
        return libro.get();
    }
    public ObjectProperty<Libro> libroProperty() {
        return libro;
    }

    public Usuario getUsuario() {
        return usuario.get();
    }
    public ObjectProperty<Usuario> usuarioProperty() {
        return usuario;
    }

    public LocalDate getFechaPrestamo() {
        return fInicio.get();
    }
    public ObjectProperty<LocalDate> fechaPrestamoProperty() {
        return fInicio;
    }

    public LocalDate getfLimite() {
        return fLimite.get();
    }
    public ObjectProperty<LocalDate> fLimiteProperty() {
        return fLimite;
    }

    public Boolean isDevuelto() {
        return devuelto.get();
    }
    public BooleanProperty devueltoProperty() {
        return devuelto;
    }

    public LocalDate getfDevolucion() {
        return fDevolucion.get();
    }
    public ObjectProperty<LocalDate> fDevolucionProperty() {
        return fDevolucion;
    }

    // Métodos virtuales para facilitar mostrar el estado en tablas FXML
    public String getEstado() {
        return isDevuelto() ? "✅ Devuelto" : "⏳ Pendiente";
    }
    public StringProperty estadoProperty() {
        return new SimpleStringProperty(getEstado());
    }

    // --- SETTERS necesarios para cargar desde BD ---
    public void setfLimite(LocalDate fLimite) {
        this.fLimite.set(fLimite);
    }

    public void setfDevolucion(LocalDate fDevolucion) {
        this.fDevolucion.set(fDevolucion);
    }

    public void setDevuelto(Boolean devuelto) {
        this.devuelto.set(devuelto);
    }

    // --- MÉTODOS ---
    public int diasRetraso() {
        LocalDate fechaComparar = isDevuelto() ? getfDevolucion() : LocalDate.now();
        int dias = (int) ChronoUnit.DAYS.between(getfLimite(), fechaComparar);
        return dias > 0 ? dias : 0;
    }

    public Boolean estaVencido() {
        return !isDevuelto() && LocalDate.now().isAfter(getfLimite());
    }

    public void marcarDevuelto(LocalDate fecha) {
        /*
         * CORRECCIÓN 4: Efectos laterales no persistentes.
         * Antes, aquí se ejecutaba `libro.devolverUnidad();`.
         * Dado que MySQL no tiene columna de stock, hacer esto en memoria creaba
         * una falsa sensación de disponibilidad que luego no servía de nada,
         * desincronizando la realidad de la base de datos de la del programa.
         */
        if (!isDevuelto()) {
            setfDevolucion(fecha);
            setDevuelto(true);
        } else {
            throw new IllegalStateException("El préstamo ya ha sido devuelto previamente.");
        }
    }

    // --- IMPLEMENTACIÓN DE COMPARABLE ---
    @Override
    public int compareTo(Prestamo otro) {
        int compFecha = otro.getFechaPrestamo().compareTo(this.getFechaPrestamo());
        if (compFecha != 0)
            return compFecha;
        return this.getUsuario().getNombre().compareToIgnoreCase(otro.getUsuario().getNombre());
    }

    @Override
    public String toString() {
        return "Préstamo de: " + getLibro().getTitulo() +
                " a " + getUsuario().getNombre() +
                " - Inicio: " + getFechaPrestamo() +
                ", Límite: " + getfLimite() +
                ", Devuelto: " + isDevuelto() +
                (isDevuelto() ? ", Fecha devolución: " + getfDevolucion() : "");
    }
}