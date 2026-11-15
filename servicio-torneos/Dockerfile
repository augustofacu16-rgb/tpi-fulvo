FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/servicio-torneos-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
