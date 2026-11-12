# Guía Maestra de Pruebas Manuales por Subsección y Tarea - TPI Fulvo'

> **Propósito:** Esta guía proporciona el protocolo técnico detallado para que cada integrante (**Tomas, Gastón, Santiago y Luis Miguel**) pueda probar de forma manual, quirúrgica y visual el código resultante de cada subsección desarrollada, antes de darla por finalizada en `TASKS.md` y registrar su entrada en `docs/audit/`.
> 
> **Modalidades de Prueba Soportadas:**
> 1. **Opción A (Postman GUI):** Peticiones configuradas con pestañas *Params*, *Authorization*, *Headers* y *Body*, ideales para inspección visual de estados y respuestas.
> 2. **Opción B (Navegador Web y Swagger UI):** Acceso directo por URL para endpoints `GET` y consola interactiva de Swagger UI (`http://localhost:8080/swagger-ui.html`) con botón `Authorize` y `Try it out` para peticiones `POST`, `PATCH` y `DELETE`.
> 3. **Opción C (Consola / curl):** Comandos rápidos para terminal PowerShell o Bash.

---

## 0. Parámetros Globales y Configuración de Postman & Navegador

### 0.1 Mapeo de Puertos y Accesos Web

| Servicio / Contenedor | Puerto Host | Acceso Directo por Navegador Web | Descripción |
|---|:---:|---|---|
| **API Gateway** | `8080` | [http://localhost:8080/health-check](http://localhost:8080/health-check) | Punto de entrada perimetral seguro. |
| **Swagger UI Unificado** | `8080` | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Consola interactiva para probar todos los endpoints desde la web. |
| **Keycloak OIDC** | `8090` | [http://localhost:8090/realms/fulvo-realm](http://localhost:8090/realms/fulvo-realm) | Servidor de identidad y emisión de JWT. |
| **Mailpit (Web UI)** | `8025` | [http://localhost:8025](http://localhost:8025) | Bandeja visual para ver los correos transaccionales enviados. |
| **WireMock Admin** | `8086` | [http://localhost:8086/__admin/mappings](http://localhost:8086/__admin/mappings) | Mappings de simulación satelital y climática. |
| **PostgreSQL 16** | `${DB_PORT_HOST:-5432}` | `localhost:${DB_PORT_HOST:-5432}` (vía DBeaver/pgAdmin) | Base de datos (`fulvo_db`). Dinámico vía `.env` si 5432 está ocupado. |

---

### 0.2 Configuración del Entorno en Postman (Postman Environment)

Crear un entorno en Postman llamado **`Fulvo Local Dev`** con las siguientes variables:

| Variable | Initial Value | Current Value | Descripción |
|---|---|---|---|
| `gateway_url` | `http://localhost:8080` | `http://localhost:8080` | URL base del API Gateway. |
| `keycloak_url` | `http://localhost:8090` | `http://localhost:8090` | URL base de Keycloak. |
| `token_organizador` | *(vacío)* | *(se llena con login)* | Token JWT con rol `ROL_ORGANIZADOR` (`admin1`). |
| `token_delegado` | *(vacío)* | *(se llena con login)* | Token JWT con rol `ROL_DELEGADO` (`delegado1`). |
| `token_arbitro` | *(vacío)* | *(se llena con login)* | Token JWT con rol `ROL_ARBITRO` (`arbitro1`). |
| `token_fanatico` | *(vacío)* | *(se llena con login)* | Token JWT con rol `ROL_FANATICO` (`fanatico1`). |

#### Petición en Postman para Auto-Login (Pestaña "Tests")
Para obtener los tokens en Postman sin copiar y pegar manualmente:
* **Método:** `POST`
* **URL:** `{{keycloak_url}}/realms/fulvo-realm/protocol/openid-connect/token`
* **Body (`x-www-form-urlencoded`):**
  * `client_id`: `fulvo-backend`
  * `grant_type`: `password`
  * `username`: `admin1` *(o delegado1, arbitro1, fanatico1)*
  * `password`: `admin123`
* **Pestaña Tests (Script JavaScript):**
  ```javascript
  var res = pm.response.json();
  pm.environment.set("token_organizador", res.access_token);
  console.log("Token de Organizador actualizado en el entorno.");
  ```

---

### 0.3 Script Alternativo en PowerShell (Para Terminal)

```powershell
function Get-FulvoToken([string]$username) {
    $body = @{
        client_id  = "fulvo-backend"
        grant_type = "password"
        username   = $username
        password   = "admin123"
    }
    $response = Invoke-RestMethod -Method Post -Uri "http://localhost:8090/realms/fulvo-realm/protocol/openid-connect/token" -ContentType "application/x-www-form-urlencoded" -Body $body
    return $response.access_token
}

$TOKEN_ADMIN    = Get-FulvoToken "admin1"
$TOKEN_DELEGADO = Get-FulvoToken "delegado1"
$TOKEN_ARBITRO  = Get-FulvoToken "arbitro1"
$TOKEN_FANATICO = Get-FulvoToken "fanatico1"
```

---

### 0.4 Gestión de Conflicto de Puerto 5432 (PostgreSQL Local Preinstalado)

#### Diagnóstico del Conflicto
Si algún integrante del equipo ya tiene instalado PostgreSQL de manera nativa en su sistema operativo, el puerto `5432` estará bloqueado. Al ejecutar `docker compose up`, Docker arrojará el error:
> `Error response from daemon: Ports are not available: exposing port TCP 0.0.0.0:5432 -> 0.0.0.0:0: listen tcp 0.0.0.0:5432: bind: address already in use`

Para comprobar si el puerto 5432 está ocupado antes de levantar Docker, ejecutar en PowerShell:
```powershell
Test-NetConnection -ComputerName localhost -Port 5432
```
Si `TcpTestSucceeded: True`, el puerto ya tiene un servicio escuchando.

#### Solución Dinámica por Desarrollador (Sin Modificar Código ni Romper a los Compañeros)
1. En la raíz del proyecto (`tpi-fulvo/`), crear un archivo **`.env`** (ignorado en `.gitignore`) copiando el archivo de ejemplo `.env.example`:
   ```bash
   cp .env.example .env
   ```
2. Editar `.env` y definir un puerto alternativo para el host (por ejemplo, `5433` o `5434`):
   ```properties
   DB_PORT_HOST=5433
   ```
3. Levantar Docker normalmente:
   ```bash
   docker compose up -d
   ```
   Docker mapeará `5433:5432`. El contenedor de PostgreSQL seguirá escuchando en su puerto interno `5432`, pero en la máquina del desarrollador se accederá a través del puerto `5433`.

#### ¿Por qué esto NO rompe nada en el sistema?
* **Dentro de la Red Docker (`bda-net`):** Los microservicios (`servicio-torneos`, `servicio-operaciones-partidos`) se comunican con PostgreSQL usando el hostname del contenedor y el puerto interno fijo: `jdbc:postgresql://postgres-db:5432/fulvo_db`. El mapeo del host no afecta en absoluto esta comunicación.
* **Desde Clientes Externos (DBeaver / pgAdmin / psql):** El desarrollador se conectará a `localhost:5433` en lugar de `5432`.
* **Si se ejecuta un microservicio localmente en el IDE (fuera de Docker):** Los microservicios están parametrizados en `application.yml` para leer:
  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/fulvo_db?currentSchema=...
  ```
  El desarrollador solo debe agregar la variable de entorno `DB_PORT=5433` o el parámetro JVM `-DDB_PORT=5433` en su Run Configuration de IntelliJ / VSCode / Eclipse.

#### Solución Alternativa (Detener el Servicio Local de Windows)
Si el desarrollador prefiere no cambiar de puerto y liberar el 5432, puede detener temporalmente su servicio nativo de Windows:
```powershell
# En PowerShell como Administrador:
Stop-Service -Name postgresql*
```

---

# ETAPA 1 (Día 1 / Sprint 1): Infraestructura Base, Common Lib, Scaffolding y Gateway

---

### Subsección 1.1: Servicios de Soporte Contenerizados [Tomas & Luis Miguel]

#### Precondición
Estar ubicado en `tpi-fulvo/` con Docker Desktop iniciado.

#### Prueba 1.1.1: Levantamiento de Contenedores
* **Opción C (Consola):**
  ```bash
  docker compose up -d
  docker compose ps
  ```
* **Opción B (Navegador):**
  * Abrir Docker Desktop y verificar que el contenedor compuesto `tpi-fulvo` esté en verde con los 4 servicios activos (`postgres-db`, `keycloak-server`, `mailpit`, `wiremock`).

#### Prueba 1.1.2: Verificación de Esquemas de Base de Datos
* **Opción B (Navegador / DBeaver / pgAdmin):**
  * Conectarse a `localhost:5432` con usuario `postgres`, contraseña `postgres`, base `fulvo_db`.
  * Expandir el árbol de esquemas: Comprobar visualmente que existan `esquema_torneos` y `esquema_operaciones`.
* **Opción C (Consola):**
  ```bash
  docker compose exec -it postgres-db psql -U postgres -d fulvo_db -c "\dn"
  ```

#### Prueba 1.1.3: Emisión de Token en Keycloak
* **Opción A (Postman):**
  * **Método y URL:** `POST {{keycloak_url}}/realms/fulvo-realm/protocol/openid-connect/token`
  * **Body (`x-www-form-urlencoded`):**
    * `client_id`: `fulvo-backend`
    * `grant_type`: `password`
    * `username`: `admin1`
    * `password`: `admin123`
  * **Status Esperado:** `200 OK`.
  * **Inspección Visual:** Copiar el `access_token` resultante, pegarlo en [jwt.io](https://jwt.io) y verificar que en el payload aparezca `"roles": ["ROL_ORGANIZADOR"]`.
* **Opción B (Navegador):**
  * Abrir: [http://localhost:8090/realms/fulvo-realm/.well-known/openid-configuration](http://localhost:8090/realms/fulvo-realm/.well-known/openid-configuration)
  * Comprobar que retorne el documento JSON de autodescubrimiento OIDC.
* **Opción C (Consola):**
  ```bash
  curl -s -X POST http://localhost:8090/realms/fulvo-realm/protocol/openid-connect/token \
    -d "grant_type=password&client_id=fulvo-backend&username=admin1&password=admin123"
  ```

#### Prueba 1.1.4: Accesibilidad de Mailpit y WireMock
* **Opción B (Navegador):**
  * Abrir Mailpit: [http://localhost:8025](http://localhost:8025) $\to$ Visualizar la interfaz limpia de la bandeja de correos.
  * Abrir WireMock Mappings: [http://localhost:8086/__admin/mappings](http://localhost:8086/__admin/mappings) $\to$ Visualizar el JSON con los stubs configurados.

---

### Subsección 1.2: Librería Compartida `fulvo-common-lib` [Luis Miguel]

#### Prueba 1.2.1: Compilación e Instalación Local en `.m2`
* **Opción C (Consola):**
  ```bash
  cd tpi-fulvo/fulvo-common-lib
  mvn clean install -DskipTests
  ```
* **Opción B (Navegador / Explorador de Archivos):**
  * Abrir el explorador de Windows en: `%USERPROFILE%\.m2\repository\com\fulvo\fulvo-common-lib\1.0.0-SNAPSHOT`
  * Verificar visualmente la presencia de `fulvo-common-lib-1.0.0-SNAPSHOT.jar` y su correspondiente `.pom`.

---

### Subsección 1.3: Scaffolding Base de `servicio-torneos` [Gastón]

#### Prueba 1.3.1: Compilación y Conexión al Esquema de Torneos
* **Opción C (Consola):**
  ```bash
  cd tpi-fulvo/servicio-torneos
  mvn clean package -DskipTests
  mvn spring-boot:run
  ```
* **Opción B (Navegador):**
  * Abrir [http://localhost:8081/health-check](http://localhost:8081/health-check) (si se ejecuta en dev local).
  * Verificar en pantalla: `{"status":"UP"}`.

---

### Subsección 1.4: Scaffolding Base de `servicio-operaciones-partidos` [Santiago]

#### Prueba 1.4.1: Compilación y Conexión al Esquema de Operaciones
* **Opción C (Consola):**
  ```bash
  cd tpi-fulvo/servicio-operaciones-partidos
  mvn clean package -DskipTests
  mvn spring-boot:run
  ```
* **Opción B (Navegador):**
  * Abrir [http://localhost:8082/health-check](http://localhost:8082/health-check).
  * Verificar en pantalla: `{"status":"UP"}`.

---

### Subsección 1.5: Gateway Reactivo (`api-gateway`) y Seguridad OIDC [Tomas]

#### Precondición
`api-gateway` levantado en el puerto `8080`.

#### Prueba 1.5.1: Health Check de Gateway
* **Opción B (Navegador):**
  * Abrir directamente en Chrome/Edge: [http://localhost:8080/health-check](http://localhost:8080/health-check)
  * **Resultado Visual:** `{"status":"UP"}` con HTTP 200.
* **Opción A (Postman):**
  * `GET {{gateway_url}}/health-check` $\to$ Status `200 OK`.

#### Prueba 1.5.2: Bloqueo de Acceso Anónimo (EN-01)
* **Opción A (Postman):**
  * **Método y URL:** `GET {{gateway_url}}/v1/torneos`
  * **Authorization:** `No Auth`
  * **Status Esperado:** `401 Unauthorized`.
* **Opción B (Navegador):**
  * Abrir en el navegador [http://localhost:8080/v1/torneos](http://localhost:8080/v1/torneos).
  * **Resultado Visual:** Pantalla blanca de error HTTP 401 Unauthorized o ventana emergente solicitando credenciales.

#### Prueba 1.5.3: Bloqueo por Rol Insuficiente (EN-02)
* **Opción A (Postman):**
  * **Método y URL:** `POST {{gateway_url}}/v1/torneos`
  * **Authorization:** `Bearer Token` $\to$ `{{token_fanatico}}`
  * **Headers:** `Content-Type: application/json`
  * **Body (raw JSON):** `{"nombre": "Torneo Ilegal"}`
  * **Status Esperado:** `403 Forbidden`.
* **Opción B (Swagger UI en Navegador):**
  * Abrir [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
  * Hacer clic en `Authorize`, pegar el token de fanático.
  * Ir al endpoint `POST /v1/torneos`, pulsar `Try it out`, pegar el body y pulsar `Execute`.
  * **Resultado Visual:** Código de respuesta `403 Forbidden` en la consola interactiva.

---

## ?? Recapitulación y Prueba Integral de la ETAPA 1 (E2E de Etapa)

### 1. ¿Qué debe quedar funcionando al finalizar la Etapa 1?
1. **Contenedores de Soporte e Infraestructura Base:**
   * `postgres-db`: PostgreSQL 16 activo, saludable y con los esquemas lógicos `esquema_torneos` y `esquema_operaciones` creados limpiamente mediante `initdb/init-schemas.sql`.
   * `keycloak-server`: Keycloak 26.5.0 en puerto host `8090` con el Realm `fulvo-realm` importado, cliente público `fulvo-backend`, los 4 roles del sistema (`ROL_ORGANIZADOR`, `ROL_DELEGADO`, `ROL_ARBITRO`, `ROL_FANATICO`) y 4 usuarios de prueba (`admin1`, `delegado1`, `arbitro1`, `fanatico1` con clave `admin123`).
   * `mailpit`: Servidor SMTP en puerto `1025` y consola Web interactiva accesible en puerto `8025`.
   * `wiremock`: Servidor de emulación en puerto `8086` listo para responder contratos mock de geocodificación y clima.
2. **Librería Compartida (`fulvo-common-lib`):**
   * Artefacto `com.fulvo:fulvo-common-lib:1.0.0-SNAPSHOT` instalado físicamente en el repositorio local Maven (`~/.m2`).
   * Contratos DTOs (`CrucePartidoDTO`, `NotificacionEmailDTO`, etc.), excepciones comunes y manejador RFC 7807 (`ProblemDetail`) disponibles para todos los módulos.
3. **Scaffoldings de Negocio:**
   * `servicio-torneos`: Proyecto Spring Boot 4.0.8 compilando limpiamente (`mvn clean package -DskipTests`) y conectado a `esquema_torneos`.
   * `servicio-operaciones-partidos`: Proyecto Spring Boot 4.0.8 compilando limpiamente y conectado a `esquema_operaciones`.
4. **Perímetro de Seguridad en Gateway (`api-gateway`):**
   * Spring Cloud Gateway en puerto `8080` con seguridad reactiva OIDC validando tokens JWT de Keycloak y mapeando autoridades a `ROL_*`.
   * Endpoint de Actuator `/health-check` en estado `UP`.
   * Bloqueo inmediato de accesos anónimos con `401 Unauthorized`.
   * Bloqueo de peticiones con rol no autorizado con `403 Forbidden`.

---

### 2. Paso a Paso Consolidado para Probar Toda la Etapa 1 Junta (Secuencia E2E)

Ejecutar la siguiente secuencia ordenada para comprobar la totalidad de la Etapa 1:

1. **Paso 1 (Infraestructura Docker):**
   * En terminal, ejecutar `docker compose up -d`.
   * Verificar con `docker compose ps` que los contenedores `postgres-db`, `keycloak-server`, `mailpit` y `wiremock` estén en estado `Up` o `healthy`.
2. **Paso 2 (Verificación de Base de Datos):**
   * Abrir DBeaver / pgAdmin o ejecutar por consola: `docker compose exec -it postgres-db psql -U postgres -d fulvo_db -c "\dn"`.
   * Comprobar que aparezcan listados los esquemas `esquema_torneos` y `esquema_operaciones`.
3. **Paso 3 (Servicios Web en el Navegador):**
   * Abrir [http://localhost:8090/realms/fulvo-realm/.well-known/openid-configuration](http://localhost:8090/realms/fulvo-realm/.well-known/openid-configuration) $\to$ Verificar el documento JSON OIDC.
   * Abrir [http://localhost:8025](http://localhost:8025) $\to$ Visualizar la interfaz web de Mailpit limpia y activa.
   * Abrir [http://localhost:8086/__admin/mappings](http://localhost:8086/__admin/mappings) $\to$ Comprobar respuesta JSON de WireMock.
4. **Paso 4 (Librería Común en `.m2`):**
   * En la carpeta de la librería, ejecutar `mvn clean install -DskipTests`.
   * Constatar el mensaje `BUILD SUCCESS` y la presencia del archivo JAR en el `.m2` local.
5. **Paso 5 (Arranque de Microservicios y Gateway):**
   * Iniciar `servicio-torneos`, `servicio-operaciones-partidos` y `api-gateway`.
   * Comprobar que no existan errores de conexión a PostgreSQL ni fallos de beans en los logs.
6. **Paso 6 (Health Check del Gateway en Postman o Navegador):**
   * Enviar `GET http://localhost:8080/health-check`.
   * Constatar status `200 OK` con cuerpo `{"status":"UP"}`.
7. **Paso 7 (Obtención de Tokens JWT en Postman):**
   * Enviar petición de login `POST http://localhost:8090/realms/fulvo-realm/protocol/openid-connect/token` para `admin1` (`ROL_ORGANIZADOR`) y `fanatico1` (`ROL_FANATICO`).
   * Constatar status `200 OK` y copiar los respectivos tokens en las variables de entorno de Postman (`token_organizador` y `token_fanatico`).
8. **Paso 8 (Prueba de Bloqueo Anónimo - 401 Unauthorized):**
   * Enviar `GET http://localhost:8080/v1/torneos` **sin** cabecera de autenticación.
   * Constatar status `401 Unauthorized`.
9. **Paso 9 (Prueba de Bloqueo por Rol Insuficiente - 403 Forbidden):**
   * Enviar `POST http://localhost:8080/v1/torneos` adjuntando `Authorization: Bearer {{token_fanatico}}` y body `{"nombre":"Torneo Ilegal"}`.
   * Constatar status `403 Forbidden`.

---

### 3. Resultados Esperados en la Totalidad de Pruebas de la Etapa 1

| # | Componente / Endpoint | Método | Auth / Token | Status Esperado | Resultado Observable / Cuerpo de Respuesta |
|:---:|---|:---:|:---:|:---:|---|
| **1** | `postgres-db` (`\dn`) | CLI / SQL | Credenciales DB | `Conexión OK` | Esquemas `esquema_torneos` y `esquema_operaciones` creados. |
| **2** | Keycloak OIDC Well-Known | `GET` | Público | `200 OK` | JSON con `issuer: ".../realms/fulvo-realm"`. |
| **3** | Mailpit Web UI (`:8025`) | `GET` | Navegador Web | `200 OK` | Interfaz web de Mailpit renderizada sin errores. |
| **4** | WireMock Admin (`:8086`) | `GET` | Navegador Web | `200 OK` | JSON con stubs cargados `{"mappings":[...],"meta":{...}}`. |
| **5** | `fulvo-common-lib` (`mvn install`) | CLI | Local | `BUILD SUCCESS` | JAR instalado en `~/.m2/repository/com/fulvo/fulvo-common-lib/`. |
| **6** | `{{gateway_url}}/health-check` | `GET` | Público | `200 OK` | `{"status":"UP"}`. Gateway reactivo en línea. |
| **7** | Keycloak Token (`/token`) | `POST` | `admin1` / `admin123` | `200 OK` | JSON con `access_token` conteniendo `"roles": ["ROL_ORGANIZADOR"]`. |
| **8** | Keycloak Token (`/token`) | `POST` | `fanatico1` / `admin123` | `200 OK` | JSON con `access_token` conteniendo `"roles": ["ROL_FANATICO"]`. |
| **9** | `{{gateway_url}}/v1/torneos` | `GET` | *(Sin Token)* | `401 Unauthorized` | Petición bloqueada en el perímetro del Gateway. |
| **10** | `{{gateway_url}}/v1/torneos` | `POST` | `Bearer {{token_fanatico}}` | `403 Forbidden` | Petición rechazada: fanático no tiene permiso de creación de torneos. |

---

# ETAPA 2 (Día 2 / Sprint 2 - Parte A): Core de Torneos, Admisiones y Fixtures

---

### Subsección 2.1: Modelado JPA y Lógica de Admisión de Torneos [Gastón]

#### Prueba 2.1.1: Alta Exitosa de Torneo (EP-01)
* **Opción A (Postman):**
  * **Método y URL:** `POST {{gateway_url}}/v1/torneos`
  * **Authorization:** `Bearer Token` $\to$ `{{token_organizador}}`
  * **Headers:** `Content-Type: application/json`
  * **Body (raw JSON):**
    ```json
    {
      "nombre": "Torneo Apertura 2026",
      "formato": "LIGA",
      "cupoEquipos": 10,
      "fechaInicio": "2026-04-01T09:00:00"
    }
    ```
  * **Status Esperado:** `201 Created`.
  * **Inspección Visual:** Cabecera `Location` con la URI del recurso creado y JSON con `id: 1`, `estado: "INSCRIPCION_ABIERTA"`.
* **Opción B (Swagger UI en Navegador):**
  * Abrir [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) $\to$ Seleccionar definición `servicio-torneos`.
  * `Authorize` con `{{token_organizador}}`.
  * Ejecutar `POST /v1/torneos` con el JSON anterior $\to$ Visualizar respuesta `201`.

#### Prueba 2.1.2: Rechazo por Cupo Inválido (EN-03)
* **Opción A (Postman):**
  * Mismo request cambiando `"cupoEquipos": 12`.
  * **Status Esperado:** `400 Bad Request`.
  * **Inspección Visual:** Objeto RFC 7807 (`ProblemDetail`) con `"title": "Regla de Negocio Violada"` y `"detail": "El cupo de equipos para Liga debe ser exactamente 10 o 20"`.

#### Prueba 2.1.3: Postulación de Equipo con Lista de Buena Fe (EP-02)
* **Opción A (Postman):**
  * **Método y URL:** `POST {{gateway_url}}/v1/torneos/1/solicitudes-inscripcion`
  * **Authorization:** `Bearer Token` $\to$ `{{token_fanatico}}`
  * **Headers:** `Content-Type: application/json`
  * **Body (raw JSON):**
    ```json
    {
      "nombreEquipo": "Los Halcones FC",
      "jugadores": [
        {"dni": "30111001", "nombre": "Juan", "apellido": "Perez", "posicion": "ARQUERO", "dorsal": 1, "esCapitan": true},
        {"dni": "30111002", "nombre": "Carlos", "apellido": "Gomez", "posicion": "DEFENSOR", "dorsal": 2, "esCapitan": false},
        {"dni": "30111003", "nombre": "Luis", "apellido": "Diaz", "posicion": "DEFENSOR", "dorsal": 3, "esCapitan": false},
        {"dni": "30111004", "nombre": "Marcos", "apellido": "Rojo", "posicion": "DEFENSOR", "dorsal": 4, "esCapitan": false},
        {"dni": "30111005", "nombre": "Enzo", "apellido": "Perez", "posicion": "VOLANTE", "dorsal": 5, "esCapitan": false},
        {"dni": "30111006", "nombre": "Rodrigo", "apellido": "De Paul", "posicion": "VOLANTE", "dorsal": 6, "esCapitan": false},
        {"dni": "30111007", "nombre": "Angel", "apellido": "Di Maria", "posicion": "VOLANTE", "dorsal": 7, "esCapitan": false},
        {"dni": "30111008", "nombre": "Julian", "apellido": "Alvarez", "posicion": "DELANTERO", "dorsal": 9, "esCapitan": false},
        {"dni": "30111009", "nombre": "Lionel", "apellido": "Messi", "posicion": "DELANTERO", "dorsal": 10, "esCapitan": false},
        {"dni": "30111010", "nombre": "Lautaro", "apellido": "Martinez", "posicion": "DELANTERO", "dorsal": 11, "esCapitan": false},
        {"dni": "30111011", "nombre": "Nahuel", "apellido": "Molina", "posicion": "DEFENSOR", "dorsal": 12, "esCapitan": false},
        {"dni": "30111012", "nombre": "Cristian", "apellido": "Romero", "posicion": "DEFENSOR", "dorsal": 13, "esCapitan": false},
        {"dni": "30111013", "nombre": "Nicolas", "apellido": "Otamendi", "posicion": "DEFENSOR", "dorsal": 14, "esCapitan": false},
        {"dni": "30111014", "nombre": "Nicolas", "apellido": "Tagliafico", "posicion": "DEFENSOR", "dorsal": 15, "esCapitan": false},
        {"dni": "30111015", "nombre": "Emiliano", "apellido": "Martinez", "posicion": "ARQUERO", "dorsal": 23, "esCapitan": false}
      ]
    }
    ```
  * **Status Esperado:** `202 Accepted`.

#### Prueba 2.1.4: Rechazo por Doble Inscripción en Mismo Torneo (EN-05)
* **Opción A (Postman):**
  * Volver a enviar la solicitud anterior con el mismo `{{token_fanatico}}`.
  * **Status Esperado:** `409 Conflict`.
  * **Inspección Visual:** Mensaje indicando que el fanático ya cuenta con una solicitud activa en el certamen (RN-03).

#### Prueba 2.1.5: Aprobación de Solicitud de Admisión
* **Opción A (Postman):**
  * **Método y URL:** `PATCH {{gateway_url}}/v1/solicitudes-inscripcion/1/admision`
  * **Authorization:** `Bearer Token` $\to$ `{{token_organizador}}`
  * **Headers:** `Content-Type: application/json`
  * **Body (raw JSON):** `{"estado": "ACEPTADA"}`
  * **Status Esperado:** `200 OK`.
  * **Inspección Visual:** `estado: "ACEPTADA"`, `equipoId` generado y cupo disponible del torneo reducido.

---

### Subsección 2.2: Generación Algorítmica Determinística de Fixtures [Gastón]

#### Prueba 2.2.1: Bloqueo de Cierre con Cupo Incompleto (RN-01)
* **Opción A (Postman):**
  * **Método y URL:** `PATCH {{gateway_url}}/v1/torneos/1/estado`
  * **Authorization:** `Bearer Token` $\to$ `{{token_organizador}}`
  * **Body (raw JSON):** `{"estado": "INSCRIPCION_CERRADA"}`
  * **Status Esperado:** `409 Conflict`.

#### Prueba 2.2.2: Generación de Fixture y Cierre al 100%
* **Opción A (Postman):**
  * Inscribir y admitir los 9 equipos restantes.
  * Enviar: `POST {{gateway_url}}/v1/torneos/1/fixtures` con `{{token_organizador}}`.
  * **Status Esperado:** `200 OK`.
  * **Inspección Visual:** Lista completa de cruces (`partidos`) en formato Round Robin y estado del torneo en `INICIADO`.

---

### Subsección 2.3: Contrato y Recepción de Cruces en Operaciones [Santiago]

#### Prueba 2.3.1: Verificación de Partidos en PostgreSQL
* **Opción B (Navegador / DBeaver / pgAdmin):**
  * Ejecutar en el editor SQL:
    ```sql
    SELECT id, torneo_id, equipo_local_id, equipo_visitante_id, estado 
    FROM esquema_operaciones.partidos 
    WHERE torneo_id = 1;
    ```
  * **Resultado Visual:** Verificar la grilla de partidos con estado `PROGRAMADO`.

---

### Subsección 2.4: Notificaciones de Admisión en Mailpit [Luis Miguel]

#### Prueba 2.4.1: Inspección de Correos en el Navegador
* **Opción B (Navegador):**
  * Abrir: [http://localhost:8025](http://localhost:8025)
  * **Resultado Visual:**
    * Correo recibido con asunto *"Postulación Recibida - Torneo Apertura 2026"*.
    * Correo recibido con asunto *"Admisión Aprobada - Bienvenido a Fulvo'"*.
    * Hacer clic en los correos y leer el cuerpo HTML renderizado.

---

### Subsección 2.5: Enrutamiento Perimetral y Reglas de Seguridad para Torneos en API Gateway [Tomas]

#### Prueba 2.5.1: Enrutamiento y Verificación de Roles en Creación de Torneo (RN-01 / Control RBAC)
* **Opción A (Postman):**
  * **Intento Anónimo:**
    * **Método y URL:** `POST {{gateway_url}}/v1/torneos`
    * **Authorization:** `No Auth`
    * **Status Esperado:** `401 Unauthorized`.
  * **Intento con Rol Fanático:**
    * **Authorization:** `Bearer Token` $\to$ `{{token_fanatico}}`
    * **Status Esperado:** `403 Forbidden`.
  * **Intento con Rol Organizador:**
    * **Authorization:** `Bearer Token` $\to$ `{{token_organizador}}`
    * **Body (raw JSON):** `{"nombre": "Torneo Apertura 2026", "formato": "LIGA", "cupoEquipos": 10}`
    * **Status Esperado:** `201 Created`.
* **Opción B (Swagger UI en Navegador):**
  * Abrir: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) $\to$ Seleccionar definición `servicio-torneos`.
  * Autenticarse primero con `{{token_fanatico}}` y ejecutar `POST /v1/torneos` $\to$ verificar `403 Forbidden`.
  * Re-autenticarse con `{{token_organizador}}` y ejecutar `POST /v1/torneos` $\to$ verificar `201 Created`.

---

### Subsección 2.6: Habilitación de OpenAPI 3.0, Swagger UI y Agregación Perimetral en API Gateway [Tomas]

#### Precondición
Tener levantados en local o Docker: `api-gateway` (puerto `8080`), `servicio-torneos` (puerto `8081`) y `servicio-operaciones-partidos` (puerto `8082`).

#### Prueba 2.6.1: Acceso Directo a OpenAPI Docs de Downstream Microservices
* **Opción B (Navegador):**
  1. Abrir en el navegador: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs) (`servicio-torneos`).
  2. Abrir en el navegador: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs) (`servicio-operaciones-partidos`).
  * **Resultado Visual:** Se descarga o visualiza el JSON de la especificación OpenAPI 3.0 de cada microservicio, conteniendo la lista de paths (`/v1/torneos`, `/v1/solicitudes-inscripcion`, `/v1/partidos/lote`, etc.), esquemas DTO y definiciones de seguridad Bearer JWT.

#### Prueba 2.6.2: Enrutamiento Perimetral de OpenAPI Docs en API Gateway (Sin Token)
* **Opción B (Navegador):**
  1. Abrir: [http://localhost:8080/api/torneos/v3/api-docs](http://localhost:8080/api/torneos/v3/api-docs)
  2. Abrir: [http://localhost:8080/api/partidos/v3/api-docs](http://localhost:8080/api/partidos/v3/api-docs)
  * **Resultado Visual:** Ambos endpoints responden con HTTP 200 y el JSON de OpenAPI correspondiente a través del puerto 8080, validando que el Gateway permite el bypass de autenticación para las rutas públicas de documentación (`/v3/api-docs/**`).

#### Prueba 2.6.3: Navegación y Prueba Interactiva en Swagger UI Centralizado
* **Opción B (Navegador):**
  1. Abrir en el navegador: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
  2. **Verificación de Agregación:** En el selector desplegable superior derecho (*"Select a definition"*), constatar la presencia y alternancia entre:
     * `servicio-torneos` (`/api/torneos/v3/api-docs`)
     * `servicio-operaciones-partidos` (`/api/partidos/v3/api-docs`)
  3. **Verificación de Seguridad Bearer JWT:**
     * Hacer clic en el botón verde **Authorize** (con ícono de candado).
     * Pegar un token JWT válido de Keycloak (ej. de `admin1` u `organizador`) y pulsar **Authorize**.
     * Comprobar que los candados de los endpoints protegidos cambian a estado cerrado.
  4. **Prueba Interactiva Dinámica:**
     * Seleccionar `servicio-torneos`, expandir `POST /v1/torneos`, pulsar **Try it out**, ingresar el body y presionar **Execute**.
     * Verificar la respuesta interactiva en vivo con status `201 Created` y el payload de respuesta renderizado.
     * Cambiar en el desplegable a `servicio-operaciones-partidos` y verificar que el endpoint `POST /v1/partidos/lote` se visualiza con su esquema `List<CrucePartidoDTO>` autogenerado dinámicamente desde el código de Spring.

---

## ?? Recapitulación y Prueba Integral de la ETAPA 2 (E2E de Etapa)

### 1. ¿Qué debe quedar funcionando al finalizar la Etapa 2?
1. **Ciclo Registral y Dominio en `servicio-torneos` (`esquema_torneos`):**
   * Creación formal de torneos validando cupos estrictos (10 o 20 para Liga; 8 o 16 para Llaves, RN-01). Bloqueo de cupos inválidos con `400 Bad Request`.
   * Gestión de solicitudes de postulación enviadas por usuarios con `ROL_FANATICO`: validación estricta de lista de buena fe (15 a 22 futbolistas, arquero obligatorio, capitán único y DNI irrepetible en el certamen, RN-02 y RN-03).
   * Admisión y evaluación por el Organizador (`ROL_ORGANIZADOR`): aceptación crea la entidad `Equipo` en estado `ACTIVO`, promueve al solicitante a `ROL_DELEGADO`, descuenta cupo y rechazo registra motivo (RN-04).
   * Cierre estricto de inscripciones al 100% de cupo (bloqueo con `409 Conflict` si faltan equipos, RN-01).
2. **Generación Algorítmica y Despacho Inter-Servicio:**
   * Motor determinístico de fixtures: algoritmo Round Robin para Liga y árbol de emparejamiento con fases avanzadas en `null` para Llaves.
   * Transición del torneo a estado `INICIADO`.
   * Cliente `RestClient` en `servicio-torneos` despachando sincrónicamente el lote de cruces (`List<CrucePartidoDTO>`) hacia `servicio-operaciones-partidos`.
3. **Recepción y Persistencia Desacoplada en `servicio-operaciones-partidos` (`esquema_operaciones`):**
   * Endpoint `POST /v1/partidos/lote` persistiendo transaccionalmente los partidos recibidos en estado `PROGRAMADO`.
   * Aislamiento total: vínculos por claves lógicas (`torneo_id`, `equipo_local_id`, `equipo_visitante_id`) sin foreign keys físicas cruzadas.
4. **Notificaciones Transaccionales:**
   * Despacho asíncrono de emails ante postulación (confirmación al fanático) y resolución de admisión (alegría o rechazo fundamentado al delegado) auditables en Mailpit (:8025) (RN-12).
5. **Perímetro en API Gateway:**
   * Enrutamiento de `/v1/torneos/**` y `/v1/solicitudes-inscripcion/**` con control reactivo de acceso por roles.
6. **Catálogo Interactivo OpenAPI 3.0 y Swagger UI Centralizado en API Gateway [Tomas]:**
   * Swagger UI unificado y accesible desde `http://localhost:8080/swagger-ui.html` agregando las especificaciones vivas de `servicio-torneos` (`/api/torneos/v3/api-docs`) y `servicio-operaciones-partidos` (`/api/partidos/v3/api-docs`).
   * Soporte interactivo para autenticación Bearer JWT mediante el botón **Authorize**, permitiendo que cualquier desarrollador o auditor pruebe visualmente los nuevos endpoints a medida que se implementan dinámicamente en el código Spring Boot.

---

### 2. Paso a Paso Consolidado para Probar Toda la Etapa 2 Junta (Secuencia E2E)

Ejecutar la siguiente secuencia de punta a punta en Postman GUI o Swagger UI:

1. **Paso 1 (Token de Organizador y Fanático):**
   * Realizar login en Keycloak como `admin1` y como `fanatico1` para obtener tokens válidos.
2. **Paso 2 (Prueba de Creación de Torneo con Cupo Inválido - EN-03 / RN-01):**
   * Enviar `POST {{gateway_url}}/v1/torneos` con `{{token_organizador}}` definiendo `cupoEquipos: 12` (inválido para Liga).
   * Constatar respuesta `400 Bad Request` (RFC 7807 `ProblemDetail`).
3. **Paso 3 (Creación Exitosa de Torneo - EP-01):**
   * Enviar `POST {{gateway_url}}/v1/torneos` con `{{token_organizador}}` creando *"Torneo Apertura 2026"* (formato `LIGA`, `cupoEquipos: 10`).
   * Constatar status `201 Created` y almacenar el `torneo_id` generado.
4. **Paso 4 (Prueba de Postulación con Plantel Inválido - EN-04 / RN-02):**
   * Enviar `POST {{gateway_url}}/v1/torneos/{{torneo_id}}/solicitudes-inscripcion` con `{{token_fanatico}}` enviando solo 12 jugadores o sin arquero.
   * Constatar status `400 Bad Request` detallando el incumplimiento de la lista de buena fe.
5. **Paso 5 (Postulación Exitosa de Equipo - EP-03 / RN-02):**
   * Enviar `POST {{gateway_url}}/v1/torneos/{{torneo_id}}/solicitudes-inscripcion` con `{{token_fanatico}}` enviando 16 jugadores tipificados (1 arquero, 1 capitán y DNIs únicos).
   * Constatar status `202 Accepted` y guardar el `solicitud_id`.
6. **Paso 6 (Prueba de Doble Postulación - EN-06 / RN-03):**
   * Reintentar enviar otra postulación para el mismo torneo con el mismo `{{token_fanatico}}`.
   * Constatar status `409 Conflict` (solo una solicitud activa por fanático).
7. **Paso 7 (Admisión de Solicitud y Promoción - EP-04 / RN-04):**
   * Enviar `PATCH {{gateway_url}}/v1/solicitudes-inscripcion/{{solicitud_id}}/admision` con `{{token_organizador}}` (`estado: "ACEPTADA"`).
   * Constatar status `200 OK`. Verificar que se haya creado el `Equipo` en estado `ACTIVO` y cupo descontado.
8. **Paso 8 (Inspección de Notificaciones en Mailpit):**
   * Abrir en navegador [http://localhost:8025](http://localhost:8025).
   * Verificar la recepción de los correos automáticos: confirmación de postulación y notificación de admisión aprobada.
9. **Paso 9 (Prueba de Cierre Prematuro de Torneo - EN-03 / RN-01):**
   * Enviar `PATCH {{gateway_url}}/v1/torneos/{{torneo_id}}/estado` con `{{token_organizador}}` (`estado: "CERRADO_INSCRIPCION"`).
   * Constatar status `409 Conflict` porque el cupo no alcanzó el 100%.
10. **Paso 10 (Completar Cupo, Cierre y Generación de Fixture - EP-05 / FN-06):**
    * Postular y admitir los equipos restantes hasta alcanzar los 10 requeridos.
    * Cerrar inscripción: `PATCH {{gateway_url}}/v1/torneos/{{torneo_id}}/estado` a `CERRADO_INSCRIPCION` (status `200 OK`).
    * Disparar fixture: `POST {{gateway_url}}/v1/torneos/{{torneo_id}}/fixtures` (status `201 Created`). El torneo pasa a `INICIADO`.
11. **Paso 11 (Comprobar Recepción de Cruces en Operaciones):**
    * Enviar `GET {{gateway_url}}/v1/torneos/{{torneo_id}}/partidos` con `{{token_fanatico}}`.
    * Constatar status `200 OK` y verificar la lista completa de partidos persistidos en `esquema_operaciones` en estado `PROGRAMADO`.

---

### 3. Resultados Esperados en la Totalidad de Pruebas de la Etapa 2

| # | Endpoint / Operación | Método | Rol / Token | Status Esperado | Resultado Observable / Cuerpo de Respuesta |
|:---:|---|:---:|:---:|:---:|---|
| **1** | `/v1/torneos` (cupo inválido) | `POST` | `ROL_ORGANIZADOR` | `400 Bad Request` | `ProblemDetail`: "Cupo de equipos no permitido para formato LIGA". |
| **2** | `/v1/torneos` (alta válida) | `POST` | `ROL_ORGANIZADOR` | `201 Created` | JSON de Torneo en estado `ABIERTO_INSCRIPCION` con su ID asignado. |
| **3** | `/v1/torneos/{id}/solicitudes-inscripcion` (plantel corto) | `POST` | `ROL_FANATICO` | `400 Bad Request` | `ProblemDetail`: "La lista de buena fe debe contener entre 15 y 22 jugadores". |
| **4** | `/v1/torneos/{id}/solicitudes-inscripcion` (válida) | `POST` | `ROL_FANATICO` | `202 Accepted` | Solicitud registrada en estado `PENDIENTE`. |
| **5** | `/v1/torneos/{id}/solicitudes-inscripcion` (duplicada) | `POST` | `ROL_FANATICO` | `409 Conflict` | `ProblemDetail`: "El usuario ya posee una solicitud registrada para este certamen". |
| **6** | `/v1/solicitudes-inscripcion/{id}/admision` | `PATCH` | `ROL_ORGANIZADOR` | `200 OK` | Solicitud `ACEPTADA`, creación de `Equipo` en `ACTIVO` y descuento de cupo. |
| **7** | Mailpit (`http://localhost:8025`) | Web UI | Navegador Web | `200 OK` | Correos de confirmación y bienvenida recibidos y legibles en HTML. |
| **8** | `/v1/torneos/{id}/estado` (cierre con cupo incompleto) | `PATCH` | `ROL_ORGANIZADOR` | `409 Conflict` | `ProblemDetail`: "No se puede cerrar la inscripción sin alcanzar el 100% del cupo". |
| **9** | `/v1/torneos/{id}/estado` (cierre al 100%) | `PATCH` | `ROL_ORGANIZADOR` | `200 OK` | Torneo pasa exitosamente a estado `CERRADO_INSCRIPCION`. |
| **10** | `/v1/torneos/{id}/fixtures` | `POST` | `ROL_ORGANIZADOR` | `201 Created` | Torneo transiciona a `INICIADO` y cruces despachados a Operaciones. |
| **11** | `/v1/torneos/{id}/partidos` | `GET` | `ROL_FANATICO` | `200 OK` | Listado completo de partidos creados en estado `PROGRAMADO` en Operaciones. |
| **12** | `/api/torneos/v3/api-docs` y `/api/partidos/v3/api-docs` | `GET` | Público / Sin Auth | `200 OK` | JSON OpenAPI 3.0 dinámico expuesto a través de Gateway sin bloqueo 401. |
| **13** | Swagger UI (`http://localhost:8080/swagger-ui.html`) | Web UI | Navegador Web | `200 OK` | Consola Swagger UI interactiva con selector desplegable de microservicios y soporte Bearer JWT. |

---

# ETAPA 3 (Día 2 / Sprint 2 - Parte B): Core de Operaciones, Arbitraje y Posiciones

---

### Subsección 3.1: Sedes, Árbitros y Asignación Anti-Superposición [Santiago]

#### Prueba 3.1.1: Alta de Sede y Árbitro
* **Opción A (Postman):**
  * **1. Alta Sede:** `POST {{gateway_url}}/v1/sedes` con `{{token_organizador}}`:
    ```json
    {
      "nombre": "Estadio Mario Alberto Kempes",
      "direccion": "Av. Cárcano s/n, Córdoba",
      "latitudManual": -31.3697,
      "longitudManual": -64.2469
    }
    ```
    Status: `201 Created` $\to$ ID generado = 1.
  * **2. Alta Árbitro:** `POST {{gateway_url}}/v1/arbitros` con `{{token_organizador}}`:
    ```json
    {
      "dni": "20333444",
      "nombre": "Patricio",
      "apellido": "Loustau"
    }
    ```
    Status: `201 Created`.

#### Prueba 3.1.2: Asignación Exitosa de Partido (EP-04)
* **Opción A (Postman):**
  * `POST {{gateway_url}}/v1/partidos/1/asignaciones` con `{{token_organizador}}`:
    ```json
    {
      "sedeId": 1,
      "arbitroDni": "20333444",
      "fechaHora": "2026-05-10T16:00:00"
    }
    ```
  * **Status Esperado:** `200 OK`.

#### Prueba 3.1.3: Conflicto Horario $\pm 2\text{ horas}$ (EN-07)
* **Opción A (Postman):**
  * Asignar partido 2 en la misma sede y con el mismo árbitro a las `17:30:00`:
    `POST {{gateway_url}}/v1/partidos/2/asignaciones`
  * **Status Esperado:** `409 Conflict`.
  * **Inspección Visual:** ProblemDetail indicando conflicto por ventana horaria de 2 horas (RN-05).

---

### Subsección 3.2: Circuito Arbitral, Actas y Penales [Santiago]

#### Prueba 3.2.1: Inicio Oficial de Partido a `EN_CURSO`
* **Opción A (Postman):**
  * `PATCH {{gateway_url}}/v1/partidos/1/estado` con `{{token_arbitro}}`:
    ```json
    {"estado": "EN_CURSO"}
    ```
  * **Status Esperado:** `200 OK`.
* *Prueba Negativa (EN-08):* Si se envía con un token de otro árbitro distinto de `20333444`, debe retornar **`403 Forbidden`**.

#### Prueba 3.2.2: Carga de Acta Oficial y Finalización (EP-05)
* **Opción A (Postman):**
  * `POST {{gateway_url}}/v1/partidos/1/actas` con `{{token_arbitro}}`:
    ```json
    {
      "golesLocal": 2,
      "golesVisitante": 1,
      "incidencias": [
        {"dorsal": 10, "tipo": "GOL", "minuto": 34, "esLocal": true},
        {"dorsal": 9, "tipo": "GOL", "minuto": 78, "esLocal": true},
        {"dorsal": 11, "tipo": "GOL", "minuto": 85, "esLocal": false},
        {"dorsal": 4, "tipo": "TARJETA_AMARILLA", "minuto": 40, "esLocal": true}
      ]
    }
    ```
  * **Status Esperado:** `200 OK`. Partido en `FINALIZADO`.

#### Prueba 3.2.3: Desempate Obligatorio en Llaves (RN-10)
* Enviar acta con resultado empatado `1 - 1` en certamen de llaves sin penales:
* **Status Esperado:** `400 Bad Request`.

---

### Subsección 3.3: Tabla de Posiciones y Goleadores [Santiago]

#### Prueba 3.3.1: Consulta de Tabla de Posiciones en el Navegador
* **Opción B (Navegador):**
  * Abrir en Chrome/Edge: [http://localhost:8080/v1/torneos/1/posiciones](http://localhost:8080/v1/torneos/1/posiciones)
  * *(Si el Gateway exige token para consultas públicas, usar Postman o Swagger UI).*
* **Opción A (Postman):**
  * `GET {{gateway_url}}/v1/torneos/1/posiciones` con `{{token_fanatico}}`
  * **Status Esperado:** `200 OK`.
  * **Inspección Visual:** El equipo ganador figura primero con `puntos: 3`, `pj: 1`, `pg: 1`, `gf: 2`, `gc: 1`, `dg: 1`.

#### Prueba 3.3.2: Consulta de Tabla de Goleadores
* **Opción A (Postman):**
  * `GET {{gateway_url}}/v1/torneos/1/goleadores` con `{{token_fanatico}}`
  * **Status Esperado:** `200 OK` listando a los dorsales 10, 9 y 11 con 1 gol cada uno.

---

### Subsección 3.4: Reemplazos y Sincronización de Planteles [Gastón]

#### Prueba 3.4.1: Reemplazo Exitoso de Jugador
* **Opción A (Postman):**
  * `POST {{gateway_url}}/v1/equipos/1/reemplazos-jugadores` con `{{token_delegado}}`:
    ```json
    {
      "dniSaliente": "30111002",
      "jugadorEntrante": {
        "dni": "30999888",
        "nombre": "Lucas",
        "apellido": "Beltran",
        "posicion": "DELANTERO",
        "dorsal": 18
      }
    }
    ```
  * **Status Esperado:** `200 OK`.

#### Prueba 3.4.2: Bloqueo de Reemplazo para Jugador Suspendido (EN-10 / RN-11)
* Asentar tarjeta roja directa al jugador saliente en un acta previa e intentar reemplazarlo.
* **Status Esperado:** `400 Bad Request` indicando que no se puede sustituir futbolistas suspendidos.

---

### Subsección 3.5: Seguridad Perimetral y Reglas de Autorización Arbitral en API Gateway [Tomas]

#### Prueba 3.5.1: Bloqueo Anónimo en Rutas Operativas y Arbitrales (HTTP 401)
* **Opción C (curl / PowerShell):**
  ```powershell
  Invoke-RestMethod -Method Post -Uri "http://localhost:8080/v1/partidos/1/actas" -ContentType "application/json" -Body '{"goles": []}'
  ```
* **Status Esperado:** `401 Unauthorized` con cabecera `Content-Type: application/problem+json`, `type: "https://fulvo.com/errors/unauthorized"` y propagación del encabezado `X-Correlation-ID`.

#### Prueba 3.5.2: Exclusividad Arbitral en Actas y Estados (HTTP 403 para no árbitros)
* **Opción A (Postman):**
  * Intentar asentar acta con token de Organizador o Fanático: `POST {{gateway_url}}/v1/partidos/1/actas` con `{{token_organizador}}` o `{{token_fanatico}}`:
    ```json
    {
      "goles": [
        {"dorsal": 9, "minuto": 15, "esLocal": true}
      ],
      "tarjetas": []
    }
    ```
  * **Status Esperado:** `403 Forbidden` (`ProblemDetail` RFC 7807: "El usuario autenticado no posee los privilegios requeridos para ejecutar esta operación.").
  * Intentar cambiar estado del partido a `EN_CURSO` con `{{token_organizador}}`:
    ```json
    {
      "estado": "EN_CURSO"
    }
    ```
  * **Status Esperado:** `403 Forbidden`. Solo el rol `ROL_ARBITRO` está autorizado en el perímetro del Gateway.

#### Prueba 3.5.3: Asentamiento de Acta Autorizado para Árbitro Designado
* **Opción A (Postman):**
  * `POST {{gateway_url}}/v1/partidos/1/actas` con `{{token_arbitro}}`:
    ```json
    {
      "goles": [
        {"dorsal": 9, "minuto": 22, "esLocal": true},
        {"dorsal": 10, "minuto": 70, "esLocal": false}
      ],
      "tarjetas": [
        {"dorsal": 4, "tipo": "TARJETA_AMARILLA", "minuto": 35, "esLocal": true}
      ]
    }
    ```
  * **Status Esperado:** `200 OK` (Gateway autoriza la petición y la reenvía a `servicio-operaciones-partidos:8082`).

#### Prueba 3.5.4: Exclusividad del Organizador en Sedes, Árbitros y Asignaciones
* **Opción A (Postman):**
  * Intentar crear sede con `{{token_arbitro}}`: `POST {{gateway_url}}/v1/sedes`
  * **Status Esperado:** `403 Forbidden`.
  * Intentar asignar sede y árbitro a un partido con `{{token_arbitro}}`: `POST {{gateway_url}}/v1/partidos/1/asignaciones`
  * **Status Esperado:** `403 Forbidden`.
  * Ejecutar ambas peticiones con `{{token_organizador}}`:
  * **Status Esperado:** `201 Created` y `200 OK`.

#### Prueba 3.5.5: Acceso Abierto para Consultas de Tablas y Estadísticas con Token Fanático
* **Opción A (Postman):**
  * `GET {{gateway_url}}/v1/torneos/1/posiciones` con `{{token_fanatico}}` $\to$ `200 OK`.
  * `GET {{gateway_url}}/v1/torneos/1/goleadores` con `{{token_fanatico}}` $\to$ `200 OK`.
  * `GET {{gateway_url}}/v1/torneos/1/partidos` con `{{token_fanatico}}` $\to$ `200 OK`.
  * `GET {{gateway_url}}/v1/partidos/1/estadisticas` con `{{token_fanatico}}` $\to$ `200 OK`.

---

## ?? Recapitulación y Prueba Integral de la ETAPA 3 (E2E de Etapa)

### 1. ¿Qué debe quedar funcionando al finalizar la Etapa 3?
1. **Logística de Sedes, Árbitros y Asignaciones (`servicio-operaciones-partidos`):**
   * Padrón de sedes (`Sede`) con geocodificación lat/long y padrón de colegiados arbitrales (`Arbitro`) con DNI como PK natural y vinculación a Keycloak.
   * Asignación logística (`POST /v1/partidos/{id}/asignaciones`): Validación inflexible de la regla anti-superposición horaria de $\pm 2$ horas (RN-05) tanto para la sede como para el árbitro.
2. **Circuito Arbitral y Máquina de Estados:**
   * Transición a `EN_CURSO`: Verificación estricta de identidad arbitral (`jwt.dni == partido.arbitro_dni`, RN-08). Bloqueo de árbitros no asignados con `403 Forbidden`.
   * Carga de acta oficial por dorsal (`POST /v1/partidos/{id}/actas`): Registro de goles, amonestaciones y expulsiones.
   * Definición en Llaves por penales (RN-10): En caso de empate en los 90', exigencia obligatoria de tanda de penales sin empate (`penalesLocal != penalesVisitante`), asignando el ganador y promoviéndolo a la siguiente fase de llaves.
   * Cancelación por disturbios (RN-09): Transición a `CANCELADO`, asignación de 0 puntos en tabla y descalificación de ambos equipos en llaves.
3. **Cómputo Atómico de Estadísticas y Posiciones:**
   * Al asentar el acta (`FINALIZADO`), recálculo transaccional inmediato de la tabla de posiciones (PJ, PG, PE, PP, GF, GC, DG y Puntos).
   * Endpoint de consulta de posiciones (`GET /v1/torneos/{id}/posiciones`) y ranking de goleadores (`GET /v1/torneos/{id}/goleadores`).
4. **Ciclo de Reemplazos de Planteles (`servicio-torneos`):**
   * Endpoint `POST /v1/equipos/{id}/reemplazos-jugadores` para el `ROL_DELEGADO`: hasta 5 sustituciones acumuladas y prohibición taxativa de sustituir futbolistas con `suspendido == true` (RN-11).
5. **Seguridad en Gateway:**
   * Enrutamiento y control perimetral exigiendo `ROL_ARBITRO` para actas y transiciones, `ROL_ORGANIZADOR` para asignaciones, y `ROL_DELEGADO` para reemplazos.

---

### 2. Paso a Paso Consolidado para Probar Toda la Etapa 3 Junta (Secuencia E2E)

Ejecutar la siguiente secuencia de punta a punta:

1. **Paso 1 (Tokens Requeridos):**
   * Disponer en Postman de los tokens: `token_organizador` (`admin1`), `token_arbitro` (`arbitro1`), `token_delegado` (`delegado1`) y `token_fanatico` (`fanatico1`).
2. **Paso 2 (Alta de Sede y Árbitro):**
   * Crear Sede: `POST {{gateway_url}}/v1/sedes` con `{{token_organizador}}` (guardar `sede_id: 1`).
   * Crear Árbitro: `POST {{gateway_url}}/v1/arbitros` con `{{token_organizador}}` (`dni: "20333444"`, asociando a `arbitro1`).
3. **Paso 3 (Asignación Horaria Válida):**
   * Enviar `POST {{gateway_url}}/v1/partidos/1/asignaciones` con `{{token_organizador}}` para las 16:00 hs.
   * Constatar status `200 OK`. Partido asignado con sede 1 y árbitro 20333444.
4. **Paso 4 (Prueba de Conflicto Horario $\pm 2$h - EN-07 / RN-05):**
   * Intentar asignar el Partido 2 a la misma sede o al mismo árbitro para las 17:00 hs (dentro de la ventana de superposición de 2 horas).
   * Constatar status `409 Conflict` (ProblemDetail detallando el conflicto horario).
5. **Paso 5 (Prueba de Exclusividad Arbitral - EN-10 / RN-08):**
   * Intentar iniciar el Partido 1 (`PATCH {{gateway_url}}/v1/partidos/1/estado` a `EN_CURSO`) con un token de árbitro que **no** sea el asignado.
   * Constatar status `403 Forbidden`.
6. **Paso 6 (Inicio Oficial del Partido por el Árbitro Designado):**
   * Enviar `PATCH {{gateway_url}}/v1/partidos/1/estado` a `EN_CURSO` con `{{token_arbitro}}` asignado.
   * Constatar status `200 OK`.
7. **Paso 7 (Carga de Acta Oficial por Dorsal - EP-05 / FN-09):**
   * Enviar `POST {{gateway_url}}/v1/partidos/1/actas` con `{{token_arbitro}}`: 2 goles para equipo local, 1 para visitante, tarjetas por dorsal.
   * Constatar status `200 OK`. El partido pasa a `FINALIZADO` y dispara el cómputo de la tabla.
8. **Paso 8 (Consulta de Tabla de Posiciones y Goleadores - EP-08 / FN-12):**
   * Enviar `GET {{gateway_url}}/v1/torneos/1/posiciones` con `{{token_fanatico}}`.
   * Constatar status `200 OK`: El equipo local suma 3 puntos (PG=1, GF=2, GC=1, DG=+1); el visitante suma 0 puntos.
   * Enviar `GET {{gateway_url}}/v1/torneos/1/goleadores` con `{{token_fanatico}}` y comprobar el ranking de artilleros por dorsal.
9. **Paso 9 (Prueba de Penales en Llaves - EP-07 / RN-10):**
   * En un partido de llaves empatado 1-1, enviar acta omitiendo penales $\to$ constatar rechazo `400 Bad Request`.
   * Enviar acta con `penalesLocal: 5` y `penalesVisitante: 4` $\to$ constatar status `200 OK` y promoción automática del ganador al siguiente cruce.
10. **Paso 10 (Reemplazo Exitoso de Jugador por Delegado - EP-09 / FN-11):**
    * Enviar `POST {{gateway_url}}/v1/equipos/1/reemplazos-jugadores` con `{{token_delegado}}` sustituyendo a un futbolista activo.
    * Constatar status `200 OK` y contador de cambios incrementado a 1.
11. **Paso 11 (Prueba de Bloqueo de Reemplazo a Jugador Suspendido - EN-12 / RN-11):**
    * Intentar sustituir a un jugador que recibió tarjeta roja directa (`suspendido == true`) en el acta previa.
    * Constatar rechazo con `400 Bad Request`.

---

### 3. Resultados Esperados en la Totalidad de Pruebas de la Etapa 3

| # | Endpoint / Operación | Método | Rol / Token | Status Esperado | Resultado Observable / Cuerpo de Respuesta |
|:---:|---|:---:|:---:|:---:|---|
| **1** | `/v1/sedes` | `POST` | `ROL_ORGANIZADOR` | `201 Created` | Sede deportiva registrada con ID y coordenadas. |
| **2** | `/v1/arbitros` | `POST` | `ROL_ORGANIZADOR` | `201 Created` | Árbitro empadronado con DNI como clave primaria. |
| **3** | `/v1/partidos/{id}/asignaciones` (válida) | `POST` | `ROL_ORGANIZADOR` | `200 OK` | Partido actualizado con fecha/hora, sede y árbitro asignados. |
| **4** | `/v1/partidos/{id}/asignaciones` (superposición) | `POST` | `ROL_ORGANIZADOR` | `409 Conflict` | `ProblemDetail`: "Conflicto horario: sede o árbitro asignados en ventana ±2h". |
| **5** | `/v1/partidos/{id}/estado` (árbitro no asignado) | `PATCH` | `ROL_ARBITRO` *(ajeno)* | `403 Forbidden` | Denegado: el DNI del JWT no coincide con el árbitro asignado al partido. |
| **6** | `/v1/partidos/{id}/estado` (a `EN_CURSO`) | `PATCH` | `ROL_ARBITRO` *(titular)* | `200 OK` | Partido en estado `EN_CURSO`. |
| **7** | `/v1/partidos/{id}/actas` (cierre regular) | `POST` | `ROL_ARBITRO` *(titular)* | `200 OK` | Partido en `FINALIZADO` con marcador 2-1 y tarjetas asentadas. |
| **8** | `/v1/partidos/{id}/actas` (empate en llaves sin penales) | `POST` | `ROL_ARBITRO` *(titular)* | `400 Bad Request` | `ProblemDetail`: "El empate en fase de eliminación directa exige definición por penales". |
| **9** | `/v1/torneos/{id}/posiciones` | `GET` | `ROL_FANATICO` | `200 OK` | Tabla con puntos, partidos jugados, diferencia de goles calculados atómicamente. |
| **10** | `/v1/equipos/{id}/reemplazos-jugadores` (válido) | `POST` | `ROL_DELEGADO` | `200 OK` | Baja del titular saliente, alta del ingresante y contador de cambios en 1. |
| **11** | `/v1/equipos/{id}/reemplazos-jugadores` (suspendido) | `POST` | `ROL_DELEGADO` | `400 Bad Request` | `ProblemDetail`: "No se permite sustituir a un futbolista sancionado disciplinariamente". |

---

# ETAPA 4 (Día 3 / Sprint 3 - Parte A): Adaptadores Externos, Resiliencia y Scheduler

---

### Subsección 4.1: Adaptador de Geocodificación y Fallback [Luis Miguel]

#### Prueba 4.1.1: Resolución Normal de Coordenadas
* **Opción A (Postman):**
  * `POST {{gateway_url}}/v1/geocodificaciones/direcciones` con `{{token_organizador}}`:
    ```json
    {"direccion": "Av. Cárcano s/n, Córdoba, Argentina"}
    ```
  * **Status Esperado:** `200 OK` con `latitud` y `longitud` numéricas.

#### Prueba 4.1.2: Fallback Manual ante Corte Satelital (RN-06)
* Detener el mock en WireMock o pasar una dirección desconocida enviando coordenadas de respaldo:
* **Opción A (Postman):**
  ```json
  {
    "direccion": "Calle Inexistente 9999",
    "latitudManual": -31.3697,
    "longitudManual": -64.2469
  }
  ```
  * **Status Esperado:** `201 Created` adoptando las coordenadas manuales de respaldo.
* *Prueba Negativa:* Enviar la misma petición sin lat/long manuales $\to$ **`422 Unprocessable Entity`**.

---

### Subsección 4.2 & 4.3: Clima y Notificaciones Mailpit [Luis Miguel]

#### Prueba 4.2.1: Consulta Climática
* **Opción A (Postman):**
  * `GET {{gateway_url}}/v1/clima/pronosticos?latitud=-31.3697&longitud=-64.2469&fechaHora=2026-05-10T16:00:00` con `{{token_organizador}}`.
  * **Status Esperado:** `200 OK` con `condicionAdversa: true/false`.

---

### Subsección 4.4, 4.5 & 4.6: Scheduler Climático Preventivo y Reprogramación [Santiago, Gastón, Tomas]

#### Prueba 4.4.1: Disparo Forzado del Scheduler Preventivo (EP-07 / RN-07)
* **Opción A (Postman):**
  * `POST {{gateway_url}}/v1/partidos/scheduler-clima/ejecutar` con `{{token_organizador}}`.
  * **Status Esperado:** `200 OK`.
* **Opción B (Navegador en Mailpit):**
  * Abrir [http://localhost:8025](http://localhost:8025).
  * **Resultado Visual:** Ver el correo de alerta: *"Alerta Meteorológica: Partido Suspendido por Tormenta"*.

#### Prueba 4.4.2: Reprogramación de Partido
* **Opción A (Postman):**
  * `POST {{gateway_url}}/v1/partidos/1/reprogramaciones` con `{{token_organizador}}`:
    ```json
    {
      "nuevaSedeId": 1,
      "nuevaFechaHora": "2026-05-12T16:00:00"
    }
    ```
  * **Status Esperado:** `200 OK`. Partido en estado `REPROGRAMADO`.

---

## ?? Recapitulación y Prueba Integral de la ETAPA 4 (E2E de Etapa)

### 1. ¿Qué debe quedar funcionando al finalizar la Etapa 4?
1. **Microservicios Adaptadores Aislados con Resiliencia:**
   * `servicio-geocodificacion` (:8084): integración satelital con OpenStreetMap (Nominatim), protección ante caídas con Circuit Breaker (`Resilience4j`) y fallback manual obligatorio a latitud/longitud ingresadas en la petición si el servicio externo falla (RN-06). Si falla y no se proveyeron coordenadas manuales, responde `422 Unprocessable Entity`.
   * `servicio-clima` (:8085): integración con Open-Meteo, algoritmo de evaluación climática determinando condición severa si precipitaciones acumuladas $> 40\text{ mm}$ o códigos de tormenta (RN-07).
   * `servicio-notificaciones` (:8083): microservicio desacoplado con JavaMail y captura silenciosa de excepciones SMTP para no abortar transacciones de negocio.
2. **Automatización Preventiva (Scheduler a 24 Horas):**
   * Componente `@Scheduled` en `servicio-operaciones-partidos`: evalúa automáticamente cada partido con fecha fijada a 24 horas vista.
   * Si `servicio-clima` reporta condiciones severas, el partido pasa automáticamente a `SUSPENDIDO` en la base de datos sin intervención humana y se despacha un email de alerta meteorológica al delegado en Mailpit (RN-07, RN-12).
3. **Flujo de Reprogramación y Consistencia de Torneo:**
   * Endpoint de reprogramación `POST /v1/partidos/{id}/reprogramaciones` para reasignar fecha/sede y pasar el partido a `REPROGRAMADO`.
   * Sincronización y consistencia en el calendario de `servicio-torneos`.
4. **Perímetro en API Gateway:**
   * Enrutamiento seguro de las rutas adaptadoras (`/v1/geocodificaciones/**`, `/v1/clima/**`, `/v1/notificaciones/**`) restringidas contra accesos no autorizados.

---

### 2. Paso a Paso Consolidado para Probar Toda la Etapa 4 Junta (Secuencia E2E)

Ejecutar la siguiente secuencia de punta a punta:

1. **Paso 1 (Resolución Normal de Geocodificación Satelital):**
   * Enviar `POST {{gateway_url}}/v1/geocodificaciones/direcciones` con `{{token_organizador}}` (`direccion: "Av. Cárcano s/n, Córdoba, Argentina"`).
   * Constatar status `200 OK` con `latitud` y `longitud` obtenidas de la API oficial o de WireMock.
2. **Paso 2 (Prueba de Fallback Manual ante Fallo Satelital - EN-08 / RN-06):**
   * Enviar `POST {{gateway_url}}/v1/geocodificaciones/direcciones` con una dirección no existente o simulando caída externa, incluyendo coordenadas de respaldo manuales.
   * Constatar status `201 Created` utilizando las coordenadas manuales de contingencia.
3. **Paso 3 (Prueba de Rechazo por Omisión de Coordenadas Manuales - RN-06):**
   * Reintentar la misma petición fallida sin proveer latitud/longitud manuales.
   * Constatar respuesta `422 Unprocessable Entity` (RFC 7807 `ProblemDetail`).
4. **Paso 4 (Consulta Climática Directa):**
   * Enviar `GET {{gateway_url}}/v1/clima/pronosticos?latitud=-31.3697&longitud=-64.2469&fechaHora=2026-05-10T16:00:00` con `{{token_organizador}}`.
   * Constatar status `200 OK` retornando el pronóstico y el flag `condicionAdversa`.
5. **Paso 5 (Disparo y Verificación de Notificación Transaccional):**
   * Enviar email de prueba a través de `POST {{gateway_url}}/v1/notificaciones/emails`.
   * Abrir en el navegador Mailpit (`http://localhost:8025`) y verificar la recepción inmediata del correo.
6. **Paso 6 (Ejecución Forzada del Scheduler Preventivo Climático - EN-09 / RN-07):**
   * Configurar un partido con fecha de disputa dentro de las 24 horas y sede con coordenadas de prueba.
   * Disparar el chequeo preventivo: `POST {{gateway_url}}/v1/partidos/scheduler-clima/ejecutar` con `{{token_organizador}}`.
   * Constatar status `200 OK`.
7. **Paso 7 (Comprobación de Suspensión Automática en BD y Alerta en Mailpit):**
   * Consultar el estado del partido: `GET {{gateway_url}}/v1/partidos/1` $\to$ Verificar que cambió automáticamente a `SUSPENDIDO`.
   * Abrir Mailpit en navegador: Verificar el arribo del correo *"Alerta Meteorológica: Partido Suspendido por Tormenta"*.
8. **Paso 8 (Reprogramación Logística del Partido):**
   * Enviar `POST {{gateway_url}}/v1/partidos/1/reprogramaciones` con `{{token_organizador}}` reasignando fecha y sede válida.
   * Constatar status `200 OK` y verificar que el partido transicionó al estado `REPROGRAMADO`.

---

### 3. Resultados Esperados en la Totalidad de Pruebas de la Etapa 4

| # | Endpoint / Componente | Método | Rol / Token | Status Esperado | Resultado Observable / Cuerpo de Respuesta |
|:---:|---|:---:|:---:|:---:|---|
| **1** | `/v1/geocodificaciones/direcciones` (normal) | `POST` | `ROL_ORGANIZADOR` | `200 OK` | Coordenadas oficiales obtenidas: `latitud: -31.369...`, `longitud: -64.246...`. |
| **2** | `/v1/geocodificaciones/direcciones` (fallback) | `POST` | `ROL_ORGANIZADOR` | `201 Created` | Adopción de coordenadas manuales de contingencia ante fallo satelital. |
| **3** | `/v1/geocodificaciones/direcciones` (sin fallback) | `POST` | `ROL_ORGANIZADOR` | `422 Unprocessable Entity` | `ProblemDetail`: "Dirección no resuelta y coordenadas manuales omitidas". |
| **4** | `/v1/clima/pronosticos` | `GET` | `ROL_ORGANIZADOR` | `200 OK` | JSON con pronóstico meteorológico y evaluación de severidad. |
| **5** | `/v1/notificaciones/emails` | `POST` | `ROL_ORGANIZADOR` | `202 Accepted` | Email despachado asíncronamente vía SMTP a Mailpit (:1025). |
| **6** | Mailpit Web UI (`:8025`) | Web UI | Navegador Web | `200 OK` | Correo transaccional renderizado y visible en la bandeja de entrada. |
| **7** | `/v1/partidos/scheduler-clima/ejecutar` | `POST` | `ROL_ORGANIZADOR` | `200 OK` | Scheduler ejecutado; detecta tormenta severa a 24 horas vista. |
| **8** | Base de Datos (`partido.estado`) | SQL / GET | Conexión DB / API | `SUSPENDIDO` | Partido transiciona automáticamente a estado `SUSPENDIDO` (RN-07). |
| **9** | Mailpit (Alerta de Suspensión) | Web UI | Navegador Web | `200 OK` | Correo de alerta de suspensión recibido en la casilla del delegado. |
| **10** | `/v1/partidos/{id}/reprogramaciones` | `POST` | `ROL_ORGANIZADOR` | `200 OK` | Partido actualizado con nueva fecha/sede y estado `REPROGRAMADO`. |

---

# ETAPA 5 (Día 3 / Sprint 3 - Parte B): Despliegue Docker, Swagger y Postman E2E

---

### Subsección 5.1: Despliegue y Aislamiento Perimetral Docker [Tomas]

#### Prueba 5.1.1: Verificación de Aislamiento en el Navegador
* **Opción B (Navegador):**
  * Intentar abrir directamente: [http://localhost:8081/health-check](http://localhost:8081/health-check) (puerto interno del microservicio Torneos).
  * **Resultado Visual:** Conexión rechazada (*"No se puede conectar"*).
  * Abrir a través del Gateway: [http://localhost:8080/health-check](http://localhost:8080/health-check).
  * **Resultado Visual:** `{"status":"UP"}`.

---

### Subsección 5.2, 5.3 & 5.4: Enriquecimiento, Auditoría y Verificación Final de OpenAPI y Swagger UI [Gastón, Santiago, Tomas]

> **Nota de Trazabilidad Arquitectónica:** La infraestructura base de OpenAPI, los contratos dinámicos de SpringDoc y la agregación unificada en Gateway fueron integrados y puestos operativos en la **Subsección 2.6 (Tomas)**. En esta Etapa 5 se audita y verifica el **enriquecimiento exhaustivo de metadatos**, descripciones formales (`@Operation`, `@ApiResponse`), esquemas RFC 7807 (`ProblemDetail`) y la incorporación de los microservicios adaptadores (`servicio-notificaciones`, `servicio-geocodificacion`, `servicio-clima`).

#### Prueba 5.2.1 & 5.3.1: Verificación de Documentación OpenAPI Enriquecida
* **Opción B (Navegador):**
  1. Abrir: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
  2. Seleccionar `servicio-torneos` y `servicio-operaciones-partidos`.
  3. Constatar que cada endpoint documenta detalladamente sus parámetros, payloads de ejemplo, códigos HTTP (200, 201, 202, 400, 401, 403, 409) y esquemas de error conformes a RFC 7807 (`ProblemDetail`).

#### Prueba 5.4.1: Auditoría Integral y Navegación E2E en Swagger UI Unificado
* **Opción B (Navegador):**
  1. Abrir: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
  2. En el desplegable superior derecho (*"Select a definition"*), alternar entre el catálogo completo de microservicios:
     * `servicio-torneos`
     * `servicio-operaciones-partidos`
     * `servicios-adaptadores`
  3. Clic en el botón verde **Authorize**: Pegar el token `Bearer` de Keycloak con distintos roles y comprobar la ejecución interactiva de endpoints (`Try it out` $\to$ `Execute`).
  4. **Resultado Visual:** Ver el JSON de respuesta renderizado en la página con los status esperados y esquemas completos sin errores de serialización.

---

### Subsección 5.5 & 5.6: Ejecución Automatizada en Postman / Newman [Todo el Equipo]

#### Prueba 5.6.1: Ejecución Manual en Postman (Collection Runner)
* **Opción A (Postman):**
  1. Abrir Postman Desktop.
  2. En el panel izquierdo, seleccionar la colección `Fulvo_Backend_Collection`.
  3. Seleccionar el entorno `Fulvo Local Dev`.
  4. Clic en el botón **Run** (Collection Runner).
  5. Clic en **Run Fulvo Backend Collection**.
  6. **Resultado Visual:**
     * Ver la ejecución secuencial de los 21 escenarios oficiales (`EP-01..09` y `EN-01..12`).
     * Barra de progreso al **100% Passed (Verde)** sin aserciones fallidas.

#### Prueba 5.6.2: Ejecución Headless con Newman (Para Integración Continua)
* **Opción C (Consola):**
  ```bash
  newman run postman/Fulvo_Backend_Collection.postman_collection.json \
    -e postman/Fulvo_Local_Environment.postman_environment.json \
    --reporters cli
  ```
* **Resultado Esperado:** Tabla resumen en consola con 21 pruebas exitosas y 0 fallos.

---

## ?? Recapitulación y Prueba Integral de la ETAPA 5 (E2E del Backend Completo)

### 1. ¿Qué debe quedar funcionando al finalizar la Etapa 5?
1. **Orquestación y Despliegue Unificado con Docker Compose:**
   * La totalidad del ecosistema (6 microservicios Spring Boot, base de datos PostgreSQL, Keycloak OIDC, servidor SMTP Mailpit y WireMock) orquestados mediante `docker compose up -d --build`.
   * **Aislamiento Perimetral Estricto:** Los microservicios de negocio (`servicio-torneos`, `servicio-operaciones-partidos`, `servicio-notificaciones`, `servicio-geocodificacion`, `servicio-clima`) operan 100% aislados dentro de la red interna `bda-net`, sin exponer puertos directos al host.
   * Únicamente la puerta de enlace `api-gateway` expone el puerto `8080`, Keycloak `8090`, Mailpit `8025` y WireMock `8086`.
2. **Documentación Swagger UI Agregada y Centralizada:**
   * Swagger UI unificado y accesible desde el navegador en `http://localhost:8080/swagger-ui.html`.
   * Desplegable funcional para alternar la visualización y prueba de las especificaciones OpenAPI 3.0 de todos los microservicios.
   * Botón **Authorize** con soporte para autenticación Bearer JWT.
3. **Certificación E2E de la Cátedra (Suite Postman Integral):**
   * Colección oficial `Fulvo_Backend_Collection.postman_collection.json` y entorno `Fulvo_Local_Environment.postman_environment.json` parametrizados y automatizados.
   * Ejecución exitosa de los **21 escenarios oficiales de la cátedra** (9 Positivos `EP-01..EP-09` y 12 Negativos `EN-01..EN-12`) con **100% Passed**.

---

### 2. Paso a Paso Consolidado para Probar Todo el Sistema de Punta a Punta (Certificación Final E2E)

Ejecutar la siguiente secuencia de validación integral:

1. **Paso 1 (Despliegue Limpio de la Plataforma):**
   * En la raíz del proyecto, ejecutar:
     ```bash
     docker compose down -v
     docker compose up -d --build
     ```
   * Aguardar a que todos los contenedores alcancen estado `healthy` o `Up` (`docker compose ps`).
2. **Paso 2 (Verificación de Aislamiento Perimetral):**
   * En el navegador, intentar acceder a:
     * [http://localhost:8081/health-check](http://localhost:8081/health-check) (Torneos directo) $\to$ **Conexión rechazada** (puerto aislado).
     * [http://localhost:8082/health-check](http://localhost:8082/health-check) (Operaciones directo) $\to$ **Conexión rechazada** (puerto aislado).
     * [http://localhost:8080/health-check](http://localhost:8080/health-check) (A través del Gateway) $\to$ **200 OK** `{"status":"UP"}`.
3. **Paso 3 (Inspección Interactiva en Swagger UI):**
   * Abrir [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
   * Seleccionar la definición de `servicio-torneos`, hacer clic en **Authorize** y colocar el token de organizador.
   * Ejecutar interactivamente `GET /v1/torneos` y verificar la respuesta JSON 200 OK directamente en la página web.
4. **Paso 4 (Verificación de Bandeja Mailpit):**
   * Abrir [http://localhost:8025](http://localhost:8025) y confirmar que la consola web está activa y lista para recibir correos transaccionales.
5. **Paso 5 (Ejecución Integral de la Suite en Postman GUI):**
   * Abrir Postman Desktop e importar la colección `Fulvo_Backend_Collection` y el entorno `Fulvo Local Dev`.
   * Abrir el **Collection Runner**, seleccionar la colección completa y pulsar **Run Fulvo Backend Collection**.
   * Observar el paso secuencial de todas las carpetas (Autenticación $\to$ Configuración $\to$ Inscripciones $\to$ Fixture $\to$ Contingencias $\to$ Operaciones $\to$ Reemplazos $\to$ Escenarios Negativos).
   * **Comprobar la barra de progreso 100% verde (0 fallos).**
6. **Paso 6 (Ejecución Headless vía Newman por Consola):**
   * Ejecutar en terminal:
     ```bash
     newman run postman/Fulvo_Backend_Collection.postman_collection.json \
       -e postman/Fulvo_Local_Environment.postman_environment.json \
       --reporters cli
     ```
   * Constatar el reporte final con 0 assertions failed.

---

### 3. Matriz de Resultados Esperados en la Totalidad de Pruebas (21 Escenarios Oficiales)

| Código | Tipo | Nombre del Escenario Oficial | Disparador / Endpoint | Código HTTP | Resultado Observable / Aserción Clave |
|:---:|:---:|---|---|:---:|---|
| **EP-01** | Positivo | Autenticación y Emisión de JWT | `POST /token` (Keycloak) | `200 OK` | Tokens válidos obtenidos con claims `ROL_ORGANIZADOR`, `ROL_DELEGADO`, `ROL_ARBITRO`, `ROL_FANATICO`. |
| **EP-02** | Positivo | Alta de Sede con Geocodificación | `POST /v1/sedes` | `201 Created` | Estadio geocodificado con latitud y longitud oficiales. |
| **EP-03** | Positivo | Postulación con Plantel Válido | `POST /v1/torneos/{id}/solicitudes-inscripcion` | `202 Accepted` | Solicitud en `PENDIENTE` y email de confirmación visible en Mailpit. |
| **EP-04** | Positivo | Admisión y Promoción a Delegado | `PATCH /v1/solicitudes-inscripcion/{id}/admision` | `200 OK` | `Equipo` en `ACTIVO`, solicitante promovido a `ROL_DELEGADO` y email en Mailpit. |
| **EP-05** | Positivo | Cierre de Cupo y Generación de Fixture | `POST /v1/torneos/{id}/fixtures` | `201 Created` | Torneo pasa a `INICIADO` y partidos persistidos en `PROGRAMADO` en Operaciones. |
| **EP-06** | Positivo | Asignación Logística de Encuentro | `POST /v1/partidos/{id}/asignaciones` | `200 OK` | Sede y árbitro asignados sin conflicto en ventana de 2 horas. |
| **EP-07** | Positivo | Disputa y Cierre con Penales | `POST /v1/partidos/{id}/actas` | `200 OK` | En empate en llaves, penales desempatan, pasa a `FINALIZADO` y promueve ganador. |
| **EP-08** | Positivo | Cómputo Automático de Posiciones | `GET /v1/torneos/{id}/posiciones` | `200 OK` | Tabla de posiciones refleja atómicamente PJ, PG, PE, PP, GF, GC, DG y Puntos. |
| **EP-09** | Positivo | Reemplazo Reglamentario en Competencia | `POST /v1/equipos/{id}/reemplazos-jugadores` | `200 OK` | Sustitución exitosa de jugador activo y contador de cambios incrementado. |
| **EN-01** | Negativo | Bloqueo Perimetral por Falta de Token | `GET /v1/torneos` *(Sin auth)* | `401 Unauthorized` | Petición rechazada de inmediato en el API Gateway. |
| **EN-02** | Negativo | Bloqueo Perimetral por Rol Insuficiente | `POST /v1/torneos` *(Token Fanático)* | `403 Forbidden` | Petición rechazada en el Gateway: fanático no puede crear certámenes. |
| **EN-03** | Negativo | Cierre Prematuro de Cupo Incompleto | `PATCH /v1/torneos/{id}/estado` | `409 Conflict` | `ProblemDetail`: Prohibido cerrar inscripciones con cupo menor al 100% (RN-01). |
| **EN-04** | Negativo | Plantel Incompleto o Sin Arquero | `POST /v1/torneos/{id}/solicitudes-inscripcion` | `400 Bad Request` | `ProblemDetail`: Violación a lista de buena fe (RN-02). |
| **EN-05** | Negativo | DNI Duplicado en el Certamen | `POST /v1/torneos/{id}/solicitudes-inscripcion` | `400 Bad Request` | `ProblemDetail`: Futbolista ya inscripto en otro plantel del torneo (RN-02). |
| **EN-06** | Negativo | Solicitud Múltiple por Mismo Fanático | `POST /v1/torneos/{id}/solicitudes-inscripcion` | `409 Conflict` | `ProblemDetail`: Fanático con postulación activa previa (RN-03). |
| **EN-07** | Negativo | Conflicto Horario Anti-Superposición | `POST /v1/partidos/{id}/asignaciones` | `409 Conflict` | `ProblemDetail`: Sede o árbitro ocupados en ventana de $\pm 2$ horas (RN-05). |
| **EN-08** | Negativo | Fallback Manual de Geocodificación | `POST /v1/geocodificaciones/direcciones` | `201 / 422` | Adopta lat/lon manual si falla el satélite; rechaza con 422 si se omiten (RN-06). |
| **EN-09** | Negativo | Suspensión Preventiva Climática a 24h | `POST /v1/partidos/scheduler-clima/ejecutar` | `200 OK` | Partido transiciona a `SUSPENDIDO` y alerta despachada a Mailpit (RN-07). |
| **EN-10** | Negativo | Exclusividad Arbitral de Partido | `PATCH /v1/partidos/{id}/estado` *(Árbitro ajeno)* | `403 Forbidden` | Denegado: el árbitro no está asignado a ese partido (RN-08). |
| **EN-11** | Negativo | Cancelación por Disturbios e Incidentes | `PATCH /v1/partidos/{id}/estado` *(a CANCELADO)* | `200 OK` | Partido a `CANCELADO`: 0 puntos en tabla y descalificación en llaves (RN-09). |
| **EN-12** | Negativo | Reemplazo de Jugador Suspendido / Cupo 5 | `POST /v1/equipos/{id}/reemplazos-jugadores` | `400 Bad Request` | `ProblemDetail`: Prohibido sustituir suspendidos o superar 5 cambios (RN-11). |

