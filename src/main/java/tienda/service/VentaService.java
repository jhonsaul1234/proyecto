package tienda.service;

import tienda.model.DetalleVenta;
import tienda.model.Venta;
import tienda.repository.VentaRepository;
import tienda.repository.DetalleVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {
    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CarritoService carritoService;

    @Transactional
    public Venta procesarVenta() {
        List<DetalleVenta> items = new ArrayList<>(carritoService.getItems()); // Crear una copia de los items
        if (items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Crear nueva venta
        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(carritoService.getTotal());
        venta.setDetalles(new ArrayList<>()); // Inicializar la lista de detalles

        // Asignar la venta a cada detalle y actualizar stock
        for (DetalleVenta detalle : items) {
            DetalleVenta nuevoDetalle = new DetalleVenta(); // Crear nuevo objeto para cada detalle
            nuevoDetalle.setVenta(venta);
            nuevoDetalle.setProducto(detalle.getProducto());
            nuevoDetalle.setCantidad(detalle.getCantidad());
            nuevoDetalle.setPrecioUnitario(detalle.getPrecioUnitario());
            nuevoDetalle.setSubtotal(detalle.getSubtotal());

            if (!productoService.actualizarStock(
                    detalle.getProducto().getId(),
                    detalle.getCantidad()
            )) {
                throw new RuntimeException("Error al actualizar stock del producto: "
                        + detalle.getProducto().getNombre());
            }

            venta.getDetalles().add(nuevoDetalle);
        }

        // Guardar la venta
        venta = ventaRepository.save(venta);

        // Verificar que los detalles se guardaron
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new RuntimeException("Error: Los detalles de la venta no se guardaron correctamente");
        }

        System.out.println("Venta guardada con " + venta.getDetalles().size() + " detalles");

        // Limpiar el carrito DESPUÉS de verificar que todo se guardó correctamente
        carritoService.limpiarCarrito();

        return venta;
    }
}