package frescuratotal.erp.suppliers.analytics;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Calculates supplier statistics from JDBC snapshots using the Stream API. */
public final class SupplierAnalyticsService {
    public SupplierAnalyticsReport analyze(List<SupplierLotSnapshot> lots) {
        // Supplier IDs distinguish businesses that happen to share a name.
        Map<Integer, List<SupplierLotSnapshot>> lotsBySupplier = lots.stream()
                .collect(Collectors.groupingBy(SupplierLotSnapshot::getSupplierId));

        Optional<SupplierAnalyticsReport.LeadingSupplier> leader = lotsBySupplier.values().stream()
                .map(this::summarizeSupplier)
                .max(Comparator.comparing(SupplierAnalyticsReport.LeadingSupplier::getSuppliedVolume));

        List<String> expiredNames = lots.stream()
                .filter(lot -> "Vencido".equals(lot.getStatusName()))
                .map(SupplierLotSnapshot::getSupplierName)
                .distinct()
                .sorted()
                .toList();

        return new SupplierAnalyticsReport(leader, expiredNames);
    }

    private SupplierAnalyticsReport.LeadingSupplier summarizeSupplier(List<SupplierLotSnapshot> supplierLots) {
        // Every group is nonempty because it was created by groupingBy.
        SupplierLotSnapshot supplier = supplierLots.get(0);
        BigDecimal total = supplierLots.stream()
                .map(SupplierLotSnapshot::getInitialQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SupplierAnalyticsReport.LeadingSupplier(
                supplier.getSupplierId(), supplier.getSupplierName(), total);
    }
}
