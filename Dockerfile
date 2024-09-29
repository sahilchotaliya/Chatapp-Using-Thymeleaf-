# Use the official OpenJDK image as the base image
FROM openjdk:22-slim

# Set the working directory in the container
WORKDIR /usr/src/chatapp

# Copy the JAR file into the container
COPY /target/web-0.0.1-SNAPSHOT.jar .

# Expose the port that the application runs on
EXPOSE 9292

# Command to run the application
CMD ["java", "-jar", "web-0.0.1-SNAPSHOT.jar"]