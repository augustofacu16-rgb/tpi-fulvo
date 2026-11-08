# Bitácora y Registro de Auditoría de Desarrollo - TPI Fulvo'

> **Propósito:** Este archivo es el registro histórico oficial de la evolución del proyecto. Documenta cronológicamente **cuándo y cómo se implementó cada sección**, los **problemas y soluciones encontrados sobre la marcha**, y las **desviaciones o adaptaciones respecto al plan original**. Sirve como instrumento clave de trazabilidad y material de estudio directo para la **defensa oral (coloquio) con los docentes de la cátedra**.
>
> **Regla:** Cada vez que se finalice una subsección o etapa en `TASKS.md`, se debe añadir obligatoriamente una nueva entrada al final de este archivo siguiendo la plantilla estándar.

---

## Plantilla Oficial de Entrada de Auditoría

```markdown
### [AUDIT-YYYYMMDD-ID] Etapa X - Subsección X.Y: [Nombre de la Subsección]

- **Fecha y Hora de Cierre:** YYYY-MM-DD HH:mm (Hora Local)
- **Estado:** COMPLETADO / EN OBSERVACIÓN
- **Gate de Etapa Asociado:** Gate X [SUPERADO / PENDIENTE]

#### 1. ¿Qué y Cómo se hizo?
* **Componentes / Módulos intervenidos:** [Listado de proyectos o carpetas]
* **Archivos creados o modificados:** [Rutas exactas de archivos Java, XML, YML, SQL]
* **Endpoints y Contratos implementados:** [Método HTTP, URI, DTOs de entrada y salida]
* **Lógica técnica y de dominio aplicada:** [Explicación concisa de servicios, repositorios, reglas y flujos ejecutados]

#### 2. ¿Qué hubo que arreglar sobre la marcha?
* **Problemas técnicos encontrados:** [Errores de compilación, excepciones en runtime, choques de puertos, dependencias incompatibles]
* **Soluciones y Workarounds aplicados:** [Cómo se resolvió cada incidencia paso a paso]

#### 3. ¿En qué difiere con lo establecido en el Plan Original?
* **Modificaciones del Modelo de Datos / Entidades:** [Cambios de campos, tipos de datos, constraints respecto a PLAN_DESARROLLO.md]
* **Ajustes en Funcionalidades o Reglas:** [Reglas adaptadas, simplificadas o ampliadas y su justificativo]
* **Motivo del cambio:** [Por qué fue necesario apartarse del plan inicial]

#### 4. Puntos Clave para la Defensa Oral (Coloquio con Docentes)
* **Decisiones de diseño a defender:** [Por qué se implementó de esta forma y no de otra]
* **Preguntas probables de los profesores:** [Qué aspectos del código o arquitectura pueden cuestionar y cómo responder con solvencia técnica]
```

---

## Registro Histórico de Auditorías

### [AUDIT-20261104-00] Inicialización: Definición Arquitectónica, Planificación y Convenciones

- **Fecha y Hora de Cierre:** 2026-11-04 11:55
- **Estado:** COMPLETADO
- **Gate de Etapa Asociado:** Gate 0 (Alineación y Aprobación del Plan) [SUPERADO]

#### 1. ¿Qué y Cómo se hizo?
* Se consolidó la especificación del sistema en `PLAN_DESARROLLO.md` y el tablero de control de avance en `TASKS.md`, basados en la documentación oficial entregada a la cátedra (`docs/Documentacion COMPLETA CUESTIONARIO BACKEND TP FULVO.pdf` y `docs/DER-Fulvo.pdf`).
* Se definió la arquitectura de microservicios: 2 microservicios core (`servicio-torneos`, `servicio-operaciones-partidos`), 1 API Gateway reactivo (`api-gateway`), 3 adaptadores de integración (`servicio-geocodificacion`, `servicio-clima`, `servicio-notificaciones`), y 1 librería compartida (`fulvo-common-lib`).
* Se crearon los documentos rectores `CONTEXT.md` (directivas previas para agentes y devs) y `AUDIT_LOG.md` (este registro).

#### 2. ¿Qué hubo que arreglar sobre la marcha?
* **Estructura Maven:** Se evaluó inicialmente un POM agregador multimodular raíz con `<modules>`, pero se corrigió hacia proyectos Spring Boot independientes que heredan de `spring-boot-starter-parent:3.3.4`, instalando `fulvo-common-lib` en el `.m2` local para alinearse de forma exacta a cómo la cátedra enseña los microservicios en los Apuntes 03 y 19.
* **Estrategia de Pruebas:** Se habían contemplado inicialmente suites de tests unitarios y MockMvc en Java. Se eliminaron por completo al contrastar que la cátedra evalúa la funcionalidad a través del Criterio API-01 (Postman) y que el empaquetado oficial del cuestionario utiliza `mvn clean package -DskipTests`, optimizando los tiempos para el plazo de 3 días.
* **Roles de Equipo:** Se removieron las asignaciones individuales por persona para que el plan sea neutral y colaborativo.

#### 3. ¿En qué difiere con lo establecido en el Plan Original?
* **Dominio y Paquetes:** Se acordó mantener `com.fulvo.*` y `groupId: com.fulvo` para preservar la identidad comercial del producto acordada en el grupo.
* **Contingencia Offline:** Se acordó incorporar formalmente el servicio `wiremock` en `docker-compose.yml` para asegurar la respuesta de Nominatim y Open-Meteo durante la defensa en la facultad sin depender de internet.
* **Brackets de Llaves:** Se formalizó que los partidos de fases avanzadas se inicialicen con equipos contrincantes en `null` y se resuelvan dinámicamente con las actas arbitrales previas.

#### 4. Puntos Clave para la Defensa Oral (Coloquio con Docentes)
* **Desacoplamiento de Persistencia:** Justificar por qué `esquema_torneos` y `esquema_operaciones` residen en la misma base de datos física pero sin Foreign Keys cruzadas (autonomía de despliegue y modelo de datos).
* **Seguridad y Gateway:** Explicar cómo el API Gateway actúa como primer filtro perimetral validando el token criptográfico emitido por Keycloak antes de enrutar a la red interna Docker.

---

### [AUDIT-20261106-01] Asignación Equitativa de Responsabilidades y Cohesión Funcional

- **Fecha y Hora de Cierre:** 2026-11-06 01:00
- **Estado:** COMPLETADO
- **Gate de Etapa Asociado:** Gate 0 (Planificación y Organización) [SUPERADO]

#### 1. ¿Qué y Cómo se hizo?
* Se formalizó la división de tareas entre los 4 integrantes del equipo (Santiago Gonzalez, Gastón Cambos Berrondo, Tomas Quiróz y Luis Miguel Urviola) asumiendo igual nivel técnico de backend en Java.
* Se agruparon las responsabilidades en 4 ejes funcionales autónomos y altamente correlacionados:
  1. **Santiago Gonzalez:** Perímetro de Acceso, Infraestructura base, Keycloak OIDC, API Gateway y agregación Swagger.
  2. **Gastón Cambos Berrondo:** Dominio Core de Torneos (`servicio-torneos`), admisiones, validación de buena fe (15-22), algoritmo determinístico de fixtures y 5 reemplazos.
  3. **Tomas Quiróz:** Dominio Core de Operaciones (`servicio-operaciones-partidos`), gestión de sedes, árbitros, anti-superposición horaria, actas arbitrales por dorsal, penales y tabla de posiciones.
  4. **Luis Miguel Urviola:** Contratos comunes (`fulvo-common-lib`), adaptadores externos (`geocodificacion`, `clima`, `notificaciones`), WireMock, Scheduler preventivo 24h y suite Postman automatizada.
* Se actualizaron `PLAN_DESARROLLO.md` y `TASKS.md` asociando cada tarea a su responsable correspondiente.

#### 2. ¿Qué hubo que arreglar sobre la marcha?
* Se revisó que la carga de trabajo no estuviera desbalanceada: cada integrante es dueño de un componente ejecutable completo con su correspondiente flujo de prueba manual post-implementación que no depende de que otro compañero termine código a medias.

#### 3. ¿En qué difiere con lo establecido en el Plan Original?
* Inicialmente se había planteado una versión con roles jerárquicos (Tech Lead vs implementadores) y posteriormente una versión anónima sin responsables. Esta nueva versión establece una distribución horizontal, equitativa y orientada a vertical slices funcionales.

#### 4. Puntos Clave para la Defensa Oral (Coloquio con Docentes)
* **Demostración Individual y Colectiva:** Cada integrante puede explicar y demostrar en vivo su flujo específico con datos reales sin interferir con las otras partes: Santiago muestra el Gateway y Keycloak; Gastón muestra la inscripción y el fixture; Tomas muestra la asignación, acta y tabla de posiciones; Luis Miguel muestra los adaptadores, Mailpit y la suite completa de Postman.

---

### [AUDIT-20261108-02] Protocolo de Gestión de Código: Ramas Locales por Agente y Pull Requests Manuales por el Dev

- **Fecha y Hora de Cierre:** 2026-11-08 17:48 (Hora Local)
- **Estado:** COMPLETADO
- **Gate de Etapa Asociado:** Gate 0 (Planificación, Convenciones y Flujo de Trabajo) [SUPERADO]

#### 1. ¿Qué y Cómo se hizo?
* Se incorporó de manera formal a la documentación rectora ([CONTEXT.md](CONTEXT.md), [PLAN_DESARROLLO.md](PLAN_DESARROLLO.md) y [TASKS.md](TASKS.md)) el flujo de trabajo colaborativo:
  1. El agente de IA **DEBE crear y cambiarse a la rama local dedicada** (`git checkout -b feature/<etapa>-<subseccion>-<descripcion-corta>`) antes de escribir o modificar código.
  2. Los agentes tienen **prohibido pushear a GitHub (`git push`)**.
  3. El desarrollador humano es quien, de manera manual, revisa los cambios locales, ejecuta el `git push -u origin feature/...` y abre la **Pull Request (PR)** en GitHub hacia `main`.
* Se trasladó la totalidad de los documentos de especificación para agentes a la carpeta `docs/agentes/` manteniendo la raíz limpia con un `README.md` orientativo.

#### 2. ¿Qué hubo que arreglar sobre la marcha?
* Se sincronizó el ciclo de vida de ramas transversalmente en todos los documentos para asegurar que la integración a `main` se produzca siempre mediante revisión de PRs en GitHub realizada por los desarrolladores.

#### 3. ¿En qué difiere con lo establecido en el Plan Original?
* Delimita con total claridad la frontera de responsabilidad: el agente aísla el desarrollo en su rama local, y el desarrollador humano valida y sube a GitHub manualmente.

#### 4. Puntos Clave para la Defensa Oral (Coloquio con Docentes)
* **Trazabilidad y Calidad de Integración:** Cada PR en GitHub sirve como evidencia documentada del trabajo individual de cada integrante, permitiendo a los profesores auditar exactamente qué código aportó cada estudiante para cada funcionalidad.

---

### [AUDIT-20261108-11] Etapa 1 - Subsección 1.1: Servicios de Soporte Contenerizados (Postgres, Keycloak, Mailpit y WireMock)

- **Fecha y Hora de Cierre:** 2026-11-08 17:58 (Hora Local)
- **Estado:** COMPLETADO
- **Gate de Etapa Asociado:** Gate 1 [PENDIENTE - Restan Subsecciones 1.2 a 1.5]

#### 1. ¿Qué y Cómo se hizo?
* **Componentes / Módulos intervenidos:** Infraestructura Docker en raíz (`docker-compose.yml`, `initdb/`, `keycloak/`, `wiremock/`).
* **Archivos creados o modificados:**
  - `docker-compose.yml`: Orquestación de `postgres-db`, `keycloak-server`, `mailpit` y `wiremock` sobre la red interna `bda-net`.
  - `.env.example`: Plantilla de puerto host `${DB_PORT_HOST:-5432}` para evitar conflictos si 5432 estuviera ocupado localmente.
  - `.gitignore`: Reglas para ignorar `.env`, artefactos `target/`, temporales y metadatos de IDEs.
  - `initdb/init-schemas.sql`: Script DDL ejecutado al inicializar Postgres creando los esquemas lógicos `esquema_torneos` y `esquema_operaciones`.
  - `keycloak/realm-export.json`: Exportación de Realm `fulvo-realm`, cliente público `fulvo-backend`, roles `ROL_ORGANIZADOR`, `ROL_DELEGADO`, `ROL_ARBITRO`, `ROL_FANATICO` y usuarios de prueba (`admin1`, `delegado1`, `arbitro1`, `fanatico1` con clave `admin123`).
  - `wiremock/mappings/nominatim-search.json`: Stub mock para emular búsquedas satelitales en Nominatim OpenStreetMap (`GET /search.*`).
  - `wiremock/mappings/open-meteo-forecast.json`: Stub mock para emular consultas meteorológicas en Open-Meteo (`GET /v1/forecast.*`).
* **Endpoints y Contratos verificados:**
  - `POST http://localhost:8090/realms/fulvo-realm/protocol/openid-connect/token`: Emisión de JWT Bearer con claims de rol verificados para los 4 perfiles.
  - `GET http://localhost:8090/realms/fulvo-realm/.well-known/openid-configuration`: Autodescubrimiento OIDC (HTTP 200).
  - `GET http://localhost:8025`: Interfaz Web de Mailpit accesible (HTTP 200).
  - `GET http://localhost:8086/__admin/mappings`: Catálogo de 2 stubs activos en WireMock (HTTP 200).
  - PostgreSQL 16: Listado de esquemas `\dn` certificando la coexistencia desacoplada de `esquema_torneos` y `esquema_operaciones`.

#### 2. ¿Qué hubo que arreglar sobre la marcha?
* **Ajuste en Keycloak 26:** Se configuró el comando `start-dev --import-realm` montando el JSON en `/opt/keycloak/data/import/realm-export.json:ro`, lo que asegura la ingesta inmediata de usuarios y roles sin demoras ni scripts bash externos.
* **Control de Puerto en Host:** Se comprobó que el puerto 5432 se encontraba libre en el host, dejando parametrizado `${DB_PORT_HOST:-5432}` para máxima portabilidad del equipo.

#### 3. ¿En qué difiere con lo establecido en el Plan Original?
* No hubo desviaciones. Se cumplieron al 100% las especificaciones de `PLAN_DESARROLLO.md` y los criterios de aceptación de `TASKS.md`.

#### 4. Puntos Clave para la Defensa Oral (Coloquio con Docentes)
* **Desacoplamiento de Esquemas:** Demostrar cómo PostgreSQL arranca con dos esquemas completamente limpios sin Foreign Keys cruzadas, asegurando la independencia lógica de los microservicios de Torneos y Operaciones.
* **Seguridad Centralizada con Keycloak:** Demostrar cómo Keycloak 26 gestiona la autenticación mediante tokens JWT firmados, inyectando los roles de negocio (`ROL_*`) dentro del claim `realm_access.roles`, que luego serán consumidos por el filtro reactivo del API Gateway.
* **Contingencia Offline con WireMock:** Explicar cómo la presencia de WireMock en Docker garantiza que la defensa del proyecto no dependa de internet ni de cuotas de APIs públicas de terceros.


---

### [AUDIT-20261108-12] Etapa 1 - Subsección 1.2: Librería Compartida `fulvo-common-lib` (Instalación en `.m2`)

- **Fecha y Hora de Cierre:** 2026-11-08 18:35 (Hora Local)
- **Estado:** COMPLETADO
- **Gate de Etapa Asociado:** Gate 1 [PENDIENTE - Restan Subsecciones 1.3 a 1.5]

#### 1. ¿Qué y Cómo se hizo?
* **Componentes / Módulos intervenidos:** Librería compartida `fulvo-common-lib`.
* **Archivos creados o modificados:**
  - `fulvo-common-lib/pom.xml`: Configuración Maven (Java 21, Spring Boot Starter Web 3.3.4, Validation, Lombok, SLF4J), sin empaquetado repackage ejecutable para garantizar exportación como dependencia `.jar` canónica.
  - `fulvo-common-lib/src/main/java/com/fulvo/common/dto/CrucePartidoDTO.java`: Contrato DTO serializable para despacho en lote de partidos entre Torneos y Operaciones (`POST /v1/partidos/lote`).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/dto/NotificacionEmailDTO.java`: Contrato DTO serializable para eventos transaccionales hacia servicio-notificaciones (`POST /v1/notificaciones/emails`).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/dto/GeocodificacionDTO.java`: Contrato DTO para resolución geográfica y fallback manual de sedes (`POST /v1/geocodificaciones`).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/dto/ClimaPronosticoDTO.java`: Contrato DTO para evaluación climática y detección de condiciones adversas severas (>40mm / tormentas).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/dto/ActuacionJugadorDTO.java`: Contrato DTO para actas arbitrales (goles y tarjetas por dorsal).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/dto/ActaPartidoDTO.java`: Contrato DTO integral para asentamiento oficial de partidos y definición por penales.
  - `fulvo-common-lib/src/main/java/com/fulvo/common/exception/BusinessRuleException.java`: Excepción base de negocio (código de regla opcional).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/exception/RecursoNoEncontradoException.java`: Excepción para recursos inexistentes (404 Not Found).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/exception/ConflictoEstadoException.java`: Excepción para transiciones inválidas y colisiones de estado (409 Conflict).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/exception/ArbitroNoAsignadoException.java`: Excepción para intentos de modificación arbitral no autorizados (403 Forbidden).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/exception/ConflictoHorarioException.java`: Excepción para superposición horaria ±2h (409 Conflict).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/exception/EmpateNoPermitidoException.java`: Excepción para omisión de penales en Llaves (400 Bad Request).
  - `fulvo-common-lib/src/main/java/com/fulvo/common/filter/CorrelationIdFilter.java`: Filtro `OncePerRequestFilter` con cabecera `X-Correlation-ID`, población en `MDC` (`correlationId`) y propagación a cabeceras de respuesta HTTP.
  - `fulvo-common-lib/src/main/java/com/fulvo/common/exception/GlobalExceptionHandler.java`: Manejador `@RestControllerAdvice` global que transforma todas las excepciones de dominio y validación en `ProblemDetail` (RFC 7807) enriquecidos con timestamp, correlación y URI.
  - `fulvo-common-lib/src/main/java/com/fulvo/common/config/FulvoCommonAutoConfiguration.java`: Configuración `@AutoConfiguration` para habilitar automáticamente los beans en cualquier microservicio Spring Boot consumidor.
  - `fulvo-common-lib/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`: Declaración de autoconfiguración de Spring Boot 3.
* **Endpoints y Contratos implementados:**
  - Contratos DTO estándar para comunicación inter-servicio vía `RestClient`.
  - Respuestas RFC 7807 (`ProblemDetail`) con HTTP 400, 403, 404, 409 y 500.
* **Lógica técnica y de dominio aplicada:**
  - Compilación Java 21 y empaquetado JAR instalado en el repositorio local `~/.m2/repository/com/fulvo/fulvo-common-lib/1.0.0-SNAPSHOT/`.
  - Inyección de MDC `[%X{correlationId}]` y limpieza obligatoria en `finally` para evitar fugas en pools de hilos de Tomcat.

#### 2. ¿Qué hubo que arreglar sobre la marcha?
* **Aislamiento de Empaquetado como Librería:** Se omitió intencionalmente la meta `repackage` del `spring-boot-maven-plugin` para que el archivo generado sea un JAR estándar consumible en el classpath de otros proyectos en lugar de un fat-JAR ejecutable.
* **Autodescubrimiento en Spring Boot 3:** Se añadió `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` para que los microservicios core y adaptadores importen automáticamente el `CorrelationIdFilter` y el `GlobalExceptionHandler` sin necesidad de forzar un `@ComponentScan("com.fulvo")` manual.

#### 3. ¿En qué difiere con lo establecido en el Plan Original?
* Se incorporaron proactivamente las excepciones arbitrales (`ArbitroNoAsignadoException`, `ConflictoHorarioException`, `EmpateNoPermitidoException`) y los DTOs de actas (`ActaPartidoDTO`, `ActuacionJugadorDTO`) previstos para la Subsección 3.6, dejándolos listos en `.m2` para que Gastón y Santiago puedan consumirlos inmediatamente sin reprocesar la librería.

#### 4. Puntos Clave para la Defensa Oral (Coloquio con Docentes)
* **Estandarización de Errores con RFC 7807:** Justificar por qué se utiliza `ProblemDetail` de Spring 6 en lugar de DTOs propios ad-hoc para respuestas de error (cumplimiento de estándares de la industria, interoperabilidad y uniformidad REST).
* **Trazabilidad Distribuida con Correlation ID y MDC:** Explicar cómo el `CorrelationIdFilter` extrae o genera el `X-Correlation-ID`, lo inyecta en el `MDC` de SLF4J permitiendo que cada línea de log incluya `[%X{correlationId}]`, y lo propaga en la respuesta para facilitar el debugging en arquitecturas distribuidas.
* **Autonomía y Desacoplamiento de Librería Común:** Justificar por qué la librería solo comparte DTOs de transferencia y excepciones agnósticas de persistencia, prohibiendo taxativamente compartir entidades JPA entre microservicios.


---

### [AUDIT-20261108-13] Etapa 1 - Actualización de Ecosistema: Spring Boot 4.0.8 y Lombok 1.18.38 en `subseccion1.2`

- **Fecha y Hora de Cierre:** 2026-11-08 19:15 (Hora Local)
- **Estado:** COMPLETADO
- **Gate de Etapa Asociado:** Gate 1 [PENDIENTE - Restan Subsecciones 1.3 a 1.5]

#### 1. ¿Qué y Cómo se hizo?
* **Componentes / Módulos intervenidos:** `fulvo-common-lib` y documentación de agentes en `docs/agentes/`.
* **Archivos creados o modificados:**
  - `fulvo-common-lib/pom.xml`: Se actualizó el `parent` a `org.springframework.boot:spring-boot-starter-parent:4.0.8` y se fijó `<lombok.version>1.18.38</lombok.version>`.
  - `docs/agentes/CONTEXT.md`: Se actualizó el stack tecnológico a Spring Boot 4.0.8 en la rama `subseccion1.2`.
  - `docs/agentes/PLAN_DESARROLLO.md`: Se actualizó la referencia del starter parent a Spring Boot 4.0.8.
  - `docs/agentes/TASKS.md`: Se actualizaron las tareas de inicialización (1.3.1, 1.4.1, 1.5.1) a Spring Boot 4.0.8.
  - `docs/agentes/DISENO_SISTEMA_FULVO.md`: Se actualizó la referencia del diagrama de contenedores a Spring Boot 4.0.8.
  - `docs/agentes/GUIA_PRUEBAS_MANUALES.md`: Se actualizó la referencia de proyectos a Spring Boot 4.0.8.
* **Lógica técnica y de dominio aplicada:**
  - Empaquetado e instalación en `.m2` (`mvn clean install -DskipTests`) de `fulvo-common-lib:1.0.0-SNAPSHOT` compilando sobre Spring Boot 4.0.8 y Java 21 LTS con `BUILD SUCCESS`.

#### 2. ¿Qué hubo que arreglar sobre la marcha?
* **Incompatibilidad Lombok con JDK 21.0.11 (`TypeTag :: UNKNOWN`):** Al compilar con Oracle JDK 21.0.11, la versión anterior de Lombok arrojaba `ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN` por cambios internos en `javac`. Se resolvió explícitamente configurando `<lombok.version>1.18.38</lombok.version>` en las propiedades del compilador.
* **Actualización a Spring Boot 4.0.8:** Se adoptó Spring Boot 4.0.8 en esta rama, resolviendo todas las dependencias transitivas desde Maven Central de forma limpia.

#### 3. ¿En qué difiere con lo establecido en el Plan Original?
* **Versión del Framework:** El plan original contemplaba Spring Boot 3.3.4. Se adaptó la rama a Spring Boot 4.0.8 y se dejó constancia formal en todos los artefactos de `docs/agentes/`.

#### 4. Puntos Clave para la Defensa Oral (Coloquio con Docentes)
* **Compatibilidad de Framework y JDK:** Explicar cómo la combinación de JDK 21 moderno requirió sincronizar la versión de Lombok y la transición hacia Spring Boot 4.0.8 asegurando que los módulos downstream consuman el JAR actualizado desde `.m2`.
