#!/bin/bash

# Configuration
REGISTRY_USER="rfm9300"
PROJECT_PREFIX="hillsong-pt"
TAG="latest"

# Services to build
SERVICES=("auth-service" "springboot-api" "nextjs-admin-panel")
IMAGE_NAMES=("auth-service" "springboot-api" "admin-panel")

echo "Starting build and push process for $REGISTRY_USER..."

for i in "${!SERVICES[@]}"; do
    SERVICE=${SERVICES[$i]}
    IMAGE_NAME=${IMAGE_NAMES[$i]}
    FULL_IMAGE_NAME="$REGISTRY_USER/${PROJECT_PREFIX}-$IMAGE_NAME:$TAG"
    
    echo "---------------------------------------------------"
    echo "Building $SERVICE as $FULL_IMAGE_NAME..."
    echo "---------------------------------------------------"
    
    docker build --platform linux/amd64 -t "$FULL_IMAGE_NAME" "./$SERVICE"
    
    if [ $? -eq 0 ]; then
        echo "Successfully built $FULL_IMAGE_NAME"
        echo "Pushing $FULL_IMAGE_NAME..."
        docker push "$FULL_IMAGE_NAME"
    else
        echo "Failed to build $SERVICE"
        exit 1
    fi
done

echo "---------------------------------------------------"
echo "All images built and pushed successfully!"
echo "---------------------------------------------------"
echo ""
echo "To deploy on your server:"
echo "1. Copy 'docker-compose.prod.yml' to the server."
echo "2. Create a '.env' file based on '.env.prod.example'."
echo "3. Run: docker compose -f docker-compose.prod.yml pull"
echo "4. Run: docker compose -f docker-compose.prod.yml up -d"
