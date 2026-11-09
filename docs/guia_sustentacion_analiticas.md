# Guía de sustentación: analíticas de proveedores

## Explicación del recorrido

La opción 6 de Main llama a SupplierAnalyticsView.showReport. La vista solicita al controlador el reporte. El controlador pide al repositorio todos los lotes, llama al servicio y devuelve el resultado. La vista imprime el resultado o un mensaje de conexión si JDBC lanza SQLException.

El repositorio utiliza try-with-resources para cerrar Connection, PreparedStatement y ResultSet incluso si hay una excepción. El while de lectura únicamente transforma filas en objetos; los cálculos estadísticos sí usan Streams, como exige la actividad. Se consultan los cinco campos del SQL de referencia y se enlazan las tres tablas con INNER JOIN.

## Primera estadística, paso a paso

1. lots.stream() crea el flujo de lotes.
2. collect(groupingBy(SupplierLotSnapshot::getSupplierId)) crea un Map cuyo ID apunta a una lista de lotes de ese proveedor.
3. values().stream() recorre los grupos. map(this::summarizeSupplier) convierte cada grupo en un resumen.
4. Dentro del resumen, map(SupplierLotSnapshot::getInitialQuantity) obtiene cantidades y reduce(BigDecimal.ZERO, BigDecimal::add) las suma.
5. max(Comparator.comparing(...getSuppliedVolume)) devuelve el resumen de mayor volumen dentro de un Optional.

El ID evita juntar dos proveedores distintos con el mismo nombre. BigDecimal conserva los decimales exactos del DECIMAL de MySQL. Una lista sin lotes produce un Map vacío y max devuelve Optional.empty(), de modo que no hay que inventar un ganador ni acceder al primer elemento de una lista vacía. Cada grupo creado por groupingBy sí tiene por lo menos un lote.

## Segunda estadística, paso a paso

filter(lot -> "Vencido".equals(lot.getStatusName())) conserva únicamente los lotes con ese estado. map obtiene sus nombres. distinct elimina nombres repetidos. sorted los ordena. toList materializa el resultado.

La lambda es una función anónima que recibe un lote y devuelve un boolean. El operador :: es una referencia a método: SupplierLotSnapshot::getSupplierName equivale a lot -> lot.getSupplierName(). BigDecimal::add equivale a (total, quantity) -> total.add(quantity). map transforma; filter selecciona; reduce acumula; collect construye una colección o mapa. Los streams no ejecutan estas operaciones hasta llegar a la operación terminal, como collect, max, reduce o toList.

## Ejemplo para explicar sin mirar el código

| Proveedor | Cantidad inicial | Cantidad actual | Estado |
|---|---:|---:|---|
| Alfa, ID 1 | 12.50 | 0 | Disponible |
| Alfa, ID 1 | 8.25 | 1 | Vencido |
| Beta, ID 2 | 19.00 | 19 | Disponible |
| Gamma, ID 3 | 2.00 | 0 | Vencido |
| Gamma, ID 3 | 3.00 | 0 | Vencido |

El ganador es Alfa con 20.75; Beta tiene 19.00 y Gamma 5.00. La lista de vencidos contiene Alfa y Gamma una sola vez cada uno. El inventario actual y la fecha no cambian estos cálculos: la actividad pide cantidad inicial y estado registrado. El ERP tiene además una revisión diaria de calidad antes del menú, que puede actualizar estados al iniciar.

## Preguntas que debes poder responder

- ¿Por qué no SUM y GROUP BY en SQL? Porque la actividad evalúa los cálculos con Stream API; el SQL trae filas para procesar en Java.
- ¿Por qué dos proveedores con el mismo nombre se separan en el volumen? Porque groupingBy usa su ID. En la segunda estadística distinct opera sobre nombres porque así lo pide el enunciado.
- ¿Qué pasa si no hay vencidos? Sigue apareciendo el líder y se informa que no hay proveedores con lotes vencidos.
- ¿Qué pasa si MySQL está caído? Se captura SQLException y se muestra un error de conexión. No se confunde con inventario vacío.
- ¿Qué pasa en un empate? max puede devolver cualquiera; ambos son válidos según la actividad.
- ¿Por qué no parallelStream? El reporte no necesita concurrencia y un stream secuencial mantiene la explicación y depuración sencillas.
- ¿Se altera la colección o la base? El servicio solo transforma datos, el DTO tiene campos finales y el repositorio ejecuta SELECT.

## Preparar una demostración

Clone el repositorio privado con su cuenta autorizada y cambie a feat/supplier-analytics-report: la funcionalidad está en esa rama mientras el PR permanece abierto. Configure Java 17 y MySQL, cree una base nueva e importe docs/database.sql una sola vez. Defina ERP_DB_USER, ERP_DB_PASSWORD y ERP_DB_URL antes de abrir NetBeans. Ejecute mvn compile exec:java, seleccione 6 y explique los dos resultados. Muestre también el servicio, el SQL, las pruebas y los dos commits del PR.

Practique el ejemplo anterior y luego cambie una cantidad a mano para predecir el ganador. La corrección del código no sustituye el dominio personal que evalúa la rúbrica.
