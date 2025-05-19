# Use an official JDK base image
FROM openjdk:17-jdk-slim

# Set workdir
WORKDIR /app

# Copy Maven build artifacts
COPY ./target/app.jar app.jar

# Expose the app port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
