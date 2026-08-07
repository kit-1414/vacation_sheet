FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY settings.gradle.kts build.gradle.kts ./
COPY vacation-sheet-service/build.gradle.kts vacation-sheet-service/build.gradle.kts
COPY vacation-sheet-service/src vacation-sheet-service/src
RUN chmod +x gradlew && ./gradlew :vacation-sheet-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /workspace/vacation-sheet-service/build/libs/*.jar app.jar
EXPOSE 8055
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
