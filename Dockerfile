FROM maven:4.0.0-rc-5-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn clean package -am -DskipTests

FROM eclipse-temurin:17.0.18_8-jdk
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]