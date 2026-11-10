package frescuratotal.erp.suppliers.analytics;

import java.io.PrintStream;
import java.sql.SQLException;

/** Spanish console presentation of the supplier report. */
public final class SupplierAnalyticsView {
    private final SupplierAnalyticsController controller;
    private final PrintStream output;

    public SupplierAnalyticsView(SupplierAnalyticsController controller, PrintStream output) {
        this.controller = controller;
        this.output = output;
    }

    public void showReport() {
        try {
            SupplierAnalyticsReport report = controller.generateReport();
            output.println("\n*** Reporte de Rendimiento de Proveedores ***");
            if (report.isEmpty()) {
                output.println("No hay datos de inventario suficientes para generar estadísticas.");
                return;
            }
            SupplierAnalyticsReport.LeadingSupplier leader = report.getLeadingSupplier().orElseThrow();
            output.println("\n>> Proveedor con Mayor Volumen de Suministro:");
            output.println(leader.getName());
            output.println("Volumen total suministrado (cantidad inicial): " + leader.getSuppliedVolume().toPlainString());
            output.println("\n>> Proveedores con Lotes Vencidos Registrados:");
            if (report.getExpiredSupplierNames().isEmpty()) {
                output.println("No hay proveedores con lotes vencidos registrados.");
            } else {
                report.getExpiredSupplierNames().forEach(name -> output.println("- " + name));
            }
        } catch (SQLException error) {
            output.println("No se pudo generar el reporte de proveedores. Verifique la conexión con MySQL.");
        }
    }
}
