package tienda.service;

import tienda.model.DetalleVenta;
import tienda.model.Producto;
import lombok.Getter;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Getter
public class CarritoService {

    private final List<DetalleVenta> items = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;

    public void agregarProducto(Producto producto, Integer cantidad) {
        if (producto == null || cantidad == null || cantidad <= 0) {
            throw new RuntimeException("Datos de producto inválidos");
        }

        // Validar stock inicial
        if (cantidad > producto.getStock()) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        // Buscar si el producto ya existe
        DetalleVenta detalleExistente = null;
        int indexExistente = -1;

        // Buscar el producto y su índice
        for (int i = 0; i < items.size(); i++) {
            DetalleVenta item = items.get(i);
            if (item.getProducto().getId().equals(producto.getId())) {
                detalleExistente = item;
                indexExistente = i;
                break;
            }
        }

        if (detalleExistente != null) {
            // Calcular nueva cantidad
            int nuevaCantidad = detalleExistente.getCantidad() + cantidad;

            // Validar stock para la cantidad total
            if (nuevaCantidad > producto.getStock()) {
                throw new RuntimeException(
                        String.format("Stock insuficiente. Ya tiene %d en el carrito y el stock disponible es %d",
                                detalleExistente.getCantidad(), producto.getStock())
                );
            }

            // Actualizar el detalle existente
            DetalleVenta detalleActualizado = new DetalleVenta();
            detalleActualizado.setProducto(producto);
            detalleActualizado.setCantidad(nuevaCantidad);
            detalleActualizado.setPrecioUnitario(producto.getPrecio());
            detalleActualizado.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(nuevaCantidad)));

            // Reemplazar el item en la lista
            items.set(indexExistente, detalleActualizado);
        } else {
            // Crear nuevo detalle
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)));

            items.add(detalle);
        }

        recalcularTotal();
    }

    // Remover producto del carrito
    public void removerProducto(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            recalcularTotal();
        }
    }

    // Limpiar carrito
    public void limpiarCarrito() {
        items.clear();
        total = BigDecimal.ZERO;
    }

    // Recalcular total
    private void recalcularTotal() {
        total = items.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<DetalleVenta> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
