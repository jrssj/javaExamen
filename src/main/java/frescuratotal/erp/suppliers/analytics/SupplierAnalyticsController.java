package frescuratotal.erp.suppliers.analytics;

import java.sql.SQLException;

/** Coordinates the database read and the independent Stream calculations. */
public final class SupplierAnalyticsController {
    private final SupplierAnalyticsRepository repository;
    private final SupplierAnalyticsService service;

    public SupplierAnalyticsController(SupplierAnalyticsRepository repository, SupplierAnalyticsService service) {
        this.repository = repository;
        this.service = service;
    }

    public SupplierAnalyticsReport generateReport() throws SQLException {
        return service.analyze(repository.findAllLots());
    }
}
