package tienda.service;

import tienda.model.Producto;
import tienda.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerProductosDisponibles() {
        return productoRepository.findByStockGreaterThan(0);
    }

    // Actualizar stock después de una venta
    @Transactional
    public boolean actualizarStock(Long productoId, Integer cantidad) {
        return productoRepository.actualizarStock(productoId, cantidad) > 0;
    }
}