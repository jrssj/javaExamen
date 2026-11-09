package frescuratotal.erp.suppliers.analytics;

import java.math.BigDecimal;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplierAnalyticsServiceTest {
    private final SupplierAnalyticsService service = new SupplierAnalyticsService();

    private SupplierLotSnapshot lot(int lotId, int supplierId, String name, String quantity, String state) {
        return new SupplierLotSnapshot(lotId, new BigDecimal(quantity), state, supplierId, name);
    }

    @Test public void handlesEmptyInventoryWithoutInventingALeader() {
        SupplierAnalyticsReport report = service.analyze(List.of());
        assertTrue(report.isEmpty());
        assertTrue(report.getLeadingSupplier().isEmpty());
        assertTrue(report.getExpiredSupplierNames().isEmpty());
    }

    @Test public void accumulatesInitialVolumeAcrossAllLotsAndStates() {
        SupplierAnalyticsReport report = service.analyze(List.of(
                lot(1, 1, "Lácteos del Campo", "12.50", "Disponible"),
                lot(2, 1, "Lácteos del Campo", "8.25", "Vencido"),
                lot(3, 1, "Lácteos del Campo", "6.00", "En Cuarentena"),
                lot(4, 2, "Frutas del Valle", "25.00", "Disponible")));
        SupplierAnalyticsReport.LeadingSupplier leader = report.getLeadingSupplier().orElseThrow();
        assertEquals(1, leader.getId());
        assertEquals("Lácteos del Campo", leader.getName());
        assertEquals(0, new BigDecimal("26.75").compareTo(leader.getSuppliedVolume()));
    }

    @Test public void preservesDecimalPrecisionWhenAddingVolumes() {
        SupplierAnalyticsReport report = service.analyze(List.of(
                lot(1, 1, "Proveedor A", "0.10", "Disponible"),
                lot(2, 1, "Proveedor A", "0.20", "Disponible"),
                lot(3, 2, "Proveedor B", "0.29", "Disponible")));
        SupplierAnalyticsReport.LeadingSupplier leader = report.getLeadingSupplier().orElseThrow();
        assertEquals(1, leader.getId());
        assertEquals(0, new BigDecimal("0.30").compareTo(leader.getSuppliedVolume()));
    }

    @Test public void groupsVolumeBySupplierIdEvenWhenNamesAreEqual() {
        SupplierAnalyticsReport report = service.analyze(List.of(
                lot(1, 1, "Proveedor Central", "2.00", "Disponible"),
                lot(2, 2, "Proveedor Central", "4.00", "Disponible"),
                lot(3, 3, "Proveedor Independiente", "5.00", "Disponible")));
        assertEquals(3, report.getLeadingSupplier().orElseThrow().getId());
    }

    @Test public void filtersExpiredStatusAndRemovesRepeatedNames() {
        SupplierAnalyticsReport report = service.analyze(List.of(
                lot(1, 1, "Frutas del Valle", "1", "Vencido"),
                lot(2, 1, "Frutas del Valle", "2", "Vencido"),
                lot(3, 2, "Carnes La Excelencia", "3", "Vencido"),
                lot(4, 3, "Solo cuarentena", "4", "En Cuarentena"),
                lot(5, 4, "Solo disponible", "5", "Disponible"),
                lot(6, 5, "Solo próximo", "6", "Proximo a Vencer"),
                lot(7, 6, "Frutas del Valle", "7", "Vencido")));
        assertEquals(List.of("Carnes La Excelencia", "Frutas del Valle"), report.getExpiredSupplierNames());
    }

    @Test public void acceptsEitherSupplierWhenMaximumVolumesTie() {
        SupplierAnalyticsReport report = service.analyze(List.of(
                lot(1, 1, "Proveedor A", "10.00", "Disponible"),
                lot(2, 2, "Proveedor B", "10.00", "Disponible")));
        SupplierAnalyticsReport.LeadingSupplier leader = report.getLeadingSupplier().orElseThrow();
        assertTrue(leader.getId() == 1 || leader.getId() == 2);
        assertEquals(0, new BigDecimal("10").compareTo(leader.getSuppliedVolume()));
        assertTrue(report.getExpiredSupplierNames().isEmpty());
    }

    @Test public void requiresTheExactStoredExpiredStatus() {
        SupplierAnalyticsReport report = service.analyze(List.of(
                lot(1, 1, "Vencido exacto", "1", "Vencido"),
                lot(2, 2, "Estado distinto", "2", "vencido"),
                lot(3, 3, "Estado nulo", "3", null)));
        assertEquals(List.of("Vencido exacto"), report.getExpiredSupplierNames());
        assertEquals(3, report.getLeadingSupplier().orElseThrow().getId());
    }

    @Test public void keepsAZeroVolumeSupplierAndDoesNotMutateTheInput() {
        List<SupplierLotSnapshot> input = List.of(lot(1, 1, "Proveedor sin volumen", "0.00", "Disponible"));
        SupplierAnalyticsReport report = service.analyze(input);
        assertFalse(report.isEmpty());
        assertEquals("Proveedor sin volumen", report.getLeadingSupplier().orElseThrow().getName());
        assertEquals(0, report.getLeadingSupplier().orElseThrow().getSuppliedVolume().signum());
        assertEquals(1, input.size());
        assertEquals("Disponible", input.get(0).getStatusName());
    }
}
