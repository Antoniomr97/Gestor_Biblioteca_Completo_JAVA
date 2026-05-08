package biblioteca.modelo.dominio;

/**
 * Representamos a los autores de los libros.
 * Hemos implementado validaciones en los setters para asegurar la integridad.
 */
public class Autor {
    private String nombre;
    private String apellidos;
    private String nacionalidad;

    public Autor(String nombre, String apellidos, String nacionalidad) {
        setNombre(nombre);
        setApellidos(apellidos);
        setNacionalidad(nacionalidad);
    }

    public Autor(Autor autor) {
        this.nombre = autor.nombre;
        this.apellidos = autor.apellidos;
        this.nacionalidad = autor.nacionalidad;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("El nombre no puede ser null ni vacío");
        this.nombre = nombre;
    }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) {
        if (apellidos == null || apellidos.trim().isEmpty()) throw new IllegalArgumentException("Los apellidos no pueden ser null ni vacíos");
        this.apellidos = apellidos;
    }

    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) {
        if (nacionalidad == null || nacionalidad.trim().isEmpty()) throw new IllegalArgumentException("La nacionalidad no puede ser null ni vacía");
        this.nacionalidad = nacionalidad;
    }

    public String getNombreCompleto() { return this.nombre + " " + this.apellidos; }

    /**
     * Obtenemos las iniciales del nombre y de cada parte de los apellidos.
     */
    public String iniciales() {
        String resultado = "";
        if (nombre != null && !nombre.isEmpty()) resultado += nombre.charAt(0) + ". ";
        if (apellidos != null && !apellidos.isEmpty()) {
            String[] partes = apellidos.split(" ");
            for (String parte : partes) {
                if (!parte.isEmpty()) resultado += parte.charAt(0) + ". ";
            }
        }
        return resultado.trim();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof Autor)) return false;
        Autor otro = (Autor) obj;
        return nombre.equals(otro.nombre) && apellidos.equals(otro.apellidos) && nacionalidad.equals(otro.nacionalidad);
    }

    @Override
    public int hashCode() {
        int resultado = nombre.hashCode();
        resultado = 31 * resultado + apellidos.hashCode();
        resultado = 31 * resultado + nacionalidad.hashCode();
        return resultado;
    }

    @Override
    public String toString() {
        return "Autor: " + getNombreCompleto() + " (" + nacionalidad + ")";
    }
}