FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/servicio-operaciones-partidos-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
