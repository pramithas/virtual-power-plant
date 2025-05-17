# Stage 1: Build the app
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /app/vpp-web/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
