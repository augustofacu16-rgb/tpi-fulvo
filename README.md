# Fulvo' - Plataforma Backend de Gestión de Torneos y Operaciones de Fútbol

> **Trabajo Práctico Integrador (TPI) - Cátedra Backend de Aplicaciones (UTN-FRC)**  
> Sistema distribuido de gestión deportiva, administración de torneos, fixtures determinísticos, operaciones de partidos en tiempo real, actas arbitrales por dorsal, tabla de posiciones y contingencias climáticas automatizadas.

---

## 🏛️ Arquitectura del Sistema

La solución adopta una **arquitectura de microservicios desacoplada** con aislamiento de persistencia lógica en PostgreSQL (esquemas independientes sin claves foráneas físicas cruzadas):

* **`api-gateway` (Puerto 8080):** Gateway perimetral reactivo (Spring Cloud Gateway) con validación de tokens JWT de Keycloak y agregación Swagger OpenAPI.
* **`servicio-torneos` (Puerto 8081):** Dominio de torneos, categorías, equipos, listas de buena fe (15 a 22 jugadores), fixtures determinísticos y control de 5 reemplazos. Opera sobre `esquema_torneos`.
* **`servicio-operaciones-partidos` (Puerto 8082):** Sedes, canchas, árbitros, programación de partidos con anti-superposición horaria (±2h), actas arbitrales por dorsal y tabla de posiciones. Opera sobre `esquema_operaciones`.
* **`fulvo-common-lib`:** Librería de contratos compartidos (DTOs de integración, excepciones de negocio RFC 7807 y filtro de trazabilidad `CorrelationIdFilter` con MDC).
* **Adaptadores de Integración Externa:**
  * `servicio-geocodificacion`: Resolución geográfica de direcciones y fallback manual.
  * `servicio-clima`: Consulta meteorológica y detección preventiva de tormentas severas.
  * `servicio-notificaciones`: Despacho transaccional de correos electrónicos vía SMTP.

---

## 🛠️ Stack Tecnológico e Infraestructura

* **Lenguaje & Framework:** Java 21 LTS + Spring Boot 4.0.8.
* **Persistencia:** PostgreSQL 16 (esquemas `esquema_torneos` y `esquema_operaciones`).
* **Seguridad & IAM:** Keycloak 26.5.0 (OIDC / OAuth2 Resource Server con roles `ROL_ORGANIZADOR`, `ROL_DELEGADO`, `ROL_ARBITRO`, `ROL_FANATICO`).
* **Email Testing:** Mailpit (Web UI en `http://localhost:8025`).
* **Mocks Offline:** WireMock (Puerto 8086) para contingencia sin conexión a internet en exámenes/coloquios.
* **Pruebas y Certificación:** Colección automatizada de Postman (Criterio API-01) con assertions `pm.test()`.

---

## 🚀 Puesta en Marcha Rápida (Entorno Local)

### 1. Requisitos Previos
* JDK 21 LTS instalado y configurado en el `PATH`.
* Maven 3.9+.
* Docker & Docker Compose.

### 2. Infraestructura Base con Docker
Copiar las variables de entorno e inicializar los contenedores de soporte:
```bash
# Configurar variables de entorno (ajustar DB_PORT_HOST si el puerto 5432 estuviera ocupado)
cp .env.example .env

# Levantar PostgreSQL, Keycloak, Mailpit y WireMock
docker compose up -d
```

### 3. Compilar e Instalar la Librería Común
Antes de ejecutar los microservicios, compilar `fulvo-common-lib` para instalarla en el repositorio `.m2` local:
```bash
cd fulvo-common-lib
mvn clean install -DskipTests
cd ..
```

### 4. Compilar los Microservicios
```bash
mvn clean package -DskipTests
```

---

## 📚 Mapa de Documentación y Protocolo de Agentes

Toda la documentación técnica, tableros de avance y especificaciones se encuentran organizadas en `docs/`:

### Documentación de Diseño y Gestión (`docs/agentes/`)
* **[docs/agentes/CONTEXT.md](docs/agentes/CONTEXT.md):** **Directivas obligatorias de ejecución** para Agentes de IA y desarrolladores, reglas innegociables de arquitectura, protocolo anti-suposición y ciclo de vida de ramas.
* **[docs/agentes/TASKS.md](docs/agentes/TASKS.md):** Tablero de control de avance por etapas con compuertas de calidad (*Gates*).
* **[docs/agentes/DISENO_SISTEMA_FULVO.md](docs/agentes/DISENO_SISTEMA_FULVO.md):** Especificación funcional oficial, catálogo de endpoints, DER, diagramas C4 y máquinas de estado.
* **[docs/agentes/PLAN_DESARROLLO.md](docs/agentes/PLAN_DESARROLLO.md):** Planificación sprint a sprint y matriz de responsabilidades por integrante.
* **[docs/agentes/GUIA_PRUEBAS_MANUALES.md](docs/agentes/GUIA_PRUEBAS_MANUALES.md):** Guion de pruebas manuales, comandos `curl` y verificación paso a paso.

### Bitácoras de Auditoría Modulares (`docs/audit/`)
Para evitar conflictos de merge en Git (*Git Hotspots*) durante el desarrollo paralelo de features, la bitácora opera bajo el **Patrón de Fragmentos Modulares**:
* **[docs/audit/](docs/audit/):** Carpeta donde cada tarea/subsección genera y conserva su propio archivo `.md` independiente (ej: `1.4-inicializacion-operaciones.md`).
* **[docs/audit/_TEMPLATE.md](docs/audit/_TEMPLATE.md):** Plantilla estándar obligatoria para cada entrada de auditoría.

---

## 🌿 Flujo de Trabajo en Git

1. **Ramas Locales Aisladas:** Cada tarea se desarrolla en su rama dedicada: `feature/<etapa>-<subseccion>-<slug>`.
2. **Prohibición de Push por Agentes:** Los agentes de IA nunca pushean al repositorio remoto.
3. **Registro Modular:** Al finalizar la tarea, se crea el fragmento en `docs/audit/<etapa>.<subseccion>-<slug>.md` (**cero conflictos de Git**).
4. **Revisión y PR Manual:** El desarrollador humano inspecciona los cambios, ejecuta `git push -u origin feature/...` y abre la Pull Request en GitHub hacia `main`.
