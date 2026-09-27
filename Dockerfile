# ---------- Build stage ----------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy wrapper and pom first for better layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw clean package -DskipTests -B

# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre

WORKDIR /app

# Create a non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/logs && chown -R spring:spring /app/logs

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]