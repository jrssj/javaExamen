# Verificación del examen: rendimiento de proveedores

Autor del examen: Junior Quauro (`jrssj`). Fecha de revisión: 9 de noviembre de 2026.

## Comprobación del enunciado

| Requisito | Implementación y comprobación |
|---|---|
| Opción visible en el menú principal | Main presenta la opción 6 y llama a SupplierAnalyticsView; recorrido real de consola con entrada 6 y 0. |
| SQL que relacione las tres tablas | JdbcSupplierAnalyticsRepository hace INNER JOIN de lotes_inventario, proveedores y estados_lote. |
| Cinco datos de la consulta de referencia | SupplierLotSnapshot recibe ID del lote, cantidad inicial, estado, ID y nombre del proveedor. |
| Procesamiento de objetos con Stream API | SupplierAnalyticsService calcula ambos indicadores sobre la List devuelta por JDBC. |
| Mayor volumen acumulado | groupingBy por ID, map de las cantidades iniciales, reduce con BigDecimal y max por volumen; incluye todos los estados. |
| Empates | Se admite cualquier proveedor con volumen máximo; hay una prueba específica. |
| Lotes vencidos | filter compara exactamente el estado Vencido, map obtiene nombres y distinct elimina repeticiones. |
| Inventario vacío | La vista muestra el mensaje exigido; se comprueba sin MySQL y desde Main con MySQL real. |
| Presentación clara | Cabecera del reporte, ganador, volumen y lista de nombres con guiones; contempla ausencia de vencidos. |
| Clase nueva y paquete apropiado | Servicio separado dentro de frescuratotal.erp.suppliers.analytics, con repositorio, controlador y vista. |
| Java 17, Maven, JDBC y MVC | Sin frameworks nuevos ni record; separación de lectura, cálculo y presentación. |
| Dos commits atómicos | Primer avance: servicio, consulta y pruebas; segundo: integración en Main, vista y documentación. |
| Rama y PR | feat/supplier-analytics-report; PR hacia main que debe permanecer abierto. |
| README | Subsección Módulo de Analíticas con finalidad, resultados, utilidad para compras y ejecución. |
| Entrega | URL privada y hash completo del último commit; acceso del trainer debe verificarse aparte. |

## Resultados de ejecución

`mvn -B clean verify`: **54 pruebas aprobadas**, 0 fallos, 0 errores y 0 omitidas. Además, **2 recorridos reales de Main aprobados**, con inventario vacío y poblado. Ambas ejecuciones finalizaron con BUILD SUCCESS. La segunda revisión completa terminó el 9 de noviembre a las 16:14 (Bogotá). También se ejecutaron 14 comprobaciones del flujo integrado de inventario, ventas, proveedores y logística: 14 aprobadas y 0 fallidas.

Las pruebas utilizan una instancia local aislada de MySQL 8.4.13 y bases nuevas con el prefijo erp_inventory_test_. No se ejecutan contra bases comerciales. La configuración de Java utilizada es Temurin 17.0.21+9, Maven 3.9.16 y Connector/J 8.4.0.

La suite comprueba acumulación de lotes, decimales, proveedores distintos con nombres iguales, empates, filtro exacto de estado, eliminación de duplicados, volumen cero, inventario vacío y fallos JDBC. La integración valida el esquema real, cantidad_inicial aunque cantidad sea cero y estado almacenado aunque difiera de la fecha. Los recorridos del menú comprueban inventario vacío y poblado, el volumen mostrado y la conservación de existencias.

Para ejecutar los recorridos adicionales con una base nueva y aislada, después de mvn test-compile y con las variables ERP_DB_* y ERP_TEST_SCHEMA_FILE configuradas:

```powershell
mvn exec:java "-Dexec.mainClass=frescuratotal.erp.suppliers.analytics.SupplierAnalyticsMainProbe" "-Dexec.classpathScope=test" "-Dexec.args=docs/database.sql"
```

El runner requiere una base vacía con prefijo erp_inventory_test_ y conserva los datos para inspección.

## Rúbrica y límites

- Correctitud funcional (10 puntos): respaldada por las pruebas descritas, incluyendo ejecución con MySQL y consola real.
- Uso de Stream API (10 puntos): groupingBy, map, reduce y max para el volumen; filter, map y distinct para vencidos. La consulta no calcula SUM, GROUP BY ni DISTINCT.
- Dominio y conocimiento (13 puntos): requiere explicación y defensa personal del estudiante. La guía de sustentación incluye el recorrido, las lambdas, referencias a métodos, operaciones terminales y un ejemplo calculado a mano; el código por sí solo no acredita estos puntos.

Repositorio privado: `jrssj/javaExamen`. Trainer: `CRISTIAN7712`. Invitación de colaboración enviada y pendiente de aceptación; el acceso quedará activo cuando la acepte. El PR hacia main permanece abierto para revisión. La actualización del nombre solo modifica documentación; el código es idéntico al código comprobado con las 54 pruebas y los dos recorridos de menú.
