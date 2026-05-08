package biblioteca.vista.gui;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.dominio.Libro;
import biblioteca.modelo.dominio.Prestamo;
import biblioteca.modelo.dominio.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TableView;

import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PrestamosController implements Initializable {

    @FXML private TableView<Prestamo> tablePrestamos;
    
    private Controlador controlador;
    private ObservableList<Prestamo> prestamosList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.controlador = FxApplication.getControlador();
        this.prestamosList = FXCollections.observableArrayList();
        tablePrestamos.setItems(prestamosList);
        refreshData();
    }

    public void refreshData() {
        if (controlador != null) {
            prestamosList.setAll(controlador.listadoPrestamos());
        }
    }

    @FXML
    private void handleAddPrestamo() {
        List<Usuario> listaU = Arrays.asList(controlador.listadoUsuarios());
        ChoiceDialog<Usuario> dialogU = new ChoiceDialog<>(null, listaU);
        dialogU.setTitle("Seleccionar Usuario");
        dialogU.setHeaderText("¿Quién va a llevarse el libro?");
        
        dialogU.showAndWait().ifPresent(u -> {
            List<Libro> listaL = Arrays.asList(controlador.listadoLibros());
            ChoiceDialog<Libro> dialogL = new ChoiceDialog<>(null, listaL);
            dialogL.setTitle("Seleccionar Libro");
            dialogL.setHeaderText("¿Qué libro desea el usuario?");
            
            dialogL.showAndWait().ifPresent(l -> {
                if (controlador.prestar(l, u, LocalDate.now())) {
                    showInfo("Préstamo", "Operación realizada con éxito.");
                    refreshData();
                } else {
                    showError("Error", "No se pudo registrar el préstamo.");
                }
            });
        });
    }

    @FXML
    private void handleReturnPrestamo() {
        Prestamo selec = tablePrestamos.getSelectionModel().getSelectedItem();
        if (selec == null) {
            showError("Selección", "Elige un préstamo del listado para marcar la devolución.");
            return;
        }
        
        if (selec.isDevuelto()) {
            showInfo("Aviso", "Este préstamo ya figura como devuelto.");
            return;
        }

        if (controlador.devolver(selec.getLibro(), selec.getUsuario(), LocalDate.now())) {
            showInfo("Devolución", "Libro devuelto correctamente.");
            refreshData();
        } else {
            showError("Error", "No se pudo procesar la devolución.");
        }
    }

    private void showError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje); alert.showAndWait();
    }
    private void showInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje); alert.showAndWait();
    }
}
