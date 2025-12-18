FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Create non-root user and a writable data directory for SQLite
RUN addgroup -S app \
 && adduser -S app -G app \
 && mkdir -p /app/data \
 && chown -R app:app /app

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
