FROM openjdk:16

EXPOSE 8080

ARG JAR_FILE

ADD target/${JAR_FILE} spring-int-testing-demo.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/spring-int-testing-demo.jar"]
