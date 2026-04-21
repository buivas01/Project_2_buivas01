FROM eclipse-temurin:25-jdk-noble

WORKDIR /usr/src/app

# Copy Maven wrapper and project descriptor first (layer cache optimisation)
COPY .mvn .mvn
COPY pom.xml mvnw ./

# Fix Windows line-endings if the file was committed on a Windows host
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download dependencies (cached as long as pom.xml does not change)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw -Dmaven.test.skip=true package -B

EXPOSE 8082

CMD ["java", "-jar", "target/consumer-0.0.1-SNAPSHOT.jar"]
