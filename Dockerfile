FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

COPY --from=build /app/target/*.jar app.jar

ADD https://dtdg.co/latest-java-tracer dd-java-agent.jar

RUN chown appuser:appgroup app.jar dd-java-agent.jar

USER appuser

EXPOSE 8080

ENV TZ=America/Sao_Paulo

ENTRYPOINT ["java", "-javaagent:/app/dd-java-agent.jar", "-jar", "app.jar"]
