# 1단계: Build stage (Gradle bootJar, 래퍼 없이 이미지에 포함된 gradle 사용)
FROM gradle:8.10.2-jdk17 AS builder
USER root
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle bootJar --no-daemon -x test \
    && BOOT_JAR="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" \
    && test -n "$BOOT_JAR" \
    && cp "$BOOT_JAR" /app/app.jar

# 2단계: Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/app.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
