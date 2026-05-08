package biblioteca.vista;

import biblioteca.modelo.dominio.*;
import biblioteca.utilidades.Entrada;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

// Esta clase se encarga de toda la interacción con el usuario por consola
// Es estática, no hace falta crear objetos de Consola
public class Consola {

    private Consola() {} // Constructor privado según diagrama, no queremos instancias

    // --- MENÚ PRINCIPAL ---
    public static void mostrarMenu() {
        System.out.println("\n" + "=".repeat(30));
        System.out.println("   GESTION DE BIBLIOTECA");
        System.out.println("=".repeat(30));

        // Recorremos todas las opciones del enum Opcion y las mostramos
        for (Opcion op : Opcion.values()) {
            System.out.printf("%2d.- %s%n", op.ordinal(), op.toString().replace("_", " "));
        }

        System.out.print("\nSeleccione una opcion: "); // Pedimos que el usuario elija
    }

    // --- CREACIÓN DE USUARIOS ---
    public static Usuario nuevoUsuario(boolean paraBuscar) {
        // Pedimos el ID y validamos con el patrón definido en Usuario
        String id = leerValidado("ID Usuario (ej. AB123): ", Usuario.ID_PATTERN);

        if (paraBuscar) {
            // Si es solo para buscar, devolvemos un objeto "mínimo" con ID
            return new Usuario(id, "Buscando", "temp@mail.com", new Direccion("C", "0", "00000", "L"));
        }

        // Pedimos los datos completos
        String nombre = leerObligatorio("Nombre");
        String email = leerValidado("Email: ", Usuario.EMAIL_BASIC);

        System.out.println("--- Direccion ---");
        String via = leerObligatorio("Calle/Via");
        String num = leerObligatorio("Numero");
        String cp = leerValidado("Codigo Postal (5 digitos): ", Direccion.CP_PATTERN);
        String loc = leerObligatorio("Localidad");

        return new Usuario(id, nombre, email, new Direccion(via, num, cp, loc));
    }

    // --- CREACIÓN DE LIBROS ---
    public static Libro nuevoLibro(boolean paraBuscar) {
        // Pedimos ISBN y validamos
        String isbn = leerValidado("ISBN (13 digitos): ", Libro.ISBN_PATTERN);

        if (paraBuscar) {
            return new Libro(isbn, "Buscando", 2024, Categoria.OTROS);
        }

        // Preguntar al usuario el tipo de libro
        System.out.print("¿Desea crear un audiolibro? (s/n): ");
        boolean esAudiolibro = Entrada.cadena().equalsIgnoreCase("s");

        // Datos comunes
        String titulo = leerObligatorio("Titulo");
        System.out.print("Anio publicacion: ");
        int anio = Entrada.entero();
        Categoria cat = elegirCategoria();

        // Pedimos autores
        ArrayList<Autor> autores = new ArrayList<>();
        boolean seguir = true;
        int i = 1;
        while (seguir) {
            System.out.println("Autor " + i + ":");
            autores.add(nuevoAutor());
            i++;
            System.out.print("¿Desea añadir otro autor? (s/n): ");
            if (!Entrada.cadena().equalsIgnoreCase("s")) seguir = false;
        }

        if (esAudiolibro) {
            // Datos específicos de Audiolibro
            System.out.println("Introduzca la duracion en formato hh:mm:ss (ejemplo: 01:23:53): ");
            Duration duracion = leerDuracion();

            System.out.print("Formato (mp3, mp4B, AA, AAX...): ");
            String formato = Entrada.cadena();

            Audiolibro a = new Audiolibro(isbn, titulo, anio, cat, duracion, formato);
            for (Autor autor : autores) a.addAutor(autor);
            return a;
        } else {
            Libro l = new Libro(isbn, titulo, anio, cat);
            for (Autor autor : autores) l.addAutor(autor);
            return l;
        }
    }




    // Metodo auxiliar para crear un autor
    private static Autor nuevoAutor() {
        String nom = leerObligatorio("Nombre Autor");
        String ape = leerObligatorio("Apellidos Autor");
        String nac = leerObligatorio("Nacionalidad");
        return new Autor(nom, ape, nac);
    }

    // Metodo para leer la fecha (aquí siempre devuelve la fecha actual)
    public static LocalDate leerFecha() {
        return LocalDate.now();
    }

    // --- MÉTODOS AUXILIARES DE VALIDACIÓN ---
    // Pedimos un dato obligatorio (no vacío)
    private static String leerObligatorio(String msg) {
        while (true) {
            System.out.print(msg + ": ");
            String s = Entrada.cadena();
            if (s != null && !s.trim().isEmpty()) return s;
            System.out.println("Error: El campo no puede estar vacio.");
        }
    }

    // Pedimos un dato que cumpla un patrón regex
    private static String leerValidado(String msg, String regex) {
        while (true) {
            String s = leerObligatorio(msg); // Primero pedimos que no esté vacío
            if (s.matches(regex)) return s;   // Si cumple el patrón, devolvemos
            System.out.println("Error: El formato no coincide con el requerido.");
        }
    }

    // Elegir categoría de libro de entre las opciones disponibles
    private static Categoria elegirCategoria() {

        Categoria[] vals = Categoria.values();

        for (int i = 0; i < vals.length; i++) {
            System.out.println((i + 1) + ".- " + vals[i]);
        }

        int op;
        do {
            System.out.print("Elige categoría: ");
            op = Entrada.entero();
        } while (op < 1 || op > vals.length);

        return vals[op - 1];
    }

    //Leer Duración AudioLibro
    private static Duration leerDuracion() {
        while (true) {
            try {
                System.out.print("Duración (hh:mm:ss): ");
                String input = Entrada.cadena();

                String[] partes = input.trim().split(":");

                if (partes.length != 3) {
                    throw new IllegalArgumentException();
                }

                long horas = Long.parseLong(partes[0]);
                long minutos = Long.parseLong(partes[1]);
                long segundos = Long.parseLong(partes[2]);

                // Validamos rangos
                if (minutos < 0 || minutos >= 60 || segundos < 0 || segundos >= 60) {
                    throw new IllegalArgumentException();
                }

                return Duration.ofHours(horas)
                        .plusMinutes(minutos)
                        .plusSeconds(segundos);

            } catch (Exception e) {
                System.out.println("Error: formato incorrecto. Usa hh:mm:ss (ej: 01:30:45)");
            }
        }
    }

}
