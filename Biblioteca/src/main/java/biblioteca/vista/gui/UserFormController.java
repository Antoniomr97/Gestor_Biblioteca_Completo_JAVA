package biblioteca.vista.gui;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.dominio.Direccion;
import biblioteca.modelo.dominio.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserFormController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private TextField txtVia;
    @FXML private TextField txtNumero;
    @FXML private TextField txtCp;
    @FXML private TextField txtLocalidad;

    private UsuariosController usuariosController;
    private Stage stage;

    public void setUsuariosController(UsuariosController usuariosController) {
        this.usuariosController = usuariosController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleGuardar() {
        try {
            Direccion dir = new Direccion(txtVia.getText(), txtNumero.getText(), txtCp.getText(), txtLocalidad.getText());
            Usuario u = new Usuario(txtId.getText(), txtNombre.getText(), txtEmail.getText(), dir);
            
            Controlador controlador = FxApplication.getControlador();
            if (controlador.alta(u)) {
                showInfo("Éxito", "Usuario registrado correctamente.");
                if (usuariosController != null) {
                    usuariosController.refreshData();
                }
                stage.close();
            } else {
                showError("Error", "No se pudo registrar. Comprueba si la ID ya existe.");
            }
        } catch (IllegalArgumentException ex) {
            showError("Datos Incorrectos", ex.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        if (stage != null) stage.close();
    }

    private void showError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje); alert.showAndWait();
    }

    private void showInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje); alert.showAndWait();
    }
}
