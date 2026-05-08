package biblioteca.vista.gui;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.dominio.Audiolibro;
import biblioteca.modelo.dominio.Autor;
import biblioteca.modelo.dominio.Categoria;
import biblioteca.modelo.dominio.Libro;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LibroFormController implements Initializable {

    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtAnio;
    @FXML private ComboBox<Categoria> comboCat;

    @FXML private TextField txtAutorNombre;
    @FXML private TextField txtAutorApellidos;
    @FXML private TextField txtAutorNacionalidad;
    @FXML private ListView<String> listAutores;
    
    @FXML private CheckBox chkAudiolibro;
    @FXML private VBox panelAudiolibro;
    @FXML private TextField txtDuracion;
    @FXML private TextField txtFormato;

    private List<Autor> autoresList = new ArrayList<>();
    private LibrosController librosController;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboCat.setItems(FXCollections.observableArrayList(Categoria.values()));
    }

    public void setLibrosController(LibrosController librosController) {
        this.librosController = librosController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleAddAutor() {
        try {
            String nombre = txtAutorNombre.getText().trim();
            String apell  = txtAutorApellidos.getText().trim();
            String nac    = txtAutorNacionalidad.getText().trim();
            Autor a = new Autor(nombre, apell, nac);
            autoresList.add(a);
            listAutores.getItems().add(nombre + " " + apell + " (" + nac + ")");
            
            txtAutorNombre.clear();
            txtAutorApellidos.clear();
            txtAutorNacionalidad.clear();
            txtAutorNombre.requestFocus();
        } catch (IllegalArgumentException ex) {
            showError("Autor inválido", ex.getMessage());
        }
    }

    @FXML
    private void toggleAudiolibro() {
        boolean esAudio = chkAudiolibro.isSelected();
        panelAudiolibro.setVisible(esAudio);
        panelAudiolibro.setManaged(esAudio);
        if (stage != null) stage.sizeToScene();
    }

    @FXML
    private void handleGuardar() {
        try {
            int anio = Integer.parseInt(txtAnio.getText().trim());
            Libro libro;
            if (chkAudiolibro.isSelected()) {
                long horas = Long.parseLong(txtDuracion.getText().trim());
                libro = new Audiolibro(
                    txtIsbn.getText(), txtTitulo.getText(),
                    anio, comboCat.getValue(),
                    Duration.ofHours(horas), txtFormato.getText().trim()
                );
            } else {
                libro = new Libro(txtIsbn.getText(), txtTitulo.getText(), anio, comboCat.getValue());
            }

            for (Autor a : autoresList) {
                libro.addAutor(a);
            }

            Controlador controlador = FxApplication.getControlador();
            if (controlador.alta(libro)) {
                showInfo("Éxito", "Libro añadido al catálogo.");
                if (librosController != null) librosController.refreshData();
                if (stage != null) stage.close();
            } else {
                showError("Error", "No se pudo añadir. Verifica si el ISBN está repetido.");
            }

        } catch (NumberFormatException nfe) {
            showError("Error de Formato", "El año y las horas deben ser números válidos.");
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
