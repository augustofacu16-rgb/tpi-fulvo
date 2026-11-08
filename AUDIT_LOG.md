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

