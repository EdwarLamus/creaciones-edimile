# ─────────────────────────────────────────────────────────────────────────────
# Dockerfile — Creaciones Edimile (Spring Boot 3 / Java 17)
# Imagen multi-etapa: construye el JAR y luego crea una imagen mínima.
# ─────────────────────────────────────────────────────────────────────────────

# ── Etapa 1: construcción ─────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar solo el pom.xml primero para aprovechar el caché de capas de Maven
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn package -DskipTests -q

# ── Etapa 2: imagen final ─────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

# Crear directorio para uploads persistentes
RUN mkdir -p /app/uploads

# Copiar el JAR desde la etapa de construcción
COPY --from=build /app/target/creaciones-edimile-*.jar app.jar

# Puerto que expone la aplicación
EXPOSE 8080

# Arrancar la aplicación leyendo el PORT que asigna Cloud Run (default 8080)
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
