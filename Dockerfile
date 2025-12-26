# Giai đoạn 1: Build
# Sử dụng Maven 3.9 và JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Giai đoạn 2: Run
# Phải sử dụng JDK 21 (hoặc JRE 21) để chạy file .jar vừa build
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
