# 1. Build stage: dùng Maven + JDK 21 để build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# copy pom.xml trước để cache dependency (tối ưu tốc độ build)
COPY pom.xml .
RUN mvn dependency:go-offline

# copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Run stage: runtime image nhẹ hơn
FROM eclipse-temurin:21-jdk
WORKDIR /app

# copy file jar từ stage build
COPY --from=build /app/target/*.jar app.jar

# mở port 8080 (Render sẽ map port động, nhưng khai báo cho rõ)
EXPOSE 8080

# chạy Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]