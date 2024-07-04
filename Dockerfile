# Use the official Maven image as the base image
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file to the working directory
COPY pom.xml .

# Copy the entire source code to the working directory
COPY src ./src

# Build the application using Maven
RUN mvn clean package -DskipTests

# Use the official OpenJDK 17 image as the base image for running the application
FROM openjdk:17-jdk-slim AS runtime

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled application JAR file from the build stage to the runtime image
COPY --from=build /app/target/*.jar app.jar

# Expose the port that the Spring Boot application will run on
EXPOSE 8081

# Define the command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
