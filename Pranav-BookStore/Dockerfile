# Stage 1: Build 

WORKDIR /app

# ── Cache layer 1: copy only dependency descriptors first
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw

# Resolve all dependencies without compiling source (cache-friendly).
RUN ./mvnw dependency:go-offline -B -q

# ── Cache layer 2: copy source and build the fat JAR.
COPY src/ src/

RUN ./mvnw package -B -q -DskipTests

#  Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for security best practice.
RUN addgroup -S bookstore && adduser -S bookstore -G bookstore

# Copy only the fat JAR from the builder stage – no Maven, no source code.
COPY --from=builder /app/target/*.jar app.jar

# Copy the data directory so CSV/JSON files are available inside the container.
COPY --from=builder /app/src/main/java/com/app/BookStore/data/ \
     /app/src/main/java/com/app/BookStore/data/

# Give ownership to the non-root user
RUN chown -R bookstore:bookstore /app

USER bookstore

# Expose the default Spring Boot port
EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
