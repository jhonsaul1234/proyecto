package tienda.controller;

import org.springframework.stereotype.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tienda.model.DetalleVenta;
import tienda.model.Venta;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Controller  // Agregar esta anotación
public class BoletaController implements Initializable {

    @FXML private Label lblNumeroVenta;
    @FXML private Label lblFecha;
    @FXML private Label lblTotal;
    @FXML private TableView<DetalleVenta> tblDetalles;
    @FXML private TableColumn<DetalleVenta, String> colProducto;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colPrecio;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colSubtotal;
    @FXML private Button btnImprimir;  // Cambiar nombre según FXML
    @FXML private Button btnCerrar;    // Agregar según FXML

    private byte[] pdfBoleta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar las columnas
        colProducto.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getProducto().getNombre()
                ));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Formatear columnas de precios
        colPrecio.setCellFactory(tc -> new TableCell<DetalleVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("S/ %.2f", price));
            }
        });

        colSubtotal.setCellFactory(tc -> new TableCell<DetalleVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("S/ %.2f", price));
            }
        });
    }

    public void setVenta(Venta venta, byte[] pdfBoleta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser null");
        }

        this.pdfBoleta = pdfBoleta;

        lblNumeroVenta.setText(String.valueOf(venta.getId()));
        lblFecha.setText(venta.getFecha().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        ));
        lblTotal.setText(String.format("S/ %.2f", venta.getTotal()));

        if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
            tblDetalles.setItems(javafx.collections.FXCollections.observableArrayList(venta.getDetalles()));
        } else {
            System.out.println("La venta no tiene detalles");
        }
    }

    @FXML
    private void handleImprimirPDF() {
        if (pdfBoleta == null) {
            mostrarError("Error", "No hay PDF para guardar");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Boleta PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(btnImprimir.getScene().getWindow());

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfBoleta);
                mostrarInformacion("Éxito", "PDF guardado correctamente");
            } catch (Exception e) {
                mostrarError("Error", "Error al guardar el PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}