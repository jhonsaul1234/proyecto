package tienda.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.context.ConfigurableApplicationContext;
import tienda.model.DetalleVenta;
import tienda.model.Venta;
import tienda.service.BoletaService;
import tienda.model.Producto;
import tienda.service.CarritoService;
import tienda.service.ProductoService;
import tienda.service.VentaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class VentaController implements Initializable {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private BoletaService boletaService;

    @FXML
    private ComboBox<Producto> cboProductos;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtImporte;

    @FXML
    private Spinner<Integer> spnCantidad;

    @FXML
    private TableView<DetalleVenta> tblCarrito;

    @FXML
    private TableColumn<DetalleVenta, String> colProducto;

    @FXML
    private TableColumn<DetalleVenta, Integer> colCantidad;

    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colPrecio;

    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colSubtotal;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnAgregar;

    @FXML
    private Button btnQuitar;

    @FXML
    private Button btnFinalizar;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar el ComboBox para mostrar solo el nombre del producto
        cboProductos.setConverter(new StringConverter<Producto>() {
            @Override
            public String toString(Producto producto) {
                return producto != null ? producto.getNombre() : "";
            }

            @Override
            public Producto fromString(String string) {
                return null;
            }
        });

        // Listener para actualizar precio cuando cambia el producto seleccionado
        cboProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtPrecio.setText(String.format("S/ %.2f", newVal.getPrecio()));
                // Ajustar spinner según el stock disponible
                SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, newVal.getStock(), 1
                );
                spnCantidad.setValueFactory(valueFactory);
                actualizarImporte();
            } else {
                txtPrecio.clear();
                txtImporte.clear();
                // Resetear spinner cuando no hay producto seleccionado
                spnCantidad.getValueFactory().setValue(1);
            }
        });

        // Configurar el spinner de cantidad inicial
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spnCantidad.setValueFactory(valueFactory);
        // Permitir edición directa del spinner
        spnCantidad.setEditable(true);

        // Validar entrada manual del spinner
        spnCantidad.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                spnCantidad.getEditor().setText(oldValue);
            }
        });

        // Listener para actualizar importe cuando cambia la cantidad
        spnCantidad.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarImporte();
        });

        // Configurar columnas de la tabla
        colProducto.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Formatear columnas de precios con símbolo de moneda
        colPrecio.setCellFactory(tc -> new TableCell<DetalleVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("S/ %.2f", price));
                }
            }
        });

        colSubtotal.setCellFactory(tc -> new TableCell<DetalleVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("S/ %.2f", price));
                }
            }
        });

        // Configurar la tabla y botones
        tblCarrito.setItems(FXCollections.observableArrayList(carritoService.getItems()));
        btnQuitar.setDisable(true);

        // Mejorar la selección de la tabla
        tblCarrito.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    btnQuitar.setDisable(newSelection == null);
                    if (newSelection != null) {
                        // Opcional: deshabilitar ComboBox mientras hay una selección
                        // cboProductos.setDisable(true);
                    } else {
                        cboProductos.setDisable(false);
                    }
                });

        // Agregar listener para doble clic en la tabla para quitar producto
        tblCarrito.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblCarrito.getSelectionModel().getSelectedItem() != null) {
                handleQuitarProducto();
            }
        });

        // Cargar productos iniciales
        cargarProductos();

        // Inicializar campos
        txtPrecio.setEditable(false);
        txtImporte.setEditable(false);
    }

    private void actualizarImporte() {
        Producto productoSeleccionado = cboProductos.getValue();
        if (productoSeleccionado != null && spnCantidad.getValue() != null) {
            BigDecimal precio = productoSeleccionado.getPrecio();
            Integer cantidad = spnCantidad.getValue();
            BigDecimal importe = precio.multiply(BigDecimal.valueOf(cantidad));
            txtImporte.setText(String.format("S/ %.2f", importe));  // Agregamos "S/ "
        } else {
            txtImporte.clear();
        }
    }

    @FXML
    private void handleAgregarProducto() {
        try {
            Producto producto = cboProductos.getValue();
            if (producto == null) {
                mostrarError("Error", "Debe seleccionar un producto");
                return;
            }

            Integer cantidad = spnCantidad.getValue();
            if (cantidad == null || cantidad <= 0) {
                mostrarError("Error", "La cantidad debe ser mayor a 0");
                return;
            }

            // Agregar el producto
            carritoService.agregarProducto(producto, cantidad);

            // Actualizar la interfaz
            actualizarTablaCarrito();
            actualizarTotal();

            // Limpiar campos
            limpiarCampos();

        } catch (Exception e) {
            mostrarError("Error", e.getMessage());
        }
    }

    private void limpiarCampos() {
        cboProductos.setValue(null);
        spnCantidad.getValueFactory().setValue(1);
        txtPrecio.clear();
        txtImporte.clear();
    }

    @FXML
    private void handleQuitarProducto() {
        int selectedIndex = tblCarrito.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            carritoService.removerProducto(selectedIndex);
            actualizarTablaCarrito();
            actualizarTotal();
        }
    }

    @FXML
    private void handleFinalizarVenta() {
        try {
            if (carritoService.getItems().isEmpty()) {
                mostrarError("Error", "El carrito está vacío");
                return;
            }

            // Confirmar venta
            if (!mostrarConfirmacion("Confirmar Venta",
                    "¿Está seguro de finalizar la venta?")) {
                return;
            }

            // Procesar venta
            Venta venta = ventaService.procesarVenta();

            // Generar boleta PDF
            byte[] pdfBoleta = boletaService.generarBoleta(venta);

            // Mostrar ventana de boleta
            mostrarVentanaBoleta(venta, pdfBoleta);

            // Actualizar interfaz principal
            actualizarTablaCarrito();
            actualizarTotal();
            cargarProductos();

        } catch (Exception e) {
            mostrarError("Error", "Error al procesar la venta: " + e.getMessage());
        }
    }

    private void mostrarVentanaBoleta(Venta venta, byte[] pdfBoleta) {
        try {
            // Intentar diferentes rutas posibles
            URL fxmlUrl = null;
            String[] posiblesRutas = {
                    "/tienda/ui/boleta.fxml",
                    "/fxml/boleta.fxml",
                    "/boleta.fxml",
                    "boleta.fxml"
            };

            for (String ruta : posiblesRutas) {
                fxmlUrl = getClass().getResource(ruta);
                if (fxmlUrl != null) {
                    System.out.println("FXML encontrado en: " + ruta);
                    break;
                }
            }

            if (fxmlUrl == null) {
                throw new RuntimeException("No se pudo encontrar el archivo FXML de la boleta");
            }

            // Verificar productos
            if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
                throw new RuntimeException("La venta no tiene detalles");
            }

            System.out.println("Mostrando boleta para venta " + venta.getId() +
                    " con " + venta.getDetalles().size() + " detalles");

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(applicationContext::getBean);

            try {
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Boleta de Venta N° " + venta.getId());
                stage.setScene(new Scene(root));

                BoletaController controller = loader.getController();
                if (controller == null) {
                    throw new RuntimeException("No se pudo obtener el controlador de la boleta");
                }

                controller.setVenta(venta, pdfBoleta);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace(); // Esto ayudará a ver el error en la consola
                throw new RuntimeException("Error al cargar la ventana de boleta: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "Error al mostrar la boleta: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        cboProductos.setItems(
                FXCollections.observableArrayList(productoService.obtenerProductosDisponibles())
        );
    }

    private void actualizarTablaCarrito() {
        // Forzar actualización de la tabla
        tblCarrito.getItems().clear();
        tblCarrito.setItems(FXCollections.observableArrayList(carritoService.getItems()));
        tblCarrito.refresh();
    }

    private void actualizarTotal() {
        lblTotal.setText(String.format("Total: S/. %.2f", carritoService.getTotal()));
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

    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait().get() == ButtonType.OK;
    }
}
