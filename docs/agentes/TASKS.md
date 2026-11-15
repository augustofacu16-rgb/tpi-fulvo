# Tablero de Control y Trazabilidad de Tareas - TPI Fulvo'

> **Estado:** `ETAPA 3 COMPLETADA - GATE 2B SUPERADO - EN ARRANQUE ETAPA 4`  
> **Regla de Operación Obligatoria:**
> 1. Antes de codear cualquier tarea, leer `CONTEXT.md`, `PLAN_DESARROLLO.md`, `DISENO_SISTEMA_FULVO.md` y las últimas entradas de `docs/audit/` .
> 2. Al finalizar cada subsección, validar los endpoints y flujos según `GUIA_PRUEBAS_MANUALES.md`, marcar las casillas completadas `[X]` en este archivo y **registrar la auditoría correspondiente en `docs/audit/`** detallando qué se hizo, qué se arregló sobre la marcha y si hubo diferencias con el plan original.
> 3. Al concluir cada etapa formal, verificar la superación de su compuerta de calidad (Gate).
> 4. **Directiva de Participación Balanceada:** En cada una de las 5 etapas, todos los integrantes (**Tomas, Gastón, Santiago y Luis Miguel**) son responsables de al menos una subsección.
> 5. **Protocolo Anti-Suposición:** Ante cualquier duda de dominio, ambigüedad o dilema técnico, el agente debe pausar y consultar al desarrollador con opciones justificadas y recomendación técnica antes de implementar.
> 6. **Aislamiento en Git y Flujo de PR por el Desarrollador (Obligatorio):** Está prohibido desarrollar directamente en `main`. El agente **DEBE crear y cambiarse a una rama propia** desde `main` (`feature/<etapa>-<subseccion>-<descripcion-corta>`) antes de empezar a codear cualquier tarea. Los agentes tienen **estrictamente prohibido pushear a GitHub (`git push`)**. Al finalizar y validar la tarea, el desarrollador humano es quien pushea la rama a mano (`git push -u origin feature/...`), crea la correspondiente **Pull Request (PR)** en GitHub para incorporar los cambios a `main`, y tras la aprobación y merge, elimina la rama.

---

## Estado Global del Proyecto (Sprint 3 Días)

| Sprint | Etapa | Descripción | Responsables por Etapa | Estado | Gate Superado | Auditoría |
|---|---|---|---|---|:---:|:---:|
| **Día 0** | **Etapa 0** | Planificación, Convenciones y Roles | Todo el Equipo | `[COMPLETADO]` | `[X]` | `[X]` (AUDIT-00) |
| **Día 1** | **Etapa 1** | Infraestructura Base, Common Lib, Scaffolding y Gateway | Tomas, Luis Miguel, Gastón, Santiago | `[COMPLETADO]` | `[X]` | `[X]` (AUDIT-20261109-GATE1) |
| **Día 2** | **Etapa 2** | Core de Torneos, Admisiones, Fixtures y Swagger Base | Tomas, Gastón, Santiago, Luis Miguel | `[COMPLETADO]` | `[X]` | `[X]` (AUDIT-20261111-GATE2A) |
| **Día 2** | **Etapa 3** | Core de Operaciones, Arbitraje, Posiciones y Planteles | Santiago, Gastón, Tomas, Luis Miguel | `[COMPLETADO]` | `[X]` | `[X]` (AUDIT-20261114-GATE2B) |
| **Día 3** | **Etapa 4** | Adaptadores Externos, Resiliencia y Scheduler | Luis Miguel, Santiago, Gastón, Tomas | `[PENDIENTE]` | `[ ]` | `[ ]` |
| **Día 3** | **Etapa 5** | Despliegue Docker, Auditoría OpenAPI y Postman E2E | Tomas, Gastón, Santiago, Luis Miguel | `[PENDIENTE]` | `[ ]` | `[ ]` |

---

## DÍA 1 / SPRINT 1: Infraestructura Base, Dominio Común, Scaffolding y Gateway

### ETAPA 1: Infraestructura Base, Common Lib, Scaffolding y Gateway Reactivo
- **Objetivo:** Infraestructura contenerizada (Postgres, Keycloak, Mailpit, WireMock), artefacto común en `.m2`, esqueletos de servicios y punto de acceso central seguro.
- **Responsables:** Tomas Augusto Quiróz, Luis Miguel Urviola Dueñas, Gastón Emiliano Cambos Berrondo, Santiago Mariano Gonzalez.

#### Subsección 1.1: Servicios de Soporte Contenerizados (Postgres, Keycloak, Mailpit y WireMock)
- [X] **1.1.1** Configurar `docker-compose.yml` en la raíz con puerto dinámico `${DB_PORT_HOST:-5432}:5432`, archivo `.env.example`, PostgreSQL 16 (`fulvo_db`), Keycloak 26.5, Mailpit y WireMock. `[Tomas]`
- [X] **1.1.2** Crear script SQL `initdb/init-schemas.sql` con creación de `esquema_torneos` y `esquema_operaciones`. `[Tomas]`
- [X] **1.1.3** Crear archivo de importación de Keycloak `keycloak/realm-export.json` con Realm `fulvo-realm`, cliente `fulvo-backend` y usuarios (`admin1`, `arbitro1`, `delegado1`, `fanatico1`). `[Tomas]`
- [X] **1.1.4** Configurar WireMock (:8086) y Mailpit (:1025/:8025) en el Compose. `[Luis Miguel]`
- [X] **1.1.5** Verificar despliegue con `docker compose up -d` y obtención de tokens JWT vía Keycloak. `[Tomas & Luis Miguel]`
- *Criterio de Aceptación 1.1:* Postgres crea ambos esquemas limpios; Keycloak emite JWT válidos; Mailpit expone Web UI en puerto 8025; WireMock responde mocks.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261108-11]`

#### Subsección 1.2: Librería Compartida `fulvo-common-lib` (Instalación en `.m2`)
- [X] **1.2.1** Inicializar proyecto Maven `fulvo-common-lib` (`com.fulvo:fulvo-common-lib:1.0.0-SNAPSHOT`, empaquetado `.jar`). `[Luis Miguel]`
- [X] **1.2.2** Implementar DTOs inter-servicio en `com.fulvo.common.dto`: `CrucePartidoDTO`, `NotificacionEmailDTO`, `GeocodificacionDTO`, `ClimaPronosticoDTO`. `[Luis Miguel]`
- [X] **1.2.3** Definir jerarquía de excepciones en `com.fulvo.common.exception`: `BusinessRuleException`, `RecursoNoEncontradoException`, `ConflictoEstadoException`. `[Luis Miguel]`
- [X] **1.2.4** Implementar `@RestControllerAdvice` global retornando **RFC 7807 (`ProblemDetail`)** de Spring Boot 4 (Apuntes 18 y 23). `[Luis Miguel]`
- [X] **1.2.5** Implementar `CorrelationIdFilter` (`OncePerRequestFilter`) propagando cabecera `X-Correlation-ID` al MDC de SLF4J (Apunte 23). `[Luis Miguel]`
- [X] **1.2.6** Compilar e instalar librería en repositorio local: `mvn clean install -DskipTests`. `[Luis Miguel]`
- *Criterio de Aceptación 1.2:* Artefacto disponible en `~/.m2` local listo para ser importado por los microservicios.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261108-12]`

#### Subsección 1.3: Scaffolding Base y Configuración DataSource de `servicio-torneos`
- [X] **1.3.1** Inicializar microservicio `servicio-torneos` (`com.fulvo:servicio-torneos`, Java 21, Spring Boot 4.0.8). `[Gastón]`
- [X] **1.3.2** Configurar `application.yml` conectando a PostgreSQL y definiendo `default_schema: esquema_torneos`. `[Gastón]`
- [X] **1.3.3** Importar dependencia `fulvo-common-lib` desde `.m2` local y estructurar paquetes base `com.fulvo.torneos.*`. `[Gastón]`
- [X] **1.3.4** Verificar compilación limpia y arranque inicial en puerto `8081`. `[Gastón]`
- *Criterio de Aceptación 1.3:* `mvn clean package -DskipTests` exitoso y conexión validada con el esquema lógico de torneos.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261108-13]`

#### Subsección 1.4: Scaffolding Base y Configuración DataSource de `servicio-operaciones-partidos`
- [X] **1.4.1** Inicializar microservicio `servicio-operaciones-partidos` (`com.fulvo:servicio-operaciones-partidos`, Java 21, Spring Boot 4.0.8). `[Santiago]`
- [X] **1.4.2** Configurar `application.yml` conectando a PostgreSQL y definiendo `default_schema: esquema_operaciones`. `[Santiago]`
- [X] **1.4.3** Importar dependencia `fulvo-common-lib` desde `.m2` local y estructurar paquetes base `com.fulvo.operaciones.*`. `[Santiago]`
- [X] **1.4.4** Verificar compilación limpia y arranque inicial en puerto `8082`. `[Santiago]`
- *Criterio de Aceptación 1.4:* `mvn clean package -DskipTests` exitoso y conexión validada con el esquema lógico de operaciones.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261108-14]`

#### Subsección 1.5: API Gateway Reactivo y Filtro de Seguridad OIDC con Keycloak
- [X] **1.5.1** Configurar `api-gateway` (`com.fulvo:api-gateway`, Spring Cloud Gateway 2025.1.4 sobre Spring Boot 4.0.8) en puerto `8080`. `[Tomas]`
- [X] **1.5.2** Configurar Spring Security reactivo con `oauth2ResourceServer(jwt)` validando tokens de Keycloak en `http://localhost:8090/realms/fulvo-realm`. `[Tomas]`
- [X] **1.5.3** Implementar `KeycloakReactiveRoleConverter` para mapear `resource_access.fulvo-backend.roles` a autoridades `ROL_*`. `[Tomas]`
- [X] **1.5.4** Implementar `CorrelationIdGlobalFilter` en el Gateway propagando `X-Correlation-ID`. `[Tomas]`
- [X] **1.5.5** Configurar `GatewayAuthenticationEntryPoint` y `GatewayAccessDeniedHandler` devolviendo RFC 7807 (`ProblemDetail`). `[Tomas]`
- *Criterio de Aceptación 1.5:* Peticiones anónimas a rutas protegidas devuelven `401 Unauthorized` bajo RFC 7807; peticiones con JWT válido y rol correcto son ruteadas.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261109-15]`

> 🔒 **COMPUERTA DE CALIDAD (GATE 1) [SUPERADA Y CERTIFICADA]:** Infraestructura operativa (`docker compose up -d`), librería compartida instalada en `.m2`, microservicios respondiendo health-checks y Gateway validando JWTs. Certificado en `docs/audit/1.0-cierre-etapa-1.md`.

---

## DÍA 2 / SPRINT 2: Core de Negocio, Admisiones, Fixtures, Partidos y Tablas

### ETAPA 2: Core de Torneos, Admisiones, Fixtures y Seguridad Perimetral
- **Objetivo:** Ciclo completo de inscripción, aprobación de delegados, generación determinística de fixtures y agregación OpenAPI unificada en Gateway.
- **Responsables:** Tomas Augusto Quiróz, Gastón Emiliano Cambos Berrondo, Santiago Mariano Gonzalez, Luis Miguel Urviola Dueñas.

#### Subsección 2.1: Modelado JPA, Ciclo Registral y Admisión de Torneos (`servicio-torneos`)
- [X] **2.1.1** Entidades JPA en `esquema_torneos`: `Torneo`, `SolicitudInscripcion`, `Equipo`, `Jugador`. `[Gastón]`
- [X] **2.1.2** Repositorios JPA con bloqueos pesimistas para control de concurrencia en inscripciones. `[Gastón]`
- [X] **2.1.3** `POST /v1/torneos` validando cupos estrictos (10/20 Liga, 8/16 Llaves, RN-01). `[Gastón]`
- [X] **2.1.4** `POST /v1/torneos/{id}/solicitudes-inscripcion` (15-22 jugadores, arquero, capitán, DNI único, RN-02, RN-03). `[Gastón]`
- [X] **2.1.5** `PATCH /v1/solicitudes-inscripcion/{id}/admision` (promoción a delegado, alta de Equipo y cupo, RN-04). `[Gastón]`
- *Criterio de Aceptación 2.1:* Torneos con cupo inválido responden `400 Bad Request`; planteles sin arquero o capitán responden `400`; doble inscripción responde `409 Conflict`.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261110-21]`

#### Subsección 2.2: Generación Algorítmica Determinística de Fixtures y Despacho Inter-Servicio
- [X] **2.2.1** `PATCH /v1/torneos/{id}/estado`: Cierre de inscripción al 100% (RN-01, rechazo `409 Conflict` si incompleto) y cancelación flexible (RN-04). `[Gastón]`
- [X] **2.2.2** Algoritmo Round Robin determinístico (Liga) y algoritmo de Llaves determinísticas (Copa) ordenados por ID de equipo. `[Gastón]`
- [X] **2.2.3** `POST /v1/torneos/{id}/fixtures`: Genera cruces, pasa torneo a `INICIADO` y despacha lote vía `RestClient` a `servicio-operaciones-partidos` (`POST /v1/partidos/lote`). `[Gastón]`
- *Criterio de Aceptación 2.2:* Intento de cerrar torneo sin cupo lleno responde `409 Conflict`; fixture de Liga con 10 equipos genera exactamente 9 fechas y 45 partidos; fixture de Llaves con 8 equipos genera 4 partidos de cuartos.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261111-22]`

#### Subsección 2.3: Recepción y Persistencia en Lote de Fixtures (`servicio-operaciones-partidos`)
- [X] **2.3.1** Entidad JPA `Partido` en `esquema_operaciones` con claves foráneas lógicas (`torneo_id`, `equipo_local_id`, `equipo_visitante_id`). `[Santiago]`
- [X] **2.3.2** `POST /v1/partidos/lote`: Recepción transaccional del lote de cruces desde `servicio-torneos`, persistiendo en estado `PROGRAMADO`. `[Santiago]`
- *Criterio de Aceptación 2.3:* Los partidos quedan persistidos en `esquema_operaciones.partidos` con estado inicial `PROGRAMADO`.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261111-23]`

#### Subsección 2.4: Integración con Adaptador de Notificaciones para Postulación y Admisión
- [X] **2.4.1** Cliente `RestClient` en `servicio-torneos` hacia `servicio-notificaciones` (`POST /v1/notificaciones/emails`). `[Luis Miguel]`
- [X] **2.4.2** Despacho asíncrono de emails por eventos: confirmación de postulación (FN-14), resolución de admisión (aprobada con credencial de delegado o rechazada con motivo, RN-12). `[Luis Miguel]`
- *Criterio de Aceptación 2.4:* Correos recibidos e inspeccionables en la interfaz web de Mailpit (`http://localhost:8025`).
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261111-24]`

#### Subsección 2.5: Enrutamiento y Seguridad Perimetral de Torneos en API Gateway
- [X] **2.5.1** Configuración de rutas en `api-gateway` para `/v1/torneos/**` y `/v1/solicitudes-inscripcion/**`. `[Tomas]`
- [X] **2.5.2** Reglas RBAC: `POST /v1/torneos` y `PATCH /v1/solicitudes-inscripcion/*/admision` exclusivos para `ROL_ORGANIZADOR`; `POST /v1/torneos/*/solicitudes-inscripcion` exclusivo para `ROL_FANATICO`; `GET /v1/torneos/**` abierto a cualquier usuario autenticado. `[Tomas]`
- *Criterio de Aceptación 2.5:* Peticiones de fanáticos a endpoints de organizador devuelven `403 Forbidden` bajo RFC 7807.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261111-25]`

#### Subsección 2.6: Infraestructura Base OpenAPI 3.0 y Agregación Swagger UI en Gateway
- [X] **2.6.1** Configuración de SpringDoc OpenAPI en `servicio-torneos` (`/api/torneos/v3/api-docs`). `[Tomas]`
- [X] **2.6.2** Configuración de SpringDoc OpenAPI en `servicio-operaciones-partidos` (`/api/partidos/v3/api-docs`). `[Tomas]`
- [X] **2.6.3** Swagger UI unificado en `api-gateway` (`http://localhost:8080/swagger-ui.html`) con selector de definiciones. `[Tomas]`
- *Criterio de Aceptación 2.6:* Swagger UI accesible y renderizando los esquemas JSON de ambos microservicios.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261111-26]`

> 🔒 **COMPUERTA DE CALIDAD (GATE 2A) [SUPERADA Y CERTIFICADA]:** Ciclo completo de torneos verificado: creación con cupos válidos, postulación de planteles, admisión de delegados, generación matemática de fixtures y recepción de cruces en Operaciones. Certificado en `docs/audit/2.0-cierre-etapa-2.md`.

---

### ETAPA 3: Core de Operaciones, Arbitraje, Posiciones y Planteles
- **Objetivo:** Logística de sedes y árbitros, asignación horaria con regla anti-superposición, ciclo arbitral completo (actas con goles y tarjetas por dorsal, penales en llaves), tabla de posiciones atómica y gestión de reemplazos/bajas de planteles.
- **Responsables:** Santiago Mariano Gonzalez, Gastón Emiliano Cambos Berrondo, Tomas Augusto Quiróz, Luis Miguel Urviola Dueñas.

#### Subsección 3.1: Logística de Sedes, Empadronamiento de Árbitros y Asignaciones Horarias
- [X] **3.1.1** Entidades JPA `Sede` (con geocodificación satelital y fallback manual, RN-06) y `Arbitro` (identificado por DNI como PK, FN-03) en `esquema_operaciones`. `[Santiago]`
- [X] **3.1.2** `POST /v1/sedes` y `POST /v1/arbitros` (exclusivos `ROL_ORGANIZADOR`). `[Santiago]`
- [X] **3.1.3** `POST /v1/partidos/{id}/asignaciones`: Asignación de sede, árbitro y fecha/hora con **control algorítmico anti-superposición horaria** ($\pm 2$ horas, RN-05). Conflicto $\to$ `409 Conflict`. `[Santiago]`
- *Criterio de Aceptación 3.1:* Asignación en ventana de conflicto responde `409 Conflict` con detalle RFC 7807.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261112-31]`

#### Subsección 3.2: Ciclo Arbitral Completo, Actas por Dorsal y Definición por Penales
- [X] **3.2.1** `PATCH /v1/partidos/{id}/estado`: Inicio de partido a `EN_CURSO` validando que el `sub` del JWT coincida con el árbitro asignado (RN-08, ajeno $\to$ `403 Forbidden`). `[Santiago]`
- [X] **3.2.2** `POST /v1/partidos/{id}/actas`: Registro de incidencias (goles, amarillas, rojas por dorsal) y definición obligatoria por penales en Llaves si empatado en 90' (`penales_local != penales_visitante`, RN-10). Transición a `FINALIZADO` y recálculo de posiciones. `[Santiago]`
- [X] **3.2.3** Cancelación por incidentes: Pasa a `CANCELADO`. En Liga suma PJ y PP a ambos sin puntos ni goles. En Llaves descalifica ambos equipos vía Torneos y resuelve avance del rival. `[Santiago]`
- *Criterio de Aceptación 3.2:* Árbitro no asignado recibe `403 Forbidden`; acta con empate en llaves sin penales responde `400 Bad Request`; ganador de penales avanza al siguiente cruce.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261113-32]`

#### Subsección 3.3: Motor de Tabla de Posiciones y Estadísticas
- [X] **3.3.1** Entidad JPA `TablaPosicion` en `esquema_operaciones` con cálculo atómico: PJ, PG (3 pts), PE (1 pt), PP (0 pts), GF, GC, DG. `[Santiago]`
- [X] **3.3.2** `GET /v1/torneos/{idTorneo}/posiciones`: Consulta de tabla ordenada por Puntos, DG, GF y PG. `[Santiago]`
- [X] **3.3.3** `GET /v1/torneos/{idTorneo}/goleadores`: Ranking de máximos artilleros acumulado por dorsal y equipo. `[Santiago]`
- *Criterio de Aceptación 3.3:* Victoria suma 3 puntos; empate suma 1 punto; tabla refleja DG y posiciones de forma atómica e inmediata tras cada acta.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261113-33]`

#### Subsección 3.4: Sincronización de Brackets y Ciclo de Reemplazos de Jugadores
- [X] **3.4.1** `POST /v1/equipos/{id}/reemplazos-jugadores` para el delegado: Máximo 5 cambios (`cambios_realizados < 5`) y prohibición de sustituir jugadores suspendidos (`suspendido == true`, RN-11). Violaciones $\to$ `400 Bad Request`. Baja lógica del saliente (`activo = false`) y alta del entrante. `[Gastón]`
- [X] **3.4.2** `DELETE /v1/torneos/{idTorneo}/equipos/{idEquipo}`: Baja voluntaria o descalificación de plantel (FN-13), notificando al delegado. `[Gastón]`
- *Criterio de Aceptación 3.4:* Reemplazo de un sexto jugador responde `400 Bad Request`; sustitución de suspendido responde `400 Bad Request`; delegado recibe email de baja en Mailpit.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261112-34]`

#### Subsección 3.5: Seguridad Perimetral y Reglas de Autorización Arbitral en API Gateway
- [X] **3.5.1** Configuración de rutas en `api-gateway` para `/v1/partidos/**`, `/v1/sedes/**`, `/v1/arbitros/**`. `[Tomas]`
- [X] **3.5.2** Reglas RBAC: `POST /v1/partidos/*/actas` y `PATCH /v1/partidos/*/estado` exclusivos `ROL_ARBITRO`; gestión de sedes/árbitros exclusivo `ROL_ORGANIZADOR`; consultas GET abiertas a usuarios autenticados. `[Tomas]`
- *Criterio de Aceptación 3.5:* Intento de cargar acta por un organizador o delegado responde `403 Forbidden`.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261113-35]`

#### Subsección 3.6: DTOs Especializados, Excepciones Arbitrales y Contratos Comunes
- [X] **3.6.1** Incorporar en `fulvo-common-lib`: `ArbitroNoAsignadoException`, `ConflictoHorarioException`, `EmpateNoPermitidoException`. `[Luis Miguel]`
- [X] **3.6.2** DTOs normalizados para actas, incidencias y cruces. Mapeo en `@RestControllerAdvice` con códigos RFC 7807 (400, 403, 409). Reinstalar en `.m2`. `[Luis Miguel]`
- *Criterio de Aceptación 3.6:* Nueva versión de la librería compilada e instalada en `.m2` consumida por los microservicios sin errores de dependencia.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar. `[COMPLETADO - AUDIT-20261113-36]`

> 🔒 **COMPUERTA DE CALIDAD (GATE 2B) [SUPERADA Y CERTIFICADA]:** Circuito arbitral completo, asignación horaria anti-superposición verificada, tabla de posiciones calculada atómicamente y planteles sincronizados. Validado de punta a punta y documentado en `docs/audit/3.0-cierre-etapa-3.md`.

---

## DÍA 3 / SPRINT 3 (Parte A): Adaptadores Externos, Resiliencia y Scheduler Preventivo

### ETAPA 4: Adaptadores Externos, Resiliencia y Scheduler Preventivo
- **Objetivo:** Microservicios adaptadores aislados consumiendo APIs externas con Circuit Breakers (`Resilience4j`), soporte de WireMock, Scheduler preventivo a 24 horas y consistencia de certámenes ante contingencias climáticas.
- **Responsables:** Luis Miguel Urviola Dueñas, Santiago Mariano Gonzalez, Gastón Emiliano Cambos Berrondo, Tomas Augusto Quiróz.

#### Subsección 4.1: Adaptador de Geocodificación y Fallback (`servicio-geocodificacion`)
- **Estado:** `[COMPLETADA]`. Cierre registrado en `docs/audit/4.1-adaptador-geocodificacion.md`.
- [X] **4.1.1** Scaffolding Spring Boot del microservicio en puerto `8084` con cliente `RestClient` hacia OpenStreetMap (Nominatim) o WireMock (:8086). `[Luis Miguel]`
- [X] **4.1.2** Protección de tolerancia a fallos con Circuit Breaker (`Resilience4j`). `[Luis Miguel]`
- [X] **4.1.3** Lógica de Fallback Manual (RN-06): si la geocodificación satelital falla y se proveyeron coordenadas manuales, retorna `201 Created`; sin coordenadas manuales retorna `422 Unprocessable Entity`; caída del proveedor retorna `503`. `[Luis Miguel]`
- *Criterio de Aceptación 4.1:* Resolución normal devuelve `200 OK`; fallback manual devuelve `201 Created`; omisión devuelve `422 Unprocessable Entity`.
- *Auditoría:* `docs/audit/4.1-adaptador-geocodificacion.md`.

#### Subsección 4.2: Adaptador de Clima y Evaluación de Severidad (`servicio-clima`)
- **Estado:** `[COMPLETADA]`. Cierre registrado en `docs/audit/4.2-adaptador-clima.md`.
- [X] **4.2.1** Scaffolding Spring Boot del microservicio en puerto `8085` con cliente `RestClient` hacia Open-Meteo o WireMock (:8086). `[Luis Miguel]`
- [X] **4.2.2** Algoritmo de evaluación de severidad climática: precipitación $> 40\text{ mm}$ o códigos WMO de tormenta $\to$ `condicion_adversa = true` (RN-07). Circuit Breaker integrado. `[Luis Miguel]`
- *Criterio de Aceptación 4.2:* `GET /v1/clima/pronosticos` evalúa severidad y retorna flag booleano con códigos HTTP RFC 7807.
- *Auditoría:* `docs/audit/4.2-adaptador-clima.md`.

#### Subsección 4.3: Adaptador de Notificaciones Transaccionales (`servicio-notificaciones`)
- **Estado:** `[COMPLETADA]`. Cierre registrado en `docs/audit/4.3-adaptador-notificaciones.md`.
- [X] **4.3.1** Scaffolding Spring Boot y configuración de `JavaMailSender` apuntando a Mailpit (puerto 1025). `[Luis Miguel]`
- [X] **4.3.2** `POST /v1/notificaciones/emails` con control silencioso de excepciones que preserve la transacción del llamador (FN-14, RN-12). `[Luis Miguel]`
- *Criterio de Aceptación 4.3:* Emails visibles en la bandeja web de Mailpit (`http://localhost:8025`).
- *Auditoría:* `docs/audit/4.3-adaptador-notificaciones.md`.

#### Subsección 4.4: Scheduler Climático Preventivo a 24 Horas y Reprogramación
- [X] **4.4.1** Implementar `@Scheduled` en `servicio-operaciones-partidos` consultando partidos a 24h. `[Luis Miguel]`
- [X] **4.4.2** Suspender automáticamente encuentros ante tormenta severa (`SUSPENDIDO`, RN-07) y sincronizar con Torneos 4.5, que notifica a los delegados (RN-12). `[Luis Miguel; correcciones Santiago]`
- [X] **4.4.3** `POST /v1/partidos/{id}/reprogramaciones` para reasignar fecha y sede pasando a `REPROGRAMADO`. `[Luis Miguel]`
- *Criterio de Aceptación 4.4:* El scheduler suspende el encuentro ante tormenta y envía el correo de alerta al delegado.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/4.4-scheduler-climatico-reprogramacion.md` al finalizar. `[COMPLETADO - AUDIT-20261113-44]`
- *Revisión corregida:* Ver `docs/audit/4.4-correcciones-integracion-concurrencia.md`. 22 comprobaciones HTTP/SQL con Torneos 4.5 real; Clima y correo simulados. Requiere integrar/desplegar también 4.5. No certifica Gate 3.

#### Subsección 4.5: Consumo de Notificaciones de Suspensión y Consistencia de Torneos
- **Estado:** `[COMPLETADA]`. Cierre integrado con 4.4 registrado en `docs/audit/4.5-cierre-integracion-postman.md` (2026-11-15).
- [X] **4.5.1** Integración en `servicio-torneos` para procesar el impacto de suspensiones y reprogramaciones sobre el cronograma del torneo. `[Gastón]`
- [X] **4.5.2** Mantener consistencia del certamen y trazabilidad de los partidos pendientes para los delegados. `[Gastón]`
- *Criterio de Aceptación 4.5:* El cronograma del torneo refleja los partidos suspendidos sin corromper cruces.
- *Auditoría:* `docs/audit/4.5-suspensiones-consistencia-torneos.md`.
- *Validación de cierre:* 23 comprobaciones HTTP/SQL de 4.5 y 22 de integración 4.4/4.5; merge compilado en ambos servicios. Santiago confirmó desde Postman el acta del partido reprogramado, estado FINALIZADO y actualización de puntos en PostgreSQL. Ver alcance y límites en la auditoría de cierre; Gate 3 no certificado por este cierre.

#### Subsección 4.6: Enrutamiento y Aislamiento de Servicios Adaptadores en API Gateway
- **Estado:** `[COMPLETADA]`. Auditoría en `docs/audit/4.6-gateway-perimetral-adaptadores.md`.
- [X] **4.6.1** Configuración de rutas en `api-gateway` para adaptadores (`/v1/geocodificaciones/**`, `/v1/clima/**`, `/v1/notificaciones/**`). `[Tomas]`
- [X] **4.6.2** Aislamiento perimetral impidiendo acceso público anónimo directo a las APIs internas de notificación. `[Tomas]`
- *Criterio de Aceptación 4.6:* Rutas de adaptadores securizadas y operativas a través del Gateway.
- *Auditoría:* `docs/audit/4.6-gateway-perimetral-adaptadores.md`. `[COMPLETADO - AUDIT-20261114-46]`

> 🔒 **COMPUERTA DE CALIDAD (GATE 3):** Adaptadores y Scheduler integrados y certificados contra Mailpit y APIs públicas / WireMock, auditados en `docs/audit/`.

---

### ETAPA 5: Despliegue Docker, Swagger y Postman E2E
- **Objetivo:** Despliegue contenerizado en red cerrada, documentación OpenAPI en todos los módulos y certificación completa de escenarios oficiales en Postman.
- **Responsables:** Tomas Augusto Quiróz, Gastón Emiliano Cambos Berrondo, Santiago Mariano Gonzalez, Luis Miguel Urviola Dueñas.

#### Subsección 5.1: Orquestación Integral Docker Compose y Red Perimetral `bda-net`
- [ ] **5.1.1** Creación de `Dockerfile` JRE alpine optimizado en los 6 microservicios. `[Tomas]`
- [ ] **5.1.2** `docker-compose.yml` unificado con los 6 servicios Java, Postgres, Keycloak, Mailpit y WireMock en la red cerrada `bda-net`. `[Tomas]`
- [ ] **5.1.3** Aislamiento perimetral: Únicamente `api-gateway` expone el puerto `8080`, Keycloak `8090`, Mailpit `8025` y WireMock `8086`. Los microservicios de negocio operan exclusivamente en la red interna aislada. `[Tomas]`
- *Criterio de Aceptación 5.1:* `docker compose up -d` exitoso; acceso directo a puertos de microservicios internos bloqueado.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar.

#### Subsección 5.2: Enriquecimiento y Documentación OpenAPI 3.0 en `servicio-torneos`
- [ ] **5.2.1** Anotaciones exhaustivas de esquemas, descripciones y respuestas RFC 7807 en `servicio-torneos`. `[Gastón]`
- *Criterio de Aceptación 5.2:* Swagger UI de Torneos documenta el 100% de endpoints con códigos de negocio.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar.

#### Subsección 5.3: Enriquecimiento y Documentación OpenAPI 3.0 en `servicio-operaciones-partidos`
- [ ] **5.3.1** Anotaciones exhaustivas de sedes, árbitros, actas, posiciones y goleadores. `[Santiago]`
- *Criterio de Aceptación 5.3:* Swagger UI de Operaciones documenta el 100% de endpoints con esquemas y errores.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar.

#### Subsección 5.4: Auditoría y Verificación de Swagger UI en API Gateway y Esquemas de Seguridad JWT
- [ ] **5.4.1** Verificación en Swagger UI unificado (`http://localhost:8080/swagger-ui.html`) alternando entre definiciones de microservicios y autenticación interactiva `Bearer` con Keycloak. `[Tomas]`
- *Criterio de Aceptación 5.4:* Swagger UI interactivo y securizado navegable desde el Gateway.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar.

#### Subsección 5.5: Colección Automatizada Postman E2E de los 21 Escenarios Oficiales
- [ ] **5.5.1** Automatización de los 10 escenarios positivos canónicos (`EP-01` a `EP-10`). `[Todo el Equipo]`
- [ ] **5.5.2** Automatización de los 11 escenarios negativos canónicos (`EN-01` a `EN-11`). `[Todo el Equipo]`
- *Criterio de Aceptación 5.5:* Scripts `pm.test()` en Postman ejecutando el 100% de los escenarios con aserciones sobre status HTTP, RFC 7807 y payloads.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar.

#### Subsección 5.6: Certificación Final E2E con Newman / Reporte de Resultados
- [ ] **5.6.1** Ejecución completa de la suite con Newman contra el entorno Docker Compose levantado. `[Todo el Equipo]`
- [ ] **5.6.2** Reporte final de certificación y congelamiento del repositorio para defensa. `[Todo el Equipo]`
- *Criterio de Aceptación 5.6:* 100% de tests pasando en Newman; cero regresiones.
- *Auditoría Requerida:* Registrar entrada en `docs/audit/` al finalizar.

> 🔒 **COMPUERTA DE CALIDAD FINAL (GATE 4):** Despliegue unificado en Docker Compose certificado, documentación OpenAPI completa y 21 escenarios automatizados superados al 100% en Postman.
