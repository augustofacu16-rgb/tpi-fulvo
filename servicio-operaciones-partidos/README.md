# Servicio de operaciones de partidos

Proyecto independiente con Java 21, Spring Boot 3.3.4 y Maven.
Paquete base: `com.fulvo.operaciones`. Puerto local: `8082`.

## Requisitos

- Instalar `com.fulvo:fulvo-common-lib:1.0.0-SNAPSHOT` en el repositorio
  Maven local desde el proyecto de Luis Miguel con `mvn clean install -DskipTests`.
- Disponer de PostgreSQL 16 con base `fulvo_db` y esquema
  `esquema_operaciones`, creados por la infraestructura de la subsección 1.1.
- Definir `DB_PASSWORD` con la contraseña de PostgreSQL.

## Configuración y ejecución

| Variable | Valor predeterminado | Uso |
|---|---|---|
| `DB_HOST` | `localhost` | Host de PostgreSQL; `postgres-db` dentro de Docker |
| `DB_PORT` | `5432` | Puerto de PostgreSQL; usar el puerto publicado al ejecutar desde el IDE |
| `DB_USERNAME` | `postgres` | Usuario de PostgreSQL |
| `DB_PASSWORD` | Sin valor predeterminado | Contraseña de PostgreSQL |

Desde esta carpeta, con las variables configuradas:

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

Verificar el arranque y la conexión a PostgreSQL:

```bash
curl -i http://localhost:8082/actuator/health
```

Se espera HTTP 200 y `{"status":"UP"}`. Actuator incluye la comprobación
de conectividad del datasource. El esquema debe existir previamente;
Hibernate valida las tablas mapeadas sin crear ni modificar el esquema.

La aplicación incluye `com.fulvo.common` en el escaneo de componentes para
integrar el manejador de errores y el filtro de correlación de la librería común.
Los logs incluyen el valor MDC `correlationId`.

Los endpoints de negocio y sus entidades se incorporan en las siguientes
subsecciones, una vez superado el Gate 1.
