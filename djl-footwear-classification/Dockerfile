FROM eclipse-temurin:25-jdk-noble

WORKDIR /usr/src/app

COPY .mvn .mvn
COPY pom.xml mvnw ./

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
COPY models models

RUN ./mvnw -Dmaven.test.skip=true package -B

EXPOSE 8081

CMD ["java", "-jar", "target/djl-footwear-classification-0.0.1-SNAPSHOT.jar"]
