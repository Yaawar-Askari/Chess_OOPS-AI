# Chess Game - Docker Setup

This document explains how to run the Chess Game using Docker, which allows you to run the application on any system without installing Java.

## Prerequisites

- Docker installed on your system
- Docker Compose (usually comes with Docker Desktop)

## Quick Start

### 1. Build and Run with GUI (Linux/macOS)

```bash
# Build the Docker image
docker build -t chess-game .

# Run with GUI (requires X11 forwarding)
docker run -it --rm \
  -e DISPLAY=$DISPLAY \
  -v /tmp/.X11-unix:/tmp/.X11-unix \
  -p 8080:8080 \
  chess-game
```

### 2. Run with Docker Compose

```bash
# Build and run with GUI
docker-compose up --build

# Run in background
docker-compose up -d --build
```

### 3. Headless Server Mode

```bash
# Run only the server (no GUI)
docker-compose --profile server-only up --build
```

## Windows Setup

### Option 1: Using WSL2 (Recommended)

1. Install WSL2 and Docker Desktop for Windows
2. Follow the Linux instructions above

### Option 2: Using X Server

1. Install an X Server for Windows (e.g., VcXsrv, Xming)
2. Start the X Server
3. Set the DISPLAY environment variable:
   ```cmd
   set DISPLAY=localhost:0.0
   ```
4. Run the Docker container:
   ```cmd
   docker run -it --rm -e DISPLAY=host.docker.internal:0.0 -p 8080:8080 chess-game
   ```

### Option 3: Headless Mode (No GUI)

```cmd
# Run only the server component
docker run -it --rm -p 8080:8080 chess-game java -jar target/chess-game-1.0.0.jar server
```

## Usage

### GUI Mode
- The application will start with a main menu
- Choose from Local Game, AI Game, Host Game, or Join Game
- Use the GUI to play chess

### Server Mode
- The server runs on port 8080
- Other players can connect using your IP address and port 8080
- No GUI is displayed

## Troubleshooting

### GUI Not Displaying
- Ensure X11 forwarding is properly configured
- Check that the DISPLAY environment variable is set
- On Windows, make sure your X Server is running

### Port Already in Use
- Change the port mapping in docker-compose.yml:
  ```yaml
  ports:
    - "8081:8080"  # Use port 8081 instead of 8080
  ```

### Build Errors
- Ensure all source files are present in the project directory
- Check that the Dockerfile is in the project root
- Try rebuilding: `docker build --no-cache -t chess-game .`

## Development

### Rebuilding After Changes
```bash
# Rebuild the image after code changes
docker build -t chess-game .

# Or use docker-compose
docker-compose build
```

### Accessing Container Shell
```bash
# Run container with shell access
docker run -it --rm chess-game /bin/bash
```

## Network Play

### Hosting a Game
1. Start the container in server mode
2. Note your IP address
3. Other players connect to `your-ip:8080`

### Joining a Game
1. Start the container in client mode
2. Enter the host's IP address when prompted

## File Persistence

To save games between container runs, mount a volume:

```bash
docker run -it --rm \
  -v $(pwd)/saves:/app/saves \
  -e DISPLAY=$DISPLAY \
  -v /tmp/.X11-unix:/tmp/.X11-unix \
  chess-game
```

This will persist saved games in the `./saves` directory on your host system. 