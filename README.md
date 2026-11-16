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

### 2. Configuración de Variables de Entorno
Copiar la plantilla de variables de entorno (ajustando `DB_PORT_HOST` o `GATEWAY_PORT_HOST` si fuera necesario):
```bash
cp .env.example .env
```

### 3. Compilar e Instalar la Librería Común y Microservicios
Antes de construir las imágenes Docker, compilar e instalar `fulvo-common-lib` en el repositorio local `.m2` y empaquetar los 6 microservicios:
```bash
# Compilar e instalar librería común
cd fulvo-common-lib
mvn clean install -DskipTests
cd ..

# Empaquetar los 6 microservicios Java
mvn clean package -DskipTests
```

### 4. Despliegue Integral Contenerizado con Docker Compose
Levantar los 10 servicios (PostgreSQL, Keycloak, Mailpit, WireMock y los 6 microservicios) en la red perimetral cerrada `bda-net`:
```bash
docker compose up -d --build
```
> **Aislamiento Perimetral Estricto:** Únicamente el `api-gateway` expone el puerto `8080` (junto con Keycloak `8090`, Mailpit `8025` y WireMock `8086`). Los microservicios de negocio (`servicio-torneos`, `servicio-operaciones-partidos`, etc.) operan de forma aislada en `bda-net` sin mapeo directo de puertos hacia el host.

---

## 📚 Documentación Técnica

Toda la documentación técnica, diagramas de arquitectura y especificaciones se encuentran organizadas en la carpeta `docs/`:

* **Diagramas C4 de Arquitectura:**
  * **[docs/TPI-Diagrama de contexto.pdf](docs/TPI-Diagrama%20de%20contexto.pdf):** Diagrama de contexto del sistema distribuido.
  * **[docs/TPI-Diagrama de contenedores.pdf](docs/TPI-Diagrama%20de%20contenedores.pdf):** Diagrama de contenedores y servicios interconectados.
* **Modelo de Datos:**
  * **[docs/DER-Fulvo.pdf](docs/DER-Fulvo.pdf):** Diagrama Entidad-Relación (DER) de esquemas relacionales.
* **Máquinas de Estado:**
  * **[docs/TPI-METorneo.drawio.pdf](docs/TPI-METorneo.drawio.pdf):** Ciclo de vida y transiciones del Torneo.
  * **[docs/TPI-MEPartido.drawio.pdf](docs/TPI-MEPartido.drawio.pdf):** Ciclo de vida y transiciones de Partidos.
  * **[docs/TPI-MEInscripcion.drawio.pdf](docs/TPI-MEInscripcion.drawio.pdf):** Flujo de solicitudes de inscripción.
* **Especificación y Cuestionario:**
  * **[docs/Documentacion COMPLETA CUESTIONARIO BACKEND TP FULVO.pdf](docs/Documentacion%20COMPLETA%20CUESTIONARIO%20BACKEND%20TP%20FULVO.pdf):** Documentación funcional y respuestas al cuestionario de la cátedra.

---

## 🌿 Flujo de Trabajo en Git

1. **Ramas de Desarrollo:** Cada funcionalidad se desarrolla en su rama dedicada: `feature/<nombre-feature>`.
2. **Validación Local:** Se verifican los cambios localmente y se ejecutan las suites de pruebas automatizadas.
3. **Revisión y Pull Request:** Se abre la Pull Request en GitHub hacia `main` para su integración y revisión.
