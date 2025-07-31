FROM openjdk:17-jdk-slim

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar


CMD ["/bin/sh", "-c", "java -jar app.jar"]