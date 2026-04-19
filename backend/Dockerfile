# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# Resolve dependencies offline
RUN ./mvnw dependency:go-offline || true

COPY src ./src
RUN ./mvnw clean package -DskipTests
RUN mkdir -p /app/build-result && cp target/backend-*.jar /app/build-result/app.jar

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build-result/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
