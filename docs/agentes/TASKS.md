# Tablero de Control y Trazabilidad de Tareas - TPI Fulvo'

> **Estado:** `PLAN APROBADO - EN ESPERA DE ARRANQUE`  
> **Regla de Operación Obligatoria:** 
> 1. Antes de codear cualquier tarea, leer `CONTEXT.md`, `PLAN_DESARROLLO.md`, `DISENO_SISTEMA_FULVO.md` y las últimas entradas de `AUDIT_LOG.md`.
> 2. Al finalizar cada subsección, validar los endpoints y flujos según `GUIA_PRUEBAS_MANUALES.md`, marcar las casillas completadas `[X]` en este archivo y **registrar la auditoría correspondiente en `AUDIT_LOG.md`** detallando qué se hizo, qué se arregló sobre la marcha y si hubo diferencias con el plan original.
> 3. Al concluir cada etapa formal, verificar la superación de su compuerta de calidad (Gate).
> 4. **Directiva de Participación Balanceada:** En cada una de las 5 etapas, todos los integrantes (**Tomas, Gastón, Santiago y Luis Miguel**) son responsables de al menos una subsección.
> 5. **Protocolo Anti-Suposición:** Ante cualquier duda de dominio, ambigüedad o dilema técnico, el agente debe pausar y consultar al desarrollador con opciones justificadas y recomendación técnica antes de implementar.
> 6. **Aislamiento en Git y Flujo de PR por el Desarrollador (Obligatorio):** Está prohibido desarrollar directamente en `main`. El agente **DEBE crear y cambiarse a una rama propia** desde `main` (`feature/<etapa>-<subseccion>-<descripcion-corta>`) antes de empezar a codear cualquier tarea. Los agentes tienen **estrictamente prohibido pushear a GitHub (`git push`)**. Al finalizar y validar la tarea, el desarrollador humano es quien pushea la rama a mano (`git push -u origin feature/...`), crea la correspondiente **Pull Request (PR)** en GitHub para incorporar los cambios a `main`, y tras la aprobación y merge, elimina la rama.

---

## Estado Global del Proyecto (Sprint 3 Días)

| Sprint | Etapa | Descripción | Responsables por Etapa | Estado | Gate Superado | Auditoría |
|---|---|---|---|---|:---:|:---:|
| **Día 0** | **Etapa 0** | Planificación, Convenciones y Roles | Todo el Equipo | `[COMPLETADO]` | `[X]` | `[X]` (AUDIT-00) |
| **Día 1** | **Etapa 1** | Infraestructura Base, Common Lib, Scaffolding y Gateway | Tomas, Luis Miguel, Gastón, Santiago | `[PENDIENTE]` | `[ ]` | `[ ]` |
| **Día 2** | **Etapa 2** | Core de Torneos, Admisiones y Contrato de Fixtures | Gastón, Santiago, Luis Miguel, Tomas | `[PENDIENTE]` | `[ ]` | `[ ]` |
| **Día 2** | **Etapa 3** | Core de Operaciones, Arbitraje, Posiciones y Planteles | Santiago, Gastón, Tomas, Luis Miguel | `[PENDIENTE]` | `[ ]` | `[ ]` |
| **Día 3** | **Etapa 4** | Adaptadores Externos, Resiliencia y Scheduler | Luis Miguel, Santiago, Gastón, Tomas | `[PENDIENTE]` | `[ ]` | `[ ]` |
| **Día 3** | **Etapa 5** | Despliegue Docker, Swagger y Postman E2E | Tomas, Gastón, Santiago, Luis Miguel | `[PENDIENTE]` | `[ ]` | `[ ]` |

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
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar. `[COMPLETADO - AUDIT-20261108-11]`

#### Subsección 1.2: Librería Compartida `fulvo-common-lib` (Instalación en `.m2`)
- [ ] **1.2.1** Inicializar proyecto Maven `fulvo-common-lib` (`com.fulvo:fulvo-common-lib:1.0.0-SNAPSHOT`, empaquetado `.jar`). `[Luis Miguel]`
- [ ] **1.2.2** Implementar DTOs inter-servicio en `com.fulvo.common.dto`: `CrucePartidoDTO`, `NotificacionEmailDTO`, `GeocodificacionDTO`, `ClimaPronosticoDTO`. `[Luis Miguel]`
- [ ] **1.2.3** Definir jerarquía de excepciones en `com.fulvo.common.exception`: `BusinessRuleException`, `RecursoNoEncontradoException`, `ConflictoEstadoException`. `[Luis Miguel]`
- [ ] **1.2.4** Implementar `@RestControllerAdvice` global retornando **RFC 7807 (`ProblemDetail`)** de Spring 6 (Apuntes 18 y 23). `[Luis Miguel]`
- [ ] **1.2.5** Implementar `CorrelationIdFilter` (`OncePerRequestFilter`) propagando cabecera `X-Correlation-ID` al MDC de SLF4J (Apunte 23). `[Luis Miguel]`
- [ ] **1.2.6** Compilar e instalar librería en repositorio local: `mvn clean install -DskipTests`. `[Luis Miguel]`
- *Criterio de Aceptación 1.2:* Artefacto disponible en `~/.m2` local listo para ser importado por los microservicios.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 1.3: Scaffolding Base y Configuración DataSource de `servicio-torneos`
- [ ] **1.3.1** Inicializar microservicio `servicio-torneos` (`com.fulvo:servicio-torneos`, Java 21, Spring Boot 3.3.4). `[Gastón]`
- [ ] **1.3.2** Configurar `application.yml` conectando a PostgreSQL y definiendo `default_schema: esquema_torneos`. `[Gastón]`
- [ ] **1.3.3** Importar dependencia `fulvo-common-lib` desde `.m2` local y estructurar paquetes base `com.fulvo.torneos.*`. `[Gastón]`
- [ ] **1.3.4** Verificar compilación limpia y arranque inicial en puerto `8081`. `[Gastón]`
- *Criterio de Aceptación 1.3:* `mvn clean package -DskipTests` exitoso y conexión validada con el esquema lógico de torneos.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 1.4: Scaffolding Base y Configuración DataSource de `servicio-operaciones-partidos`
- [ ] **1.4.1** Inicializar microservicio `servicio-operaciones-partidos` (`com.fulvo:servicio-operaciones-partidos`, Java 21, Spring Boot 3.3.4). `[Santiago]`
- [ ] **1.4.2** Configurar `application.yml` conectando a PostgreSQL y definiendo `default_schema: esquema_operaciones`. `[Santiago]`
- [ ] **1.4.3** Importar dependencia `fulvo-common-lib` desde `.m2` local y estructurar paquetes base `com.fulvo.operaciones.*`. `[Santiago]`
- [ ] **1.4.4** Verificar compilación limpia y arranque inicial en puerto `8082`. `[Santiago]`
- *Criterio de Aceptación 1.4:* `mvn clean package -DskipTests` exitoso y conexión validada con el esquema lógico de operaciones.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 1.5: Gateway Reactivo (`api-gateway`) con `RouteLocatorBuilder` y Seguridad OIDC
- [ ] **1.5.1** Inicializar proyecto Spring Boot `api-gateway` con `spring-cloud-starter-gateway` (`com.fulvo:api-gateway`, Java 21, Spring Boot 3.3.4). `[Tomas]`
- [ ] **1.5.2** Configurar bean `RouteLocator` con `RouteLocatorBuilder` (Apunte 19) enrutando hacia downstream (`/v1/torneos/**`, `/v1/partidos/**`, adaptadores). `[Tomas]`
- [ ] **1.5.3** Implementar convertidor reactivo de roles `realm_access.roles` $\to$ autoridades `ROL_*` (`ROL_ORGANIZADOR`, `ROL_DELEGADO`, `ROL_ARBITRO`, `ROL_FANATICO`) con `Converter<Jwt, Mono<AbstractAuthenticationToken>>` (Apunte 21). `[Tomas]`
- [ ] **1.5.4** Configurar `SecurityWebFilterChain` reactiva bloqueando anónimos (401) y verificando roles (403). `[Tomas]`
- [ ] **1.5.5** Exponer y verificar endpoint `/actuator/health` en puerto `8080`. `[Tomas]`
- *Criterio de Aceptación 1.5:* Petición sin token responde 401; token de fanático a POST torneo responde 403; health responde UP (200).
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

> 🔴 **COMPUERTA DE CALIDAD (GATE 1):** No se inicia el Día 2 sin que Gateway, Postgres, Keycloak, `fulvo-common-lib` en `.m2` y los scaffoldings de torneos y operaciones estén certificados y auditados en `AUDIT_LOG.md`.

---

## DÍA 2 / SPRINT 2: Servicios Core de Negocio y Lógica Transaccional

### ETAPA 2: Core de Torneos, Admisiones y Contrato de Fixtures
- **Objetivo:** Organización de certámenes, admisiones, validación de planteles de buena fe, fixture determinístico, recepción en operaciones y perimetría en Gateway.
- **Responsables:** Gastón Emiliano Cambos Berrondo, Santiago Mariano Gonzalez, Luis Miguel Urviola Dueñas, Tomas Augusto Quiróz.

#### Subsección 2.1: Modelado JPA y Lógica de Admisión de Torneos
- [ ] **2.1.1** Crear entidades JPA en `esquema_torneos`: `Torneo`, `SolicitudInscripcion`, `Equipo` y `Jugador` (DNI String PK natural) y repositorios. `[Gastón]`
- [ ] **2.1.2** `POST /v1/torneos` con validación de cupo estricto (10/20 Liga, 8/16 Llaves) (FN-01, RN-01). `[Gastón]`
- [ ] **2.1.3** `POST /v1/torneos/{id}/solicitudes-inscripcion` validando 15-22 jugadores, arquero, capitán y DNI único por certamen (FN-04, RN-02, RN-03). `[Gastón]`
- [ ] **2.1.4** `PATCH /v1/solicitudes-inscripcion/{id}/admision`: Aprobación administrativa, creación de `Equipo` en `ACTIVO`, promoción a `ROL_DELEGADO` y descuento de cupo o rechazo con motivo (FN-05, RN-04). `[Gastón]`
- *Criterio de Aceptación 2.1:* No se permite violar cupo ni reglas de buena fe; las solicitudes admitidas descuentan cupo y crean el equipo en estado activo.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 2.2: Generación Algorítmica Determinística de Fixtures
- [ ] **2.2.1** `PATCH /v1/torneos/{id}/estado` cerrando inscripción únicamente al 100% de cupo (FN-06, RN-01). `[Gastón]`
- [ ] **2.2.2** Implementar algoritmo Round Robin (Liga) y árbol de emparejamiento con cruces posteriores en `null` (Llaves); transición a `INICIADO`. `[Gastón]`
- [ ] **2.2.3** Cliente `RestClient` inyectado por constructor con `@RequiredArgsConstructor` (Apunte 18) para despachar lote de cruces a `servicio-operaciones-partidos`. `[Gastón]`
- *Criterio de Aceptación 2.2:* Intento de cierre con cupo incompleto responde 409; cierre al 100% genera y despacha fixture completo.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 2.3: Contrato y Recepción de Cruces en Lote en Operaciones
- [ ] **2.3.1** Implementar endpoint `POST /v1/partidos/lote` en `servicio-operaciones-partidos` recibiendo `List<CrucePartidoDTO>`. `[Santiago]`
- [ ] **2.3.2** Persistir masivamente los partidos recibidos en estado `PROGRAMADO` en `esquema_operaciones` desacoplados de tablas de torneos. `[Santiago]`
- *Criterio de Aceptación 2.3:* Operaciones persiste los partidos sin claves foráneas físicas directas hacia el esquema de torneos.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 2.4: Integración del Cliente RestClient y Notificaciones de Admisión
- [ ] **2.4.1** Implementar cliente `RestClient` en `servicio-torneos` invocando asíncronamente a `servicio-notificaciones` (`POST /v1/notificaciones/emails`). `[Luis Miguel]`
- [ ] **2.4.2** Despachar confirmación de postulación (FN-14) y resolución de admisión (aprobado/rechazado) vía Mailpit (RN-12). `[Luis Miguel]`
- *Criterio de Aceptación 2.4:* Los correos de confirmación y resolución llegan a la bandeja web de Mailpit (`:8025`).
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 2.5: Enrutamiento Perimetral y Reglas de Seguridad para Torneos en API Gateway
- [ ] **2.5.1** Configurar rutas en Gateway `/v1/torneos/**` y `/v1/solicitudes-inscripcion/**` con validación reactiva de roles. `[Tomas]`
- [ ] **2.5.2** Bloquear intentos de creación de torneo por roles ajenos a `ROL_ORGANIZADOR` con 403 Forbidden. `[Tomas]`
- *Criterio de Aceptación 2.5:* El Gateway filtra las solicitudes y enruta correctamente al puerto 8081.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

> 🔴 **COMPUERTA DE CALIDAD (GATE 2A):** Torneos y Operaciones interactúan fluidamente vía RestClient en la generación y recepción de fixtures.

---

### ETAPA 3: Core de Operaciones, Arbitraje, Posiciones y Planteles
- **Objetivo:** Sedes, árbitros, agenda horaria, máquina de estados de partidos, actas por dorsal, definición en llaves, tabla de posiciones y perimetría arbitral.
- **Responsables:** Santiago Mariano Gonzalez, Gastón Emiliano Cambos Berrondo, Tomas Augusto Quiróz, Luis Miguel Urviola Dueñas.

#### Subsección 3.1: Sedes, Árbitros y Asignación Anti-Superposición
- [ ] **3.1.1** Crear entidades JPA en `esquema_operaciones`: `Sede`, `Arbitro`, `Partido`, `ActuacionJugador`, `TablaPosicion`. `[Santiago]`
- [ ] **3.1.2** Endpoints `POST /v1/sedes` y `POST /v1/arbitros`: Registro de predios y padrón arbitral. `[Santiago]`
- [ ] **3.1.3** `POST /v1/partidos/{id}/asignaciones` con validación estricta de ventana anti-superposición ($\pm 2$ horas) para sede y árbitro (FN-07, RN-05). `[Santiago]`
- *Criterio de Aceptación 3.1:* Intento de superposición horaria en sede o árbitro responde 409 Conflict (ProblemDetail).
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 3.2: Circuito Arbitral, Actas Oficiales y Desempate en Llaves
- [ ] **3.2.1** `PATCH /v1/partidos/{id}/estado` a `EN_CURSO` validando identidad del árbitro (`@AuthenticationPrincipal Jwt jwt` vs `partido.arbitro_dni`, FN-09, RN-08). `[Santiago]`
- [ ] **3.2.2** `POST /v1/partidos/{id}/actas`: Registro de goles y tarjetas por dorsal. Si hay empate en Llaves, exigencia de penales con desempate obligatorio (RN-10). Transición a `FINALIZADO` y promoción del ganador a la siguiente fase de llaves. `[Santiago]`
- [ ] **3.2.3** Cancelación por incidentes en `EN_CURSO` transicionando a `CANCELADO` y asignando 0 puntos en tabla o descalificación mutua (FN-10, RN-09). `[Santiago]`
- *Criterio de Aceptación 3.2:* Árbitro no asignado es rechazado con 403 Forbidden; empate en Llaves sin penales rechaza con 400 Bad Request; acta actualiza estado del partido y promueve al ganador.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 3.3: Motor de Tabla de Posiciones y Estadísticas
- [ ] **3.3.1** Implementar cómputo transaccional de posiciones (PJ, PG [3 pts], PE [1 pt], PP [0 pts], GF, GC, DG, Pts). `[Santiago]`
- [ ] **3.3.2** Endpoints `GET /v1/torneos/{idTorneo}/posiciones` y `GET /v1/torneos/{idTorneo}/goleadores` (FN-12). `[Santiago]`
- *Criterio de Aceptación 3.3:* Al finalizar un partido, la tabla de posiciones refleja atómicamente los resultados y puntos.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 3.4: Sincronización de Brackets y Ciclo de Reemplazos de Jugadores
- [ ] **3.4.1** `POST /v1/equipos/{id}/reemplazos-jugadores` validando máximo 5 cambios y prohibición taxativa de sustituir futbolistas suspendidos (FN-11, RN-11). `[Gastón]`
- [ ] **3.4.2** `DELETE /v1/torneos/{idTorneo}/equipos/{idEquipo}` para baja voluntaria o descalificación de planteles (FN-13). `[Gastón]`
- *Criterio de Aceptación 3.4:* Sexto cambio o reemplazo de jugador suspendido rechaza con 400 Bad Request.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 3.5: Seguridad Perimetral y Reglas de Autorización Arbitral en API Gateway
- [ ] **3.5.1** Configurar rutas en Gateway `/v1/partidos/**`, `/v1/sedes/**` y `/v1/arbitros/**`. `[Tomas]`
- [ ] **3.5.2** Exigir rol `ROL_ARBITRO` para transiciones y actas; `ROL_ORGANIZADOR` para asignaciones; y acceso público/fanático para consultas de resultados. `[Tomas]`
- *Criterio de Aceptación 3.5:* Intentos de asentar acta sin token de árbitro son rechazados en el perímetro del Gateway.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 3.6: DTOs Especializados, Excepciones Arbitrales y Contratos Comunes
- [ ] **3.6.1** Incorporar en `fulvo-common-lib` las excepciones `ArbitroNoAsignadoException`, `ConflictoHorarioException`, `EmpateNoPermitidoException`. `[Luis Miguel]`
- [ ] **3.6.2** Definir DTOs de actas y reinstalar en `.m2` (`mvn clean install -DskipTests`). `[Luis Miguel]`
- *Criterio de Aceptación 3.6:* Artefacto común actualizado y consumible con excepciones arbitrales estandarizadas.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

> 🔴 **COMPUERTA DE CALIDAD (GATE 2B):** Torneos y Operaciones integrados con todas las reglas de negocio verificadas y auditadas en `AUDIT_LOG.md`.

---

## DÍA 3 / SPRINT 3: Adaptadores, Scheduler, Swagger y Validación Postman E2E

### ETAPA 4: Adaptadores Externos, Resiliencia y Scheduler Preventivo
- **Objetivo:** Microservicios adaptadores protegidos con Circuit Breaker, emulación WireMock, proceso desatendido climático y resiliencia en Gateway.
- **Responsables:** Luis Miguel Urviola Dueñas, Santiago Mariano Gonzalez, Gastón Emiliano Cambos Berrondo, Tomas Augusto Quiróz.

#### Subsección 4.1: Adaptador de Geocodificación y Fallback (`servicio-geocodificacion`)
- [ ] **4.1.1** Scaffolding Spring Boot y consumo de API OpenStreetMap Nominatim / WireMock vía RestClient (Apunte 18). `[Luis Miguel]`
- [ ] **4.1.2** Configurar Resilience4j Circuit Breaker y fallback manual ante corte de red o dirección inválida (FN-02, RN-06). `[Luis Miguel]`
- *Criterio de Aceptación 4.1:* Si Nominatim/WireMock responde retorna lat/lon oficiales; si falla y vienen coordenadas manuales responde 201; si falla sin datos manuales responde 422.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 4.2: Adaptador de Clima y Evaluación de Severidad (`servicio-clima`)
- [ ] **4.2.1** Scaffolding Spring Boot y consumo de API Open-Meteo / WireMock para coordenadas dadas vía RestClient. `[Luis Miguel]`
- [ ] **4.2.2** Evaluar umbral de severidad (>40mm de lluvia o códigos de tormenta/granizo) (FN-08, RN-07). `[Luis Miguel]`
- *Criterio de Aceptación 4.2:* Retorna evaluación climática correcta sin lanzar errores 500 ante caídas externas.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 4.3: Adaptador de Notificaciones Transaccionales (`servicio-notificaciones`)
- [ ] **4.3.1** Scaffolding Spring Boot y configuración de `JavaMailSender` apuntando a Mailpit (puerto 1025). `[Luis Miguel]`
- [ ] **4.3.2** `POST /v1/notificaciones/emails` con control silencioso de excepciones que preserve la transacción del llamador (FN-14, RN-12). `[Luis Miguel]`
- *Criterio de Aceptación 4.3:* Emails visibles en la bandeja web de Mailpit (`http://localhost:8025`).
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 4.4: Scheduler Climático Preventivo a 24 Horas y Reprogramación
- [ ] **4.4.1** Implementar `@Scheduled` en `servicio-operaciones-partidos` consultando partidos a 24h. `[Santiago]`
- [ ] **4.4.2** Suspender automáticamente encuentros ante tormenta severa (`SUSPENDIDO`, RN-07) e invocar Notificaciones (RN-12). `[Santiago]`
- [ ] **4.4.3** `POST /v1/partidos/{id}/reprogramaciones` para reasignar fecha y sede pasando a `REPROGRAMADO`. `[Santiago]`
- *Criterio de Aceptación 4.4:* El scheduler suspende el encuentro ante tormenta y envía el correo de alerta al delegado.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 4.5: Consumo de Notificaciones de Suspensión y Consistencia de Torneos
- [ ] **4.5.1** Integración en `servicio-torneos` para procesar el impacto de suspensiones y reprogramaciones sobre el cronograma del torneo. `[Gastón]`
- [ ] **4.5.2** Mantener consistencia del certamen y trazabilidad de los partidos pendientes para los delegados. `[Gastón]`
- *Criterio de Aceptación 4.5:* El cronograma del torneo refleja los partidos suspendidos sin corromper cruces.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 4.6: Enrutamiento y Aislamiento de Servicios Adaptadores en API Gateway
- [ ] **4.6.1** Configuración de rutas en `api-gateway` para adaptadores (`/v1/geocodificaciones/**`, `/v1/clima/**`, `/v1/notificaciones/**`). `[Tomas]`
- [ ] **4.6.2** Aislamiento perimetral impidiendo acceso público anónimo directo a las APIs internas de notificación. `[Tomas]`
- *Criterio de Aceptación 4.6:* Rutas de adaptadores securizadas y operativas a través del Gateway.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

> 🔴 **COMPUERTA DE CALIDAD (GATE 3):** Adaptadores y Scheduler integrados y certificados contra Mailpit y APIs públicas / WireMock, auditados en `AUDIT_LOG.md`.

---

### ETAPA 5: Despliegue Docker, Swagger y Postman E2E
- **Objetivo:** Despliegue contenerizado en red cerrada, documentación OpenAPI en todos los módulos y certificación completa de escenarios oficiales en Postman.
- **Responsables:** Tomas Augusto Quiróz, Gastón Emiliano Cambos Berrondo, Santiago Mariano Gonzalez, Luis Miguel Urviola Dueñas.

#### Subsección 5.1: Orquestación Integral Docker Compose y Red Perimetral `bda-net`
- [ ] **5.1.1** Crear `Dockerfile` canónico basado en `eclipse-temurin:21-jre-alpine` para cada uno de los 6 microservicios. `[Tomas]`
- [ ] **5.1.2** Configurar `docker-compose.yml` final de red interna `bda-net` con aislamiento perimetral (solo puertos 8080, 8090, 8025 y 8086 WireMock expuestos). `[Tomas]`
- [ ] **5.1.3** Configurar `docker-compose.override.yml` para depuración en desarrollo y arranque en frío con `docker compose up -d --build`. `[Tomas]`
- *Criterio de Aceptación 5.1:* Todo el backend levanta con un solo comando sin intervención manual.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 5.2: Documentación OpenAPI 3.0 de Torneos (`servicio-torneos`)
- [ ] **5.2.1** Integrar `springdoc-openapi-starter-webmvc-ui` en `servicio-torneos`. `[Gastón]`
- [ ] **5.2.2** Anotar endpoints, parámetros, esquemas de admisión, planteles y respuestas de error RFC 7807. `[Gastón]`
- *Criterio de Aceptación 5.2:* Endpoints del ciclo registral documentados exhaustivamente.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 5.3: Documentación OpenAPI 3.0 de Operaciones (`servicio-operaciones-partidos`)
- [ ] **5.3.1** Integrar `springdoc-openapi-starter-webmvc-ui` en `servicio-operaciones-partidos`. `[Santiago]`
- [ ] **5.3.2** Anotar endpoints de partidos, asignaciones, actas, sedes y tabla de posiciones. `[Santiago]`
- *Criterio de Aceptación 5.3:* Endpoints operativos y de arbitraje documentados con sus códigos de respuesta.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 5.4: Agregación de Swagger UI en API Gateway y Esquemas de Seguridad JWT
- [ ] **5.4.1** Exponer agregación centralizada de Swagger en `api-gateway` accesible en `http://localhost:8080/swagger-ui.html`. `[Tomas]`
- [ ] **5.4.2** Configurar seguridad JWT Bearer en OpenAPI y descripciones de respuestas RFC 7807 (`ProblemDetail`). `[Tomas]`
- *Criterio de Aceptación 5.4:* Swagger UI unificado accesible desde el puerto 8080.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 5.5: Automatización de Suite Postman Integral y Entorno de Pruebas
- [ ] **5.5.1** Crear colección `postman/Fulvo_Backend_Collection.postman_collection.json` y entorno `postman/Fulvo_Local_Environment.postman_environment.json`. `[Luis Miguel]`
- [ ] **5.5.2** Scripts de pre-request con auto-login en Keycloak para Admin, Delegado, Árbitro y Fanático extrayendo tokens e IDs dinámicamente. `[Luis Miguel]`
- *Criterio de Aceptación 5.5:* Ejecución 100% automatizada sin copia ni pega manual.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

#### Subsección 5.6: Certificación Colaborativa de Escenarios Oficiales Positivos y Negativos (EP y EN)
- [ ] **5.6.1** Certificar escenarios de seguridad perimetral (`EN-01` 401, `EN-02` 403). `[Tomas]`
- [ ] **5.6.2** Certificar escenarios del ciclo registral y torneos (`EP-01` a `EP-03`, `EN-03` a `EN-05`, `EN-10`). `[Gastón]`
- [ ] **5.6.3** Certificar escenarios operativos de partidos y arbitraje (`EP-04` a `EP-07`, `EN-07` a `EN-09`). `[Santiago]`
- [ ] **5.6.4** Certificar escenarios de resiliencia, clima y notificaciones (`EP-08`, `EP-09`, `EN-06`, `EN-11`, `EN-12`) y corrida integral en Newman. `[Luis Miguel]`
- *Criterio de Aceptación 5.6:* 100% de las pruebas en Postman (`pm.test()`) ejecutadas con resultado verde sin fallos.
- *Auditoría Requerida:* Registrar entrada en `AUDIT_LOG.md` al finalizar.

> 🏆 **COMPUERTA DE APROBACIÓN FINAL (GATE 4):** Backend completamente verificado, documentado en Swagger, desplegado en Docker y certificado de punta a punta con Postman y Mailpit, con la bitácora de auditoría cerrada en `AUDIT_LOG.md`.
