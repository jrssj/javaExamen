# Verificación del registro de lotes

El registro valida identificadores, vencimiento, cantidades, precios y ubicación.
El DAO comprueba la relación entre proveedor y producto, inserta el lote y utiliza
el ID generado para asignar su código definitivo. La operación se confirma con
commit y se revierte con rollback si falla cualquiera de sus pasos.

Las pruebas de inventario verifican el esquema oficial, DECIMAL(10,2), códigos
únicos, asociación proveedor-producto y rollback ante errores inducidos. Las
pruebas JDBC requieren una base aislada cuyo nombre comience por
erp_inventory_test_. La configuración y los comandos están en el README.

La consulta de existencias conserva los bloqueos de cuarentena. La trazabilidad
relaciona proveedor, producto y clientes con ventas entregadas.
