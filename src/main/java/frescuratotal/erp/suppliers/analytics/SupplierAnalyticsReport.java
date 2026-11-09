package frescuratotal.erp.suppliers.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Results of the two supplier statistics, without console formatting. */
public final class SupplierAnalyticsReport {
    private final Optional<LeadingSupplier> leadingSupplier;
    private final List<String> expiredSupplierNames;

    public SupplierAnalyticsReport(Optional<LeadingSupplier> leadingSupplier,
                                   List<String> expiredSupplierNames) {
        this.leadingSupplier = leadingSupplier;
        this.expiredSupplierNames = List.copyOf(expiredSupplierNames);
    }

    public boolean isEmpty() { return leadingSupplier.isEmpty(); }
    public Optional<LeadingSupplier> getLeadingSupplier() { return leadingSupplier; }
    public List<String> getExpiredSupplierNames() { return expiredSupplierNames; }

    public static final class LeadingSupplier {
        private final int id;
        private final String name;
        private final BigDecimal suppliedVolume;

        public LeadingSupplier(int id, String name, BigDecimal suppliedVolume) {
            this.id = id;
            this.name = name;
            this.suppliedVolume = suppliedVolume;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getSuppliedVolume() { return suppliedVolume; }
    }
}
