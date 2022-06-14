# 스프링부트가 java8에서 실행됨
FROM openjdk:8

RUN apt-get update

ARG JAR_FILE=build/libs/*.jar

RUN echo ${JAR_FILE}

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=production", "-jar","/app.jar"]
