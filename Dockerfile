FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/datamesh-manager-connector-hive-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]