package biblioteca.vista.gui;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.dominio.Usuario;
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

public class UsuariosController implements Initializable {

    @FXML private TableView<Usuario> tableUsuarios;
    
    private Controlador controlador;
    private ObservableList<Usuario> usuariosList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.controlador = FxApplication.getControlador();
        this.usuariosList = FXCollections.observableArrayList();
        tableUsuarios.setItems(usuariosList);
        refreshData();
    }

    public void refreshData() {
        if (controlador != null) {
            usuariosList.setAll(controlador.listadoUsuarios());
        }
    }

    @FXML
    private void handleAddUsuario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/gui/user-form.fxml"));
            VBox form = loader.load();
            
            UserFormController formCtrl = loader.getController();
            formCtrl.setUsuariosController(this);

            Stage stage = new Stage();
            stage.setTitle("Nuevo Usuario");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(form));
            formCtrl.setStage(stage);
            
            stage.showAndWait();
        } catch (Exception ex) {
            showError("Error Crítico", "No se pudo abrir el formulario: " + ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteUsuario() {
        Usuario seleccionado = tableUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showError("Atención", "Selecciona un usuario en la tabla primero.");
            return;
        }
        if (confirmDialog("Confirmar Borrado", "¿Seguro que quieres eliminar a " + seleccionado.getNombre() + "?")) {
            try {
                if (controlador.baja(seleccionado)) {
                    usuariosList.remove(seleccionado); // Refresh automático FXML
                    showInfo("Borrado", "Usuario eliminado de la base de datos.");
                } else {
                    showError("Error al borrar", "No se pudo eliminar el usuario.");
                }
            } catch (RuntimeException ex) {
                showError("No se puede eliminar", ex.getMessage());
            }
        }
    }

    private void showError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void showInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private boolean confirmDialog(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
