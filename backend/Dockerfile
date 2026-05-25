# Stage 1: Build the application using Maven and JDK 17
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application jar file (skipping tests for speed)
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight runtime image using JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (standard Spring Boot port)
EXPOSE 8080

# Command to execute the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
