# Estágio 1: Build (Opcional se você rodar mvn package local)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Estágio 2: Runtime (O que vai para a AWS)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Criar usuário não-root por segurança
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copia o JAR gerado no estágio de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Limite de memória para evitar que o Fargate/App Runner mate o container
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]