package frescuratotal.erp.suppliers.analytics;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplierAnalyticsViewTest {
    private String render(SupplierAnalyticsRepository repository) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        SupplierAnalyticsController controller = new SupplierAnalyticsController(repository, new SupplierAnalyticsService());
        new SupplierAnalyticsView(controller, new PrintStream(bytes, true, StandardCharsets.UTF_8)).showReport();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test public void explainsEmptyInventoryWithoutPrintingAnInventedSupplier() {
        String output = render(List::of);
        assertTrue(output, output.contains("No hay datos de inventario suficientes para generar estadísticas."));
        assertFalse(output, output.contains(">> Proveedor con Mayor Volumen"));
    }

    @Test public void displaysLeaderVolumeAndEachExpiredNameOnce() {
        String output = render(() -> List.of(
                new SupplierLotSnapshot(1, new BigDecimal("15.25"), "Vencido", 1, "Proveedor vencido"),
                new SupplierLotSnapshot(2, new BigDecimal("20.25"), "Vencido", 1, "Proveedor vencido"),
                new SupplierLotSnapshot(3, new BigDecimal("30.00"), "Disponible", 2, "Otro proveedor")));
        assertTrue(output, output.contains("Volumen total suministrado (cantidad inicial): 35.50"));
        assertEquals(1, output.split("- Proveedor vencido", -1).length - 1);
        assertFalse(output, output.contains("- Otro proveedor"));
    }

    @Test public void distinguishesNoExpiredLotsFromEmptyInventory() {
        String output = render(() -> List.of(new SupplierLotSnapshot(1, BigDecimal.TEN, "Disponible", 1, "Proveedor disponible")));
        assertTrue(output, output.contains("Proveedor disponible"));
        assertTrue(output, output.contains("No hay proveedores con lotes vencidos registrados."));
        assertFalse(output, output.contains("No hay datos de inventario suficientes"));
    }

    @Test public void reportsDatabaseFailureWithoutPretendingInventoryIsEmpty() {
        String output = render(() -> { throw new SQLException("Fallo de conexión de prueba"); });
        assertTrue(output, output.contains("No se pudo generar el reporte de proveedores."));
        assertFalse(output, output.contains("No hay datos de inventario suficientes"));
    }
}
