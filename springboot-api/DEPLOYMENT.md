# Backend Deployment Guide

This flow builds the Spring Boot API image locally, pushes it to a registry, and keeps the VPS limited to `docker compose pull` plus `docker compose up -d`.

## Prerequisites

Local machine:
- Docker installed
- Logged in to your registry

Server:
- Docker Engine installed
- Docker Compose v2 available as `docker compose`

## 1. Build and Push

Pick an image name, for example `youruser/hillsong-pt-api`, and tag each release explicitly.

```bash
cd springboot-api

export IMAGE_NAME=youruser/hillsong-pt-api
export IMAGE_TAG=0.0.86

docker build --platform linux/amd64 -t "$IMAGE_NAME:$IMAGE_TAG" -t "$IMAGE_NAME:latest" .
docker push "$IMAGE_NAME:$IMAGE_TAG"
docker push "$IMAGE_NAME:latest"
```

The image already includes the app jar and bundled static files under `/app/files`.

## 2. Prepare the Server

Copy only these files to the server:
- `springboot-api/docker-compose.yml`
- `springboot-api/.env.example` as the starting point for `.env`

Example:

```bash
scp springboot-api/docker-compose.yml user@your-server:~/hillsong-api/
scp springboot-api/.env.example user@your-server:~/hillsong-api/.env
```

## 3. Configure `.env` on the Server

Edit `~/hillsong-api/.env` and set at minimum:

```properties
DOCKER_IMAGE_NAME=youruser/hillsong-pt-api:0.0.86
PORT=8080
SPRING_PROFILES_ACTIVE=prod
IS_PRODUCTION=true
BASE_URL=https://api.your-domain.com

JWT_SECRET=replace_with_a_long_random_secret
JWT_ISSUER=https://api.your-domain.com
JWT_AUDIENCE=users

SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_FROM=noreply@your-domain.com

ADMIN_TOKEN=replace_with_a_long_random_admin_token

MINIO_ACCESS_KEY=replace_me
MINIO_SECRET_KEY=replace_me
MINIO_BUCKET=church-files
```

If you want to expose MinIO on different public ports, also set `MINIO_API_PORT` and `MINIO_CONSOLE_PORT`.

## 4. Deploy on the Server

```bash
cd ~/hillsong-api
docker compose pull
docker compose up -d
docker compose ps
```

The API container will start from the registry image, and MinIO plus the bucket bootstrap container will be created locally on the server.

## 5. Update to a New Release

For each release:

```bash
cd springboot-api

export IMAGE_NAME=youruser/hillsong-pt-api
export IMAGE_TAG=0.0.87

docker build --platform linux/amd64 -t "$IMAGE_NAME:$IMAGE_TAG" -t "$IMAGE_NAME:latest" .
docker push "$IMAGE_NAME:$IMAGE_TAG"
docker push "$IMAGE_NAME:latest"
```

Then on the server update `DOCKER_IMAGE_NAME` if you want the pinned tag, and redeploy:

```bash
cd ~/hillsong-api
docker compose pull
docker compose up -d
```

## Persistence

- App logs are stored in `./logs` on the server.
- File uploads are stored in `./uploads` on the server.
- MinIO object data is stored in the `minio_data` Docker volume.

## Direct Run Alternative

If you do not want Compose, you can still run the API image directly after pushing it:

```bash
docker pull youruser/hillsong-pt-api:0.0.86
docker run -d \
  --name backend-springboot \
  --restart unless-stopped \
  --env-file .env \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e UPLOAD_PATH=/app/uploads \
  -p 8080:8080 \
  -v "$(pwd)/logs:/app/logs" \
  -v "$(pwd)/uploads:/app/uploads" \
  youruser/hillsong-pt-api:0.0.86
```
