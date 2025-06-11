# Build stage
FROM eclipse-temurin:23-jdk-alpine as builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src
RUN chmod +x mvnw
RUN ./mvnw clean package

# Run stage
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/AgilityAccess-1.0-SNAPSHOT.jar app.jar
ENV PORT=8080
EXPOSE ${PORT}
ENTRYPOINT ["java", "-Dport=${PORT}", "-jar", "app.jar"]
