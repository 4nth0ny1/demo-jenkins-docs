# Build a minimal image to run the Spring Boot jar
FROM eclipse-temurin:21-jre-alpine

# Working directory inside the container
WORKDIR /app

# Copy the Spring Boot jar from the Jenkins/Maven build
# This path must match the jar your Package stage creates
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app listens on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
