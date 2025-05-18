FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the whole project
COPY . .

# Download dependencies and compile but skip tests to speed build
RUN ./mvnw clean install -DskipTests

# Default command runs the integration tests
CMD ["./mvnw", "verify"]
