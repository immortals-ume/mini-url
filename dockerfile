# Stage 1: Build with JDK 21 + Gradle 8.13 installed manually
FROM eclipse-temurin:21-jdk AS build

ENV GRADLE_VERSION=8.13
ENV GRADLE_HOME=/opt/gradle/gradle-$GRADLE_VERSION
ENV PATH=${GRADLE_HOME}/bin:${PATH}

WORKDIR /app

# Install unzip and curl
RUN microdnf install -y unzip curl && microdnf clean all

# Download and install Gradle
RUN curl -fsSL https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip \
    && unzip gradle.zip -d /opt/gradle \
    && rm gradle.zip

# Copy gradle files for dependency caching
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# Download dependencies
RUN gradle buildNeeded --no-daemon

# Copy source code
COPY src ./src

# Build the application skipping tests
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime with JDK 21 runtime only
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN chown appuser:appgroup /app/app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "mini-url-1.0.0.jar"]