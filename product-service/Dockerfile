# Use JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

# Runtime image
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp

COPY --from=builder /workspace/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
