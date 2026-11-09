package frescuratotal.erp.suppliers.analytics;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.support.SqlTestDatabase;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplierAnalyticsMySqlTest {
    @Test public void mapsOfficialTablesAndUsesInitialQuantityAndStoredStatus() throws Exception {
        String schema = System.getenv("ERP_TEST_SCHEMA_FILE");
        Assume.assumeTrue(schema != null);
        try (Connection setup = SqlTestDatabase.create(); Statement statement = setup.createStatement()) {
            SqlTestDatabase.minimal(statement, Path.of(schema));
            String database = setup.getCatalog();
            JdbcSupplierAnalyticsRepository repository = new JdbcSupplierAnalyticsRepository(() -> {
                Connection connection = DatabaseConnection.getInstance().getConnection();
                connection.setCatalog(database);
                return connection;
            });
            SupplierAnalyticsService service = new SupplierAnalyticsService();
            assertTrue(service.analyze(repository.findAllLots()).isEmpty());

            statement.executeUpdate("INSERT INTO proveedores(id_proveedor,nombre,nit,email) VALUES"
                    + "(1,'Proveedor A','TEST-A','a@example.invalid'),(2,'Proveedor B','TEST-B','b@example.invalid')");
            statement.executeUpdate("INSERT INTO productos(id_producto,nombre,id_categoria,unidad_medida) VALUES(1,'Producto de prueba',1,'unidad')");
            statement.executeUpdate("INSERT INTO productos_proveedor(id_proveedor,id_producto) VALUES(1,1),(2,1)");
            statement.executeUpdate("INSERT INTO lotes_inventario(numero_lote,id_producto,id_proveedor,fecha_vencimiento,cantidad_inicial,cantidad,precio_compra,precio_venta,id_estado) VALUES"
                    + "('AN-1',1,1,DATE_ADD(CURDATE(),INTERVAL 30 DAY),10.10,0,1,2,(SELECT id_estado FROM estados_lote WHERE nombre='Vencido')),"
                    + "('AN-2',1,1,DATE_ADD(CURDATE(),INTERVAL 30 DAY),20.20,1,1,2,(SELECT id_estado FROM estados_lote WHERE nombre='Vencido')),"
                    + "('AN-3',1,2,DATE_SUB(CURDATE(),INTERVAL 1 DAY),25.00,25,1,2,(SELECT id_estado FROM estados_lote WHERE nombre='Disponible'))");

            List<SupplierLotSnapshot> snapshots = repository.findAllLots();
            assertEquals(3, snapshots.size());
            assertEquals(1, snapshots.get(0).getSupplierId());
            assertEquals("Vencido", snapshots.get(0).getStatusName());
            assertEquals(0, new BigDecimal("10.10").compareTo(snapshots.get(0).getInitialQuantity()));
            SupplierAnalyticsReport report = service.analyze(snapshots);
            assertEquals("Proveedor A", report.getLeadingSupplier().orElseThrow().getName());
            assertEquals(0, new BigDecimal("30.30").compareTo(report.getLeadingSupplier().orElseThrow().getSuppliedVolume()));
            assertEquals(List.of("Proveedor A"), report.getExpiredSupplierNames());
            try (ResultSet result = statement.executeQuery("SELECT cantidad,e.nombre FROM lotes_inventario l JOIN estados_lote e ON e.id_estado=l.id_estado WHERE numero_lote='AN-1'")) {
                assertTrue(result.next());
                assertEquals(0, result.getBigDecimal(1).signum());
                assertEquals("Vencido", result.getString(2));
            }
        }
    }
}
