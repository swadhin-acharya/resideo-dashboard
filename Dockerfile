FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml ./
COPY resideo-dashboard-core/pom.xml resideo-dashboard-core/
COPY resideo-dashboard-client/pom.xml resideo-dashboard-client/
COPY resideo-dashboard-standalone/pom.xml resideo-dashboard-standalone/
RUN mvn dependency:go-offline -B -pl resideo-dashboard-standalone -am || true
COPY . .
RUN mvn package -DskipTests -B -q -pl resideo-dashboard-standalone -am

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/resideo-dashboard-standalone/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
