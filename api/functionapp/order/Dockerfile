FROM maven:3.9.9-eclipse-temurin-11 AS build
WORKDIR /workspace/app

# Copy Maven descriptor and resolve dependencies
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

# Copy source code and build the application
COPY src ./src
RUN mvn -q -B package -DskipTests


FROM eclipse-temurin:11-jre
WORKDIR /app

ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE="prod" \
    SERVER_PORT="8080"

COPY --from=build /workspace/app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
