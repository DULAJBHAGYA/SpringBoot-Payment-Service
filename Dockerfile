# Stage 1: Build
FROM maven:3.9.6-amazoncorretto-21-al2023 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Extract layers for better caching
FROM amazoncorretto:21-alpine AS builder
WORKDIR /builder
COPY --from=build /app/target/s4-payment-service-1.0.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Runtime (Optimized for AWS Lightsail - Alpine Linux)
FROM amazoncorretto:21-alpine
WORKDIR /s4it

# Create a non-root user for security (Alpine uses addgroup/adduser)
RUN addgroup -S s4group && adduser -S s4user -G s4group

# Copy layers from builder stage (better caching and smaller size)
COPY --from=builder /builder/dependencies/ ./
COPY --from=builder /builder/spring-boot-loader/ ./
COPY --from=builder /builder/snapshot-dependencies/ ./
COPY --from=builder /builder/application/ ./

# Change ownership to non-root user
RUN chown -R s4user:s4group /s4it

# Switch to non-root user
USER s4user

# Port for the Payment service
EXPOSE 8002

# Start the application using Spring Boot's layered jar format
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]