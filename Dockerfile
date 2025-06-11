# Use an official Maven image to build the application
FROM maven:3.8-openjdk-17-slim AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle

# Copy the source code
COPY src ./src

# Build the application
RUN ./gradlew build -x test

# Use an official OpenJDK runtime as a parent image for the final image
FROM openjdk:17-jdk-slim

# Set the working directory in the final image
WORKDIR /app

# Copy the packaged jar file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]