# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Install system dependencies including X11 for GUI
RUN apt-get update && \
    apt-get install -y \
    maven \
    xvfb \
    x11-apps \
    && rm -rf /var/lib/apt/lists/*

# Copy the Maven project files
COPY . /app

# Build the project
RUN mvn clean package -DskipTests

# Create a script to run the application with X11 forwarding
RUN echo '#!/bin/bash\n\
if [ "$DISPLAY" = "" ]; then\n\
    echo "No DISPLAY set, running in headless mode..."\n\
    java -jar target/chess-game-1.0.0.jar server\n\
else\n\
    echo "Starting chess game with GUI..."\n\
    java -jar target/chess-game-1.0.0.jar\n\
fi' > /app/run.sh && chmod +x /app/run.sh

# Expose the port for the server
EXPOSE 8080

# Set the default command
CMD ["/app/run.sh"] 