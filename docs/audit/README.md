# Fragmentos de Auditoría de Desarrollo

Este directorio almacena las entradas de auditoría de cada feature/tarea en archivos individuales para **eliminar el 100% de los conflictos de Git**.

## Convención de Nombres de Archivo
Cada archivo debe nombrarse según la etapa y subsección:
`<etapa>.<subseccion>-<descripcion-corta>.md`

Ejemplos:
* `1.1-servicios-soporte-docker.md`
* `1.2-libreria-compartida-common.md`
* `1.3-scaffolding-servicio-torneos.md`
* `1.4-inicializacion-operaciones.md`

## Regla de Oro para Desarrolladores y Agentes de IA
1. **NUNCA modificar archivos de auditoría de otras tareas** ni editar directamente otros fragmentos existentes.
2. Al finalizar una subsección, **crear un nuevo archivo `.md` en esta carpeta (`docs/audit/`)** copiando la estructura de `_TEMPLATE.md`.
