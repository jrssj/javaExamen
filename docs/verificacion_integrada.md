# Verificación y pruebas del ERP

El proyecto utiliza Java 17, Maven y MySQL. Las pruebas de validación y consola
se ejecutan con `mvn clean verify`. Las pruebas JDBC requieren definir
ERP_TEST_SCHEMA_FILE y una conexión a una base vacía y nueva con prefijo
erp_inventory_test_. Se conservan los esquemas de ensayo para inspección.

La suite cubre validaciones, vencimientos, cuarentenas, inspecciones, mermas,
trazabilidad, generación de facturas y coherencia del SQL consolidado. También
comprueba rollback, idempotencia y operaciones concurrentes.

El esquema contiene 23 tablas y 24 relaciones. Para una instalación nueva se
utiliza docs/database.sql. Si ya hay datos, se utilizan migraciones compatibles
en lugar de repetir la carga completa.

El reporte de proveedores y sus pruebas específicas se describen en la sección
Módulo de Analíticas del README cuando se incorpora ese avance.
