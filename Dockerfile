# Giai đoạn 1: Dùng Maven để Build code ra file .jar
FROM maven:3.4.12-openjdk-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Giai đoạn 2: Dùng Java để chạy file .jar
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/project3-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]