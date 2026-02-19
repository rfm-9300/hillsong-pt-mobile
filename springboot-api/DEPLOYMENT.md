# Backend Deployment Guide (Container Registry)

This guide helps you deploy the Spring Boot API to your VPS by building the image locally, pushing it to a registry (Docker Hub, GitHub Container Registry, etc.), and pulling it on the VPS.

## Prerequisites

1.  **Local Machine**: Docker installed and logged in to your registry.
2.  **VPS**: Docker and Docker Compose installed.

## 1. Build and Push (Local Machine)

First, choose your image name (e.g., `yourusername/church-api`).

```bash
# Navigate to the API directory
cd springboot-api

# 1. Login to your registry (if needed)
docker login

# 2. Build the image for linux/amd64 (standard VPS architecture)
# Change 'yourusername/church-api' to your actual image name
docker build --platform linux/amd64 -t yourusername/church-api:latest .

# 3. Push the image
docker push yourusername/church-api:latest
```

## 2. Prepare VPS

You only need a few files on the VPS now. Create a directory (e.g., `~/church-api`) and upload:

1.  `docker-compose.yml`
2.  `.env` (Configure this based on `.env.example`)

You can copy `docker-compose.yml` via SCP:

```bash
scp springboot-api/docker-compose.yml user@your-vps-ip:~/church-api/
```

## 3. Configure Environment

On your VPS, create/edit the `.env` file:

```bash
cd ~/church-api
nano .env
```

Add your configuration AND the image name:

```properties
# ... other config ...
DOCKER_IMAGE_NAME=yourusername/church-api:latest
```

## 4. Deploy

Run Docker Compose. It will automatically pull the image specified in `.env`.

```bash
# Pull the latest image
docker-compose pull

# Start services
docker-compose up -d
```

## 5. Updating the App

To update the application in the future:

1.  **Local**: Build and push a new image tag.
2.  **VPS**:
    ```bash
    docker-compose pull
    docker-compose up -d
    ```
    This will recreate the container with the new image.

## Persistence

-   **Database**: Stored in `pg-volume` (Docker volume).
-   **Uploads**: Stored in `./uploads` on the VPS.
