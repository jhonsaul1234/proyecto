package tienda.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import tienda.model.DetalleVenta;
import tienda.model.Venta;

import java.util.HashMap;
import java.util.Map;

@Service
public class BoletaService {

    public byte[] generarBoleta(Venta venta) {
        try {
            // Cargar el template de la boleta
            JasperReport report = JasperCompileManager.compileReport(
                    getClass().getResourceAsStream("/tienda/report/boleta.jrxml")
            );

            // Preparar los parámetros
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("numeroVenta", venta.getId());
            parameters.put("fecha", venta.getFecha());
            parameters.put("total", venta.getTotal());

            // Asegurarnos de que los detalles tengan todos los datos necesarios
            for (DetalleVenta detalle : venta.getDetalles()) {
                if (detalle.getProducto() == null || detalle.getProducto().getNombre() == null) {
                    throw new RuntimeException("Datos de producto incompletos en el detalle");
                }
            }

            // Crear el datasource con los detalles de la venta
            JRBeanCollectionDataSource dataSource =
                    new JRBeanCollectionDataSource(venta.getDetalles());

            // Generar el reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    report, parameters, dataSource
            );

            // Exportar a PDF
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error detallado
            throw new RuntimeException("Error al generar la boleta: " + e.getMessage());
        }
    }
}