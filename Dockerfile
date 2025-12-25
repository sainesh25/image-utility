# Multi-stage build for Railway deployment

# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-11 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime with Tomcat
FROM tomcat:9.0-jdk11-temurin-jammy

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR file from builder stage
COPY --from=builder /app/target/image-utility.war /usr/local/tomcat/webapps/ROOT.war

# Expose port (Railway will override this with $PORT)
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
