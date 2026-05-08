package biblioteca.vista.gui;

import biblioteca.controlador.Controlador;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador de la ventana principal.
 * Gestiona la navegación entre pestañas y las acciones del menú Archivo
 * (Hacer copia de seguridad / Cargar copia de seguridad).
 */
public class MainController implements Initializable {

    // Contenedores inyectados mediante fx:include
    @FXML private VBox usuariosTab;
    @FXML private VBox librosTab;
    @FXML private VBox prestamosTab;

    // Controladores inyectados de cada tab (nombrados automágicamente agregando 'Controller' al ID del fx:include)
    @FXML private UsuariosController  usuariosTabController;
    @FXML private LibrosController    librosTabController;
    @FXML private PrestamosController prestamosTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showUsuarios(); // Por defecto mostramos usuarios
    }

    // ==============================
    // NAVEGACIÓN ENTRE PESTAÑAS
    // ==============================

    @FXML
    private void showUsuarios() {
        if (usuariosTab != null) usuariosTab.setVisible(true);
        if (librosTab != null)   librosTab.setVisible(false);
        if (prestamosTab != null) prestamosTab.setVisible(false);
        if (usuariosTabController != null) usuariosTabController.refreshData();
    }

    @FXML
    private void showLibros() {
        if (usuariosTab != null) usuariosTab.setVisible(false);
        if (librosTab != null)   librosTab.setVisible(true);
        if (prestamosTab != null) prestamosTab.setVisible(false);
        if (librosTabController != null) librosTabController.refreshData();
    }

    @FXML
    private void showPrestamos() {
        if (usuariosTab != null) usuariosTab.setVisible(false);
        if (librosTab != null)   librosTab.setVisible(false);
        if (prestamosTab != null) prestamosTab.setVisible(true);
        if (prestamosTabController != null) prestamosTabController.refreshData();
    }

    @FXML
    private void closeApp() {
        Platform.exit();
    }

    // ==============================
    // MENÚ ARCHIVO: COPIA DE SEGURIDAD
    // ==============================

    /**
     * Acción: "Hacer copia de seguridad".
     * Permite al usuario elegir una carpeta donde se guardarán los 4 ficheros XML.
     */
    @FXML
    private void hacerCopiaSeguridad() {
        // 1. El usuario elige la carpeta destino con DirectoryChooser
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecciona la carpeta para la copia de seguridad");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));

        Stage stage = obtenerStage();
        File carpeta = chooser.showDialog(stage);

        if (carpeta == null) {
            // El usuario canceló el diálogo
            return;
        }

        // 2. Confirmar la acción
        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar copia de seguridad");
        confirmacion.setHeaderText("Se generarán 4 ficheros XML en:");
        confirmacion.setContentText(carpeta.getAbsolutePath() +
                "\n\n• Autores.xml\n• Libros.xml\n• Usuarios.xml\n• Prestamos.xml" +
                "\n\n¿Deseas continuar?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        // 3. Ejecutar la exportación
        try {
            Controlador ctrl = FxApplication.getControlador();
            if (ctrl == null) {
                mostrarError("Error", "El controlador no está disponible.");
                return;
            }
            ctrl.hacerCopiaSeguridad(carpeta.getAbsolutePath());

            // 4. Informar al usuario del éxito
            Alert exito = new Alert(AlertType.INFORMATION);
            exito.setTitle("Copia de seguridad completada");
            exito.setHeaderText("✅ Copia de seguridad realizada con éxito");
            exito.setContentText("Los ficheros XML se han guardado en:\n" + carpeta.getAbsolutePath());
            exito.showAndWait();

        } catch (Exception e) {
            mostrarError("Error al hacer copia de seguridad", e.getMessage());
        }
    }

    /**
     * Acción: "Cargar copia de seguridad".
     * Permite al usuario elegir la carpeta que contiene los XML y restaura los datos.
     * ADVERTENCIA: Esta operación borra los datos actuales de la BD.
     */
    @FXML
    private void cargarCopiaSeguridad() {
        // 1. El usuario elige la carpeta origen con DirectoryChooser
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecciona la carpeta con la copia de seguridad");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));

        Stage stage = obtenerStage();
        File carpeta = chooser.showDialog(stage);

        if (carpeta == null) {
            return;
        }

        // 2. Validar que los ficheros XML existen
        String[] ficherosRequeridos = {"Autores.xml", "Libros.xml", "Usuarios.xml", "Prestamos.xml"};
        StringBuilder faltantes = new StringBuilder();
        for (String f : ficherosRequeridos) {
            if (!new File(carpeta, f).exists()) {
                faltantes.append("  • ").append(f).append("\n");
            }
        }
        if (!faltantes.isEmpty()) {
            mostrarError("Ficheros no encontrados",
                    "Faltan los siguientes ficheros XML en la carpeta seleccionada:\n" + faltantes +
                    "\nCarpeta: " + carpeta.getAbsolutePath());
            return;
        }

        // 3. Advertencia: esta acción es DESTRUCTIVA
        Alert aviso = new Alert(AlertType.WARNING);
        aviso.setTitle("⚠️ Advertencia - Restaurar copia de seguridad");
        aviso.setHeaderText("¡ATENCIÓN! Esta acción borrará TODOS los datos actuales.");
        aviso.setContentText(
                "Se eliminarán todos los Usuarios, Libros, Autores y Préstamos actuales\n" +
                "y se restaurarán los datos desde los ficheros XML.\n\n" +
                "Carpeta origen: " + carpeta.getAbsolutePath() +
                "\n\n¿Estás seguro de que deseas continuar?");
        aviso.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> resultado = aviso.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        // 4. Ejecutar la restauración
        try {
            Controlador ctrl = FxApplication.getControlador();
            if (ctrl == null) {
                mostrarError("Error", "El controlador no está disponible.");
                return;
            }
            ctrl.cargarCopiaSeguridad(carpeta.getAbsolutePath());

            // 5. Refrescar todas las vistas para que muestren los datos nuevos
            if (usuariosTabController  != null) usuariosTabController.refreshData();
            if (librosTabController    != null) librosTabController.refreshData();
            if (prestamosTabController != null) prestamosTabController.refreshData();

            // 6. Informar al usuario del éxito
            Alert exito = new Alert(AlertType.INFORMATION);
            exito.setTitle("Restauración completada");
            exito.setHeaderText("✅ Copia de seguridad restaurada con éxito");
            exito.setContentText("Los datos han sido restaurados desde:\n" + carpeta.getAbsolutePath());
            exito.showAndWait();

        } catch (Exception e) {
            mostrarError("Error al cargar la copia de seguridad", e.getMessage());
        }
    }

    // ==============================
    // UTILIDADES PRIVADAS
    // ==============================

    /**
     * Muestra un diálogo de error al usuario.
     *
     * @param titulo  Título de la ventana de error.
     * @param mensaje Mensaje descriptivo del error.
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert error = new Alert(AlertType.ERROR);
        error.setTitle("❌ " + titulo);
        error.setHeaderText(titulo);
        error.setContentText(mensaje != null ? mensaje : "Error desconocido.");
        error.showAndWait();
    }

    /**
     * Obtiene el Stage (ventana) principal para pasar al DirectoryChooser.
     *
     * @return El Stage actual, o null si no se puede obtener.
     */
    private Stage obtenerStage() {
        try {
            // Intentamos obtenerlo desde cualquier nodo del grafo de escena
            if (usuariosTab != null && usuariosTab.getScene() != null) {
                return (Stage) usuariosTab.getScene().getWindow();
            }
            if (librosTab != null && librosTab.getScene() != null) {
                return (Stage) librosTab.getScene().getWindow();
            }
        } catch (Exception e) {
            System.err.println("MainController.obtenerStage: No se pudo obtener el Stage → " + e.getMessage());
        }
        return null;
    }
}
