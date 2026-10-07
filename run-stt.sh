#!/bin/bash

set -e

start_backend() {
    echo "Compiling backend..."
    mvn clean package -DskipTests

    echo "Starting backend with Docker Compose..."
    docker compose up --build -d

    echo "Backend started."
}

start_frontend() {
    echo "Starting React frontend..."

    cd ../stt-frontend

    docker rm -f stt-frontend 2>/dev/null || true

    docker build -t stt-frontend:latest .

    docker run -d \
    -p 8081:8081 \
    --name stt-frontend \
    -e VITE_KEYCLOAK_URL="http://localhost:8088" \
    -e VITE_PUBLICATION_API_URL="http://localhost:8080" \
    -e VITE_APP_URL="http://localhost:8081/rag" \
    stt-frontend:latest

    echo "Frontend started."
}

case "$1" in
    backend)
        start_backend
        ;;
    frontend)
        start_frontend
        ;;
    all|"")
        start_backend
        start_frontend
        ;;
    *)
        echo "Usage: $0 [backend|frontend|all]"
        exit 1
        ;;
esac
