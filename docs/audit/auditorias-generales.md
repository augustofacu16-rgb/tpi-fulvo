# Registro de Auditorías Generales y Transversales

Este archivo registra cambios transversales, refactorizaciones globales, estandarizaciones de endpoints y configuraciones arquitectónicas que aplican a múltiples componentes y no pertenecen al alcance aislado de una única rama `feature/` (`ft/`).

---

### [AUDIT-20261108-GEN-01] Estandarización de Endpoint de Diagnóstico y Salud: Migración de Actuator a `/health-check`

- **Fecha y Hora de Cierre:** 2026-11-08 22:15 (Hora Local -3)
- **Estado:** COMPLETADO
- **Alcance:** Transversal (API Gateway, Servicio Torneos, Servicio Operaciones y Documentación)

#### 1. ¿Qué y Cómo se hizo?
* **Componentes / Módulos intervenidos:**
  - `api-gateway`
  - `servicio-torneos`
  - `servicio-operaciones-partidos`
  - `postman`
  - `docs/agentes`
* **Archivos creados o modificados:**
  - `docs/audit/auditorias-generales.md`: Creación del archivo de registro de auditorías transversales fuera de ramas de features.
  - `api-gateway/src/main/resources/application.yml`: Configuración de Spring Boot Actuator con `base-path: /` y `path-mapping.health: health-check`.
  - `api-gateway/src/main/java/com/fulvo/gateway/config/SecurityConfig.java`: Ajuste de reglas de seguridad reactiva liberando `/health-check`, `/health-check/**` e `/info` vía `.permitAll()`.
  - `servicio-torneos/src/main/resources/application.yml`: Configuración análoga exponiendo el endpoint de salud en `/health-check`.
  - `servicio-operaciones-partidos/src/main/resources/application.yml`: Configuración análoga exponiendo `/health-check`.
  - `servicio-operaciones-partidos/README.md`: Actualización del comando de prueba curl hacia `/health-check`.
  - `postman/Fulvo-Auth-Quickstart.postman_collection.json`: Actualización de la petición de diagnóstico del Gateway a `GET {{gateway_url}}/health-check`.
  - `docs/agentes/GUIA_PRUEBAS_MANUALES.md`, `PLAN_DESARROLLO.md`, `TASKS.md`: Actualización de URLs de verificación perimetral hacia `/health-check`.
* **Endpoints y Contratos implementados:**
  - `GET /health-check`: Retorna `200 OK` con payload `{"status":"UP",...}` (público sin requerir token JWT).
* **Lógica técnica y de dominio aplicada:**
  - Se desacopla la convención interna del framework (`/actuator/...`) mediante las directivas `management.endpoints.web.base-path: /` y `management.endpoints.web.path-mapping.health: health-check`.
  - Se garantiza que balanceadores, probes de orquestación (Docker/Kubernetes) y clientes puedan consumir un endpoint semántico canónico `/health-check` sin exponer la estructura por defecto de Spring Actuator.

#### 2. ¿Qué hubo que arreglar sobre la marcha?
* **Problemas técnicos encontrados:**
  - Si únicamente se cambiaba `base-path: /health-check`, la ruta resultante requería `/health-check/health`.
* **Soluciones y Workarounds aplicados:**
  - Se combinó `base-path: /` con `path-mapping.health: health-check`, permitiendo que la llamada exacta `GET /health-check` responda directamente con el status `UP` del microservicio.
  - En `SecurityConfig.java` se aplicó `.pathMatchers("/health-check", "/health-check/**", "/info").permitAll()`.

#### 3. ¿En qué difiere con lo establecido en el Plan Original?
* **Modificaciones del Modelo de Datos / Entidades:** Ninguna.
* **Ajustes en Funcionalidades o Reglas:** Sustitución de la URI canónica de salud de `/actuator/health` a `/health-check`.
* **Motivo del cambio:** Estandarización y requerimiento explícito de diseño para utilizar un endpoint semántico `/health-check` uniforme en lugar del prefijo por defecto de Actuator.

#### 4. Puntos Clave para la Defensa Oral (Coloquio con Docentes)
* **Decisiones de diseño a defender:**
  - No fue necesario crear controladores manuales adicionales: se aprovechó el motor nativo de diagnóstico de Spring Boot Actuator remapeando su path mediante propiedades estándar (`path-mapping.health`).
  - La seguridad perimetral en Spring Cloud Gateway preserva el principio de mínimo privilegio, liberando exclusivamente `/health-check` de autenticación OIDC.
* **Preguntas probables de los profesores:**
  - *¿Por qué `/health-check` no exige token JWT?* Porque los monitores de infraestructura (healthchecks de Docker Compose, liveness probes) no poseen credenciales OIDC y requieren validar si el proceso está activo antes de enrutar tráfico.
