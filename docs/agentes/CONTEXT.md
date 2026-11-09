# Protocolo de Contexto y Directivas de Ejecución para Agentes de IA y Desarrolladores

> **LEER OBLIGATORIAMENTE ANTES DE ESCRIBIR O MODIFICAR CUALQUIER LÍNEA DE CÓDIGO**  
> Este documento establece el flujo de trabajo, las restricciones arquitectónicas y el protocolo estricto que cualquier Agente de IA o desarrollador debe seguir en este repositorio.

---

## 1. Fase 0: Inspección Previa Obligatoria

Antes de comenzar a implementar cualquier tarea o subsección, el agente/desarrollador **debe ejecutar en orden los siguientes pasos de lectura**:

1. **Revisar el Estado Actual de Tareas (`TASKS.md`):**
   * Inspeccionar qué subsección y etapa están en estado `[PENDIENTE]` o `[EN PROCESO]`.
   * Identificar exactamente cuál es la **próxima subsección a implementar** y cuáles son sus criterios de aceptación.
   * **NUNCA saltar a una etapa posterior** si la compuerta de calidad (*Gate*) de la etapa previa no está superada y marcada con `[X]`.
2. **Consultar el Diseño Integral del Sistema (`DISENO_SISTEMA_FULVO.md`):**
   * Consultar la especificación de dominio oficial del sistema (transcripción canónica del cuestionario aprobado por la cátedra `Documentacion COMPLETA CUESTIONARIO BACKEND TP FULVO.pdf`).
   * Revisar las Reglas de Negocio Oficiales (`RN-01` a `RN-12`), las funcionalidades (`FN-01` a `FN-14`), el catálogo de endpoints canónicos, las exclusiones del MVP y los diagramas Mermaid C4 (Contexto y Contenedores), DER y máquinas de estado antes de escribir código.
3. **Consultar la Especificación del Plan (`PLAN_DESARROLLO.md`):**
   * Leer en detalle la subsección asignada para conocer las entidades, campos, rutas de endpoints, reglas de negocio y flujos HTTP comprometidos.
4. **Consultar las Bitácoras de Auditoría (`docs/audit/`):**
   * Leer las entradas registradas en `docs/audit/` para entender qué decisiones técnicas, arreglos sobre la marcha o modificaciones del dominio ocurrieron en las etapas anteriores.
5. **Consultar la Documentación de Referencia (`docs/`):**
   * En caso de dudas sobre el modelo de datos o diagramas de estados, verificar `docs/DER-Fulvo.pdf`, `docs/TPI-MEPartido.drawio.pdf`, etc.
6. **Consultar la Guía de Pruebas Manuales (`GUIA_PRUEBAS_MANUALES.md`):**
   * Revisar el guion de prueba manual asignado a la subsección para conocer de antemano los comandos `curl`, payloads JSON, precondiciones y códigos HTTP esperados.
7. **Crear y Cambiarse a la Rama de Trabajo Dedicada en Git (`feature/...`):**
   * **Obligación del Agente antes de codear:** Antes de modificar o crear cualquier archivo para resolver una tarea, el agente **DEBE crear la rama local correspondiente y cambiarse a esa misma rama** (`git checkout -b feature/<etapa>-<subseccion>-<descripcion-corta>`). Si la rama ya fue creada previamente, debe cambiarse a ella (`git checkout feature/...`).
   * **PROHIBIDO desarrollar o tocar código directamente sobre la rama `main`**.
   * **PROHIBIDO PUSHEAR A GITHUB MEDIANTE AGENTES:** Los agentes de IA tienen **estrictamente prohibido ejecutar `git push` o interactuar con el repositorio remoto**. La subida a GitHub y la creación del Pull Request la realiza **exclusivamente el desarrollador humano a mano**.

---

## 2. Reglas de Oro de Arquitectura y Convenciones (Innegociables)

Cualquier código generado en este proyecto debe respetar los siguientes principios canónicos de la cátedra **Backend de Aplicaciones (UTN-FRC)**:

1. **Stack Tecnológico:**
   * Java 21 LTS.
   * Spring Boot 4.0.8 (fijado en rama `subseccion1.2`).
   * Spring Cloud 2023.0.x (para el API Gateway).
   * PostgreSQL 16.
   * Keycloak 26.5.0 (OIDC / OAuth2 Resource Server).
2. **Organización de Proyectos y Maven:**
   * `groupId`: `com.fulvo`
   * Raíz de paquetes Java: `com.fulvo.*` (ej. `com.fulvo.torneos`, `com.fulvo.operaciones`, `com.fulvo.common`, etc.).
   * **Estructura Desacoplada:** Cada microservicio es un proyecto Spring Boot independiente que hereda directamente de `org.springframework.boot:spring-boot-starter-parent:4.0.8`.
   * La librería compartida `fulvo-common-lib` es un artefacto `.jar` que se compila e instala en el repositorio local `.m2` (`mvn clean install -DskipTests`). Los microservicios la importan como dependencia estándar.
   * Compilación ágil sin pruebas de Java: `mvn clean package -DskipTests`.
3. **Persistencia Desacoplada (PostgreSQL):**
   * Dos esquemas lógicos independientes: `esquema_torneos` (administrado por `servicio-torneos`) y `esquema_operaciones` (administrado por `servicio-operaciones-partidos`).
   * **PROHIBIDAS las claves foráneas físicas cruzadas** entre esquemas. La vinculación entre microservicios se hace exclusivamente mediante identificadores lógicos (`torneo_id` de tipo `Long`, `arbitro_dni` de tipo `String`, etc.).
   * En tablas de personas (`jugador`, `arbitro`), usar clave primaria natural `dni` de tipo `String`.
4. **Inyección de Dependencias y Buenas Prácticas:**
   * **PROHIBIDO `@Autowired` en atributos directos.** Usar siempre inyección por constructor asistida por `@RequiredArgsConstructor` de Lombok con atributos `private final`.
5. **Comunicación Inter-Servicio:**
   * Usar exclusivamente `RestClient` fluido (Spring 6 / Boot 3). **PROHIBIDO el uso de `RestTemplate`**.
   * Encapsular llamadas HTTP en clases cliente anotadas con `@Component` (ej. `OperacionesApiClient`, `GeocodificacionApiClient`).
   * Intercambiar exclusivamente DTOs mínimos de integración (ubicados en `fulvo-common-lib`), nunca entidades JPA.
6. **Manejo de Errores y RFC 7807:**
   * Centralizado mediante `@RestControllerAdvice` retornando objetos estándar **RFC 7807 (`ProblemDetail`)**.
   * **PROHIBIDO el *swallowing* de excepciones** (bloques `catch` vacíos o con solo `printStacktrace`). Preservar siempre la causa original (`new MiExcepcion(mensaje, e)`).
7. **Trazabilidad y Observabilidad:**
   * Filtro `CorrelationIdFilter` (`OncePerRequestFilter`) que propague la cabecera `X-Correlation-ID` en las peticiones y la cargue en el `MDC` de SLF4J.
   * Todo mensaje de log emitido por Logback debe incluir `[%X{correlationId}]`.
8. **Estrategia de Pruebas (Criterio API-01):**
   * **NO se implementan tests unitarios ni MockMvc en código Java** (ahorro crítico para el plazo de 3 días).
   * El 100% de la verificación funcional y validación de reglas de negocio se realiza a través de la **Colección Automatizada de Postman** con scripts `pm.test()`, levantando el entorno con Docker Compose.
9. **Aislamiento Perimetral en Docker:**
   * En `docker-compose.yml`, los microservicios de negocio operan en la red interna privada `bda-net` sin exponer puertos directos al exterior. Únicamente exponen puertos públicos:
     * `api-gateway`: `8080`
     * `keycloak-server`: `8090`
     * `mailpit` (Web UI): `8025`
     * `wiremock`: `8086`
10. **Resiliencia ante Conflictos de Puerto en PostgreSQL (Puerto 5432 Ocupado):**
    * En `docker-compose.yml`, el puerto hacia el host se parametriza como `${DB_PORT_HOST:-5432}:5432`.
    * Si un dev ya tiene un PostgreSQL instalado localmente en su máquina, no necesita desinstalarlo ni detenerlo forzosamente: basta con crear un archivo `.env` en la raíz (basado en `.env.example`) configurando `DB_PORT_HOST=5433` (o 5434).
    * La comunicación interna entre contenedores en la red `bda-net` permanece inalterada en `postgres-db:5432`.
    * Para ejecutar microservicios desde el IDE fuera de Docker, los `application.yml` leen `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/fulvo_db`, permitiendo pasar `-DDB_PORT=5433` sin tocar el código fuente.
11. **Estrategia de Ramas en Git y Prohibición de Push Remoto mediante Agentes:**
    * **Prohibido desarrollar directamente sobre `main`:** La rama `main` refleja únicamente código integrado, probado y certificado con compuertas de calidad superadas.
    * **Una rama local por tarea / subsección (Obligación del Agente):** Antes de comenzar a escribir o modificar código para cualquier tarea o subsección, el agente **DEBE crear la rama local dedicada y cambiarse a esa misma rama** (`git checkout -b feature/<etapa>-<subseccion>-<descripcion-corta>`). Todo el desarrollo de la subsección debe quedar aislado en esa rama.
    * **PROHIBICIÓN ESTRICTA DE PUSH MEDIANTE AGENTES:** Ningún agente de IA debe ejecutar `git push`, interactuar con credenciales remotas, ni abrir Pull Requests en GitHub.
    * **Subida y Pull Request 100% Manual por el Desarrollador:** Una vez que el agente finalice la codificación en la rama local, valide los flujos según `GUIA_PRUEBAS_MANUALES.md`, marque `TASKS.md` y registre la auditoría creando su fragmento nuevo en `docs/audit/<etapa>.<subseccion>-<descripcion>.md`, el agente se detiene e informa al desarrollador. Es el desarrollador quien realiza el `git push -u origin feature/...` y abre la Pull Request (PR) a mano en GitHub hacia `main`.
    * **Ciclo de vida de la rama:**
      1. El agente verifica/sincroniza `main` local.
      2. El agente crea la rama local y se cambia a ella: `git checkout -b feature/<etapa>-<subseccion>-<descripcion-corta>`.
      3. El agente implementa la subsección en dicha rama.
      4. El agente valida mediante `GUIA_PRUEBAS_MANUALES.md`.
      5. El agente actualiza `TASKS.md` y crea un **nuevo archivo de fragmento** en `docs/audit/<etapa>.<subseccion>-<descripcion>.md` (evitando cualquier conflicto en Git).
      6. **El desarrollador a mano:** Revisa los cambios, ejecuta `git push -u origin feature/...`, crea la Pull Request en GitHub hacia `main`, la revisa y mergea.
      7. Se elimina la rama una vez integrada.

---

## 3. Protocolo de Consulta Obligatoria ante Dudas o Ambigüedad (Anti-Suposición)

> **Regla Inflexible:** Cuando un Agente de IA tenga dudas sobre qué hacer, cómo modelar una regla no explícita, o se enfrente a cualquier bifurcación de diseño, **ESTÁ ESTRICTAMENTE PROHIBIDO ASUMIR, ADIVINAR O IMPROVISAR UNA SOLUCIÓN**. Debe pausar inmediatamente y consultar al desarrollador antes de escribir o modificar código.

### 3.1 Cuándo es Obligatorio Consultar al Desarrollador:
1. **Ambigüedades en Reglas de Negocio:** Cuando la especificación del dominio deje margen a interpretación (ej. criterios secundarios de desempate en posiciones, tiempos de prórroga antes de penales, validaciones adicionales de fechas).
2. **Dilemas de Diseño o Alternativas Técnicas:** Cuando existan dos o más enfoques viables según los apuntes de la cátedra (ej. estrategias de mapeo de DTOs, manejo de eventos de dominio o granularidad de excepciones).
3. **Casos de Borde No Contemplados:** Cuando al codificar se identifique un escenario no previsto en el `PLAN_DESARROLLO.md`, en `DISENO_SISTEMA_FULVO.md` o en los diagramas de `docs/`.
4. **Modificaciones del Modelo de Datos:** Si surge la necesidad de agregar un campo, tabla o relación que no figuraba en el diseño original.

### 3.2 Formato Estructurado de la Consulta al Desarrollador:
El agente no debe hacer preguntas vagas. Debe formular su consulta con rigor técnico siguiendo esta estructura:
* **1. Duda Puntual y Contexto:** Explicar con precisión el archivo, entidad, endpoint o regla donde surge la duda.
* **2. Opciones Viables con Pros y Contras:** Presentar 2 o 3 alternativas claras, fundamentadas en las buenas prácticas de Java 21 / Spring Boot 3 y los apuntes de Backend de Aplicaciones.
* **3. Recomendación del Agente:** Proponer cuál considera la mejor opción y la justificación técnica de por qué elegirla.
* **4. Esperar Respuesta:** Detener la edición de código hasta que el desarrollador valide o corrija la dirección a seguir.
* **5. Registro de Trazabilidad:** La decisión acordada con el desarrollador se registrará obligatoriamente en el archivo de auditoría correspondiente dentro de `docs/audit/` (en la sección *Qué se arregló sobre la marcha* o *Diferencias con el plan original*).

---

## 4. Protocolo de Cierre Obligatorio por Subsección y Etapa

Una vez finalizada la codificación de una subsección o etapa completa, el agente/desarrollador **DEBE ejecutar obligatoriamente las siguientes cuatro acciones antes de dar la tarea por concluida**:

### Acción 1: Validación y Prueba Manual
* Ejecutar las pruebas manuales correspondientes detalladas en `GUIA_PRUEBAS_MANUALES.md` (levantamiento, `curl`, base de datos, Mailpit, Keycloak o WireMock).
* Comprobar que los códigos HTTP (200, 201, 202, 400, 401, 403, 409, 422) y cuerpos de respuesta coincidan con los criterios establecidos.

### Acción 2: Actualizar `TASKS.md`
* Marcar con `[X]` las tareas específicas completadas.
* Si se completaron todas las subsecciones de una etapa y se validó su compuerta de calidad, marcar `Gate Superado` con `[X]` y actualizar la tabla resumen superior.

### Acción 3: Registrar Auditoría en Fragmento Modular (`docs/audit/`)
* Crear un **nuevo archivo independiente** en `docs/audit/<etapa>.<subseccion>-<descripcion-corta>.md` (ej. `docs/audit/2.1-entidades-torneos.md`) copiando la estructura de `docs/audit/_TEMPLATE.md` (garantizando 0% de conflictos en Git).
* Registrar con honestidad técnica:
  1. **Qué y cómo se hizo** (archivos, clases, endpoints, lógica).
  2. **Qué se arregló sobre la marcha** (errores de compilación, choques de puertos, ajustes de configuración, dependencias faltantes).
  3. **En qué difiere del plan original** (ajustes al modelo de datos, cambios de nombres, simplificaciones o reglas adaptadas).
  4. **Puntos clave para la defensa oral** (argumentos y decisiones que el equipo debe justificar ante los profesores).
* Cada fragmento en `docs/audit/` es autosuficiente y constituye el registro oficial de la subsección para consulta y defensa oral.

### Acción 4: Entrega Local al Desarrollador para Push y PR Manual
* El agente verifica que todos los cambios implementados se encuentren limpios y validados dentro de la rama local `feature/...`.
* **EL AGENTE NO EJECUTA `git push` NI CREA PRs EN GITHUB.**
* El agente notifica al desarrollador humano que la subsección o tarea está lista en su rama local, detallando el resumen técnico y los criterios de aceptación cumplidos.
* **El desarrollador a mano:** Revisa los archivos, realiza el `git push -u origin feature/...`, crea la **Pull Request (PR)** en GitHub hacia `main`, y tras la revisión y aprobación, mergea la PR y elimina la rama.

---

## 5. Instrucción de Arranque

Cuando se solicite continuar o comenzar una subsección, el agente debe responder confirmando que leyó el estado en `TASKS.md` y las auditorías en `docs/audit/` y proceder quirúrgicamente con las tareas asignadas.
