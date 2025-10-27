FROM eclipse-temurin:17
WORKDIR workspace

ARG JAR_FRILE=build/libs/*.jar
copy ${JAR_FRILE} catalog-service.jar

ENTRYPOINT ["java","-jar","catalog-service.jar"]