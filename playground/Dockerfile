# Usage
# docker build -t mosazhaw/djl-sentiment-analysis .
# docker run --name djl-sentiment-analysis -p 9000:8080 -d mosazhaw/djl-sentiment-analysis

FROM eclipse-temurin:25-jdk-noble

# Copy Files
WORKDIR /usr/src/app
COPY src src
COPY .mvn .mvn
COPY pom.xml mvnw ./

# Install
RUN sed -i 's/\r$//' mvnw
RUN chmod +x mvnw
RUN ./mvnw -Dmaven.test.skip=true package

# Docker Run Command
EXPOSE 8080
CMD ["java","-jar","/usr/src/app/target/playground-0.0.1-SNAPSHOT.jar"]
