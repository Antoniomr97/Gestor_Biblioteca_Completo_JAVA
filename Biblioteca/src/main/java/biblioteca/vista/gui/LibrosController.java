package biblioteca.vista.gui;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.dominio.Libro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LibrosController implements Initializable {

    @FXML private TableView<Libro> tableLibros;
    
    private Controlador controlador;
    private ObservableList<Libro> librosList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.controlador = FxApplication.getControlador();
        this.librosList = FXCollections.observableArrayList();
        tableLibros.setItems(librosList);
        refreshData();
    }

    public void refreshData() {
        if (controlador != null) {
            librosList.setAll(controlador.listadoLibros());
        }
    }

    @FXML
    private void handleAddLibro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/gui/libro-form.fxml"));
            VBox form = loader.load();

            LibroFormController formCtrl = loader.getController();
            formCtrl.setLibrosController(this);

            Stage stage = new Stage();
            stage.setTitle("Nuevo Libro / Audiolibro");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(form));
            formCtrl.setStage(stage);

            stage.showAndWait();
        } catch (Exception ex) {
            showError("Error", "Error al abrir el formulario: " + ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteLibro() {
        Libro seleccionado = tableLibros.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showError("Selección", "Selecciona un libro de la tabla primero.");
            return;
        }
        if (confirmDialog("Confirmar borrado", "¿Eliminar el libro: " + seleccionado.getTitulo() + "?")) {
            try {
                if (controlador.baja(seleccionado)) {
                    librosList.remove(seleccionado); // Refresh automático FXML
                    showInfo("Borrado", "Libro eliminado del catálogo.");
                } else {
                    showError("Error", "No se pudo eliminar el libro.");
                }
            } catch (RuntimeException ex) {
                showError("No se puede eliminar", ex.getMessage());
            }
        }
    }

    private void showError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje); alert.showAndWait();
    }
    private void showInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje); alert.showAndWait();
    }
    private boolean confirmDialog(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
