# Build stage: produces the self-contained app.jar with the Gradle wrapper.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :app:buildFatJar --no-daemon --quiet

# Runtime stage: JRE only, non-root user.
FROM eclipse-temurin:21-jre
RUN useradd --system --create-home --uid 10001 app
WORKDIR /app
COPY --from=build /workspace/app/build/libs/app.jar app.jar
RUN mkdir -p /app/data/uploads && chown -R app:app /app
USER app
ENV STORAGE_ROOT=/app/data/uploads
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=3 \
    CMD curl --fail --silent http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
