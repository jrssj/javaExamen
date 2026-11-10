package frescuratotal.erp.suppliers.analytics;

import java.sql.SQLException;
import java.util.List;

public interface SupplierAnalyticsRepository {
    List<SupplierLotSnapshot> findAllLots() throws SQLException;
}
