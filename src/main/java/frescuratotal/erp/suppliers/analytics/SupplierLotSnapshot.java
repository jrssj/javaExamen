package frescuratotal.erp.suppliers.analytics;

import java.math.BigDecimal;

/** Inventory data required by the supplier report, before Stream processing. */
public final class SupplierLotSnapshot {
    private final int lotId;
    private final BigDecimal initialQuantity;
    private final String statusName;
    private final int supplierId;
    private final String supplierName;

    public SupplierLotSnapshot(int lotId, BigDecimal initialQuantity, String statusName,
                               int supplierId, String supplierName) {
        this.lotId = lotId;
        this.initialQuantity = initialQuantity;
        this.statusName = statusName;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
    }

    public int getLotId() { return lotId; }
    public BigDecimal getInitialQuantity() { return initialQuantity; }
    public String getStatusName() { return statusName; }
    public int getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
}
