# Stage 1: Build stage
FROM gradle:8.3.3-jdk17 AS build

# Set working directory inside the container
WORKDIR /app

# Copy only the necessary files for caching dependencies
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# Download dependencies (cache layer)
RUN gradle --no-daemon buildNeeded

# Copy source code
COPY src ./src

# Build the application (adjust task if needed, e.g. assemble, build)
RUN gradle --no-daemon clean build -x test

# Stage 2: Run stage
FROM eclipse-temurin:17-jre-alpine

# Create a non-root user to run the app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create app directory
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appgroup /app/mini-url.jar

# Switch to non-root user
USER appuser

# Expose the port your app listens on (adjust as needed)
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java","-jar","mini-url.jar"]
