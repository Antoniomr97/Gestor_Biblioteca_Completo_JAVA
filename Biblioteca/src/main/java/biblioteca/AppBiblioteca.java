package biblioteca;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.Modelo;
import biblioteca.vista.Vista;

public class AppBiblioteca {
    public static void main(String[] args) {
        // Configuramos nuestro sistema siguiendo el patrón MVC
        // Ya no necesitamos pasar capacidad porque usamos ArrayLists
        Modelo modelo = new Modelo();
        Vista vista = new Vista();
        Controlador controlador = new Controlador(modelo, vista);

        // Le decimos a la vista qué controlador usar
        vista.setControlador(controlador);

        // Iniciamos la ejecución desde el controlador
        controlador.comenzar();
    }
}
