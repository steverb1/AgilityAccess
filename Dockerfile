FROM eclipse-temurin:23-jre-alpine

WORKDIR /app
COPY target/AgilityAccess-1.0-SNAPSHOT.jar app.jar

EXPOSE 7070
ENTRYPOINT ["java", "-jar", "app.jar"]
