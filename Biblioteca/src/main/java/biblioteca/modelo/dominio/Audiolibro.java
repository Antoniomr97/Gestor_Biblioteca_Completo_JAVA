package biblioteca.modelo.dominio;

import java.time.Duration;

public class Audiolibro extends Libro {
    private Duration duracion;
    private String formato;

    // Constructor principal
    public Audiolibro(String isbn, String titulo, int anio, Categoria categoria,
                      Duration duracion, String formato) {
        super(isbn, titulo, anio, categoria);
        this.duracion = duracion;
        this.formato = formato;
    }


    // Constructor copia
    public Audiolibro(Audiolibro a) {
        super(a);
        this.duracion = a.duracion;
        this.formato = a.formato;
    }

    // Getters y setters
    public Duration getDuracion() { return duracion; }
    public void setDuracion(Duration duracion) { this.duracion = duracion; }

    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }

    @Override
    public String toString() {
        String dur = String.format("%02d:%02d:%02d",
                duracion.toHours(),
                duracion.toMinutesPart(),
                duracion.toSecondsPart());
        return super.toString() +
                " - Duración: " + dur +
                " - Formato: " + formato;
    }
}
