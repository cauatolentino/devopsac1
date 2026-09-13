# ---------------------------------------------------------------------------
# Etapa 1 - build do jar com Maven
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# copia so o pom primeiro para aproveitar o cache de dependencias do Docker
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------------------------------
# Etapa 2 - imagem final, so com o JRE e o jar
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=postgres

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
