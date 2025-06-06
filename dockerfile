# -------- Stage 1: Build --------
FROM eclipse-temurin:21-jdk AS build

# Arguments and environment variables
ARG GRADLE_VERSION=8.13
ENV GRADLE_HOME=/opt/gradle/gradle-${GRADLE_VERSION} \
    PATH="/opt/gradle/gradle-${GRADLE_VERSION}/bin:${PATH}" \
    APP_HOME=/app

# Set working directory
WORKDIR ${APP_HOME}

# Install unzip and curl
RUN apt-get update && \
    apt-get install -y unzip curl && \
    rm -rf /var/lib/apt/lists/*

# Install Gradle manually for control and reproducibility
RUN curl -fsSL https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip && \
    unzip gradle.zip -d /opt/gradle && \
    rm gradle.zip

# Copy only the files needed to cache dependencies
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle build -x test --no-daemon

# Copy source code and build
COPY src ./src
RUN gradle clean build

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:21-jre-alpine

ENV APP_HOME=/app
WORKDIR ${APP_HOME}

# Create non-root user for running the app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR from build stage
COPY --from=build /app/build/libs/mini-url-1.0.1.jar mini-url.jar

# Set ownership and permissions
RUN chown appuser:appgroup mini-url.jar
USER appuser

# Expose application port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "mini-url.jar"]