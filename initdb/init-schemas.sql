-- ============================================================================
-- Inicialización de Esquemas Lógicos Desacoplados - TPI Fulvo'
-- Asignatura: Backend de Aplicaciones (UTN-FRC)
-- ============================================================================

-- Esquema lógico para el microservicio de Torneos (administrado por servicio-torneos)
CREATE SCHEMA IF NOT EXISTS esquema_torneos;

-- Esquema lógico para el microservicio de Operaciones y Partidos (administrado por servicio-operaciones-partidos)
CREATE SCHEMA IF NOT EXISTS esquema_operaciones;
