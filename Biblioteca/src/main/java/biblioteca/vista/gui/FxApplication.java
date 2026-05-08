package biblioteca.vista.gui;

import biblioteca.controlador.Controlador;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Clase principal de JavaFX.
 * Esta clase se encarga de cargar la interfaz visual y mostrar la ventana
 * principal.
 */
public class FxApplication extends Application {

    // Referencia estática al controlador para que la interfaz pueda usarlo
    private static Controlador controlador;

    /**
     * Metodo estático para asignar el controlador antes de lanzar la aplicación.
     * 
     * @param ctrl El controlador de negocio.
     */
    public static void setControlador(Controlador ctrl) {
        controlador = ctrl;
    }

    /**
     * Metodo estático para obtener el controlador desde el controlador de la UI
     * (MainController).
     * 
     * @return El controlador de negocio.
     */
    public static Controlador getControlador() {
        return controlador;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Didáctico: FXMLLoader es la herramienta que "traduce" el archivo XML (FXML) a
            // objetos Java.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/gui/main.fxml"));
            Parent root = loader.load();

            // Creamos la "Escena" (el contenido de la ventana)
            Scene scene = new Scene(root);

            // Configuramos la "Caja" (Stage) que es la ventana física del sistema operativo
            primaryStage.setTitle("Sistema de Gestión de Biblioteca");
            primaryStage.setScene(scene);

            // Evitamos que la ventana se cierre sin avisar al controlador si fuera
            // necesario
            primaryStage.setOnCloseRequest(event -> {
                if (controlador != null) {
                    controlador.terminar();
                }
            });

            // Mostramos la ventana al usuario
            primaryStage.show();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
