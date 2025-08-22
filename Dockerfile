# 1. Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Run stage (runtime image nhẹ hơn)
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# chạy Spring Boot
ENTRYPOINT ["java","-jar","app.jar"]