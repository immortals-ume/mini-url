# -------- Stage 1: Build --------
FROM eclipse-temurin:21-jdk AS build

ENV GRADLE_VERSION=8.13 \
    GRADLE_HOME=/opt/gradle/gradle-${GRADLE_VERSION} \
    PATH=/opt/gradle/gradle-${GRADLE_VERSION}/bin:${PATH} \
    APP_HOME=/app

WORKDIR ${APP_HOME}

# Install unzip and curl for Gradle setup
RUN microdnf install -y unzip curl && microdnf clean all

# Install Gradle
RUN curl -fsSL https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip \
    && unzip gradle.zip -d /opt/gradle \
    && rm gradle.zip

# Cache Gradle configuration and dependencies
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle
RUN gradle buildNeeded --no-daemon

# Copy source code and build the JAR (excluding tests)
COPY src ./src
RUN gradle clean build -x test --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:21-jre-alpine

ENV APP_HOME=/app
WORKDIR ${APP_HOME}

# Add non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/mini-url-1.0.0.jar mini-url-1.0.0.jar

# Set file ownership and permissions
RUN chown appuser:appgroup mini-url-1.0.0.jar
USER appuser

# Expose app port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "mini-url-1.0.0.jar"]