# Build stage
FROM maven:3.9.6-eclipse-temurin-8 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

# Run stage
FROM eclipse-temurin:8-jre
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m"
WORKDIR /app

# create dirs
RUN mkdir -p /app/uploads /app/logs

COPY --from=builder /workspace/target/printercloud-backend-1.0.0.jar /app/app.jar

EXPOSE 8080

# Use docker profile by default
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

