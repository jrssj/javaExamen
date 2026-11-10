package frescuratotal.erp.suppliers.analytics;

import frescuratotal.erp.common.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Reads individual lots; all aggregation remains in the Stream service. */
public final class JdbcSupplierAnalyticsRepository implements SupplierAnalyticsRepository {
    @FunctionalInterface
    public interface ConnectionProvider {
        Connection open() throws SQLException;
    }

    private final ConnectionProvider connections;

    public JdbcSupplierAnalyticsRepository() {
        this(DatabaseConnection.getInstance()::getConnection);
    }

    public JdbcSupplierAnalyticsRepository(ConnectionProvider connections) {
        this.connections = connections;
    }

    @Override public List<SupplierLotSnapshot> findAllLots() throws SQLException {
        String sql = "SELECT l.id_lote, l.cantidad_inicial, e.nombre AS estado_lote, "
                + "p.id_proveedor, p.nombre AS nombre_proveedor "
                + "FROM lotes_inventario l "
                + "INNER JOIN proveedores p ON l.id_proveedor=p.id_proveedor "
                + "INNER JOIN estados_lote e ON l.id_estado=e.id_estado "
                + "ORDER BY l.id_lote";
        List<SupplierLotSnapshot> lots = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                lots.add(new SupplierLotSnapshot(result.getInt("id_lote"),
                        result.getBigDecimal("cantidad_inicial"), result.getString("estado_lote"),
                        result.getInt("id_proveedor"), result.getString("nombre_proveedor")));
            }
        }
        return lots;
    }
}
