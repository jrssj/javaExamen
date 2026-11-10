package frescuratotal.erp.suppliers.analytics;

import frescuratotal.erp.Main;
import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.support.SqlTestDatabase;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.*;

/** Runs the real main menu against empty and populated isolated MySQL tables. */
public class SupplierAnalyticsMainProbe {
    public static void main(String[] args) throws Exception {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            if (!connection.getCatalog().startsWith("erp_inventory_test_")) {
                throw new SQLException("Este recorrido requiere una base aislada de pruebas.");
            }
            try (ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                require(!tables.next(), "La base de este recorrido debe estar vacía.");
            }
            SqlTestDatabase.minimal(statement, Path.of(args[0]));
            String empty = runMenu();
            require(empty.contains("No hay datos de inventario suficientes para generar estadísticas."), "El menú no resolvió el inventario vacío.");
            require(empty.contains("¡Hasta pronto!"), "El menú no volvió a la salida.");
            System.out.println("PASS: menú principal con inventario vacío");

            statement.executeUpdate("INSERT INTO proveedores(id_proveedor,nombre,nit,email) VALUES(1,'Proveedor Alfa','AN-ALFA','alfa@example.invalid'),(2,'Proveedor Beta','AN-BETA','beta@example.invalid')");
            statement.executeUpdate("INSERT INTO productos(id_producto,nombre,id_categoria,unidad_medida) VALUES(1,'Producto de ensayo',1,'unidad')");
            statement.executeUpdate("INSERT INTO productos_proveedor(id_proveedor,id_producto) VALUES(1,1),(2,1)");
            statement.executeUpdate("INSERT INTO lotes_inventario(numero_lote,id_producto,id_proveedor,fecha_vencimiento,cantidad_inicial,cantidad,precio_compra,precio_venta,id_estado) VALUES"
                    + "('MAIN-AN-1',1,1,DATE_ADD(CURDATE(),INTERVAL 30 DAY),12.50,0,1,2,(SELECT id_estado FROM estados_lote WHERE nombre='Disponible')),"
                    + "('MAIN-AN-2',1,1,DATE_SUB(CURDATE(),INTERVAL 1 DAY),8.25,1,1,2,(SELECT id_estado FROM estados_lote WHERE nombre='Vencido')),"
                    + "('MAIN-AN-3',1,2,DATE_ADD(CURDATE(),INTERVAL 30 DAY),19.00,19,1,2,(SELECT id_estado FROM estados_lote WHERE nombre='Disponible'))");
            String populated = runMenu();
            require(populated.contains("Proveedor Alfa"), "No apareció el proveedor con mayor volumen.");
            require(populated.contains("Volumen total suministrado (cantidad inicial): 20.75"), "El volumen inicial acumulado es incorrecto.");
            require(populated.split("- Proveedor Alfa", -1).length - 1 == 1, "Los nombres vencidos se duplicaron.");
            require(!populated.contains("- Proveedor Beta"), "Se incluyó un proveedor sin lotes vencidos.");
            require(!populated.contains("No se pudo"), "Falló la conexión durante el recorrido del menú.");
            try (ResultSet result = statement.executeQuery("SELECT SUM(cantidad) FROM lotes_inventario")) {
                require(result.next() && result.getBigDecimal(1).intValueExact() == 20, "El reporte alteró las existencias.");
            }
            System.out.println("PASS: menú principal con resultados JDBC y Streams");
            System.out.println(populated);
            System.out.println("SUPPLIER_ANALYTICS_MENU=2 PASS, 0 FAIL");
        } finally { DatabaseConnection.getInstance().shutdown(); }
    }

    private static String runMenu() {
        InputStream previousInput = System.in;
        PrintStream previousOutput = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream("6\n0\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
            return bytes.toString(StandardCharsets.UTF_8);
        } finally { System.setIn(previousInput); System.setOut(previousOutput); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
