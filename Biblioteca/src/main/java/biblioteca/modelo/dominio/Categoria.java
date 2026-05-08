package biblioteca.modelo.dominio;

public enum Categoria {
    NOVELA("Novela"),
    ENSAYO("Ensayo"),
    INFANTIL("Infantil"),
    COMIC("Comic"),
    POESIA("Poesia"),
    TECNICO("Tecnico"),
    OTROS("Otros");

    private final String texto;

    Categoria(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
