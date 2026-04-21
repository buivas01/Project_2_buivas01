#!/bin/bash
set -e

echo "=== Step 1: Compile project ==="
./mvnw compile -B

echo "=== Step 2: Train model ==="
./mvnw exec:java -Dexec.mainClass="ch.zhaw.deeplearningjava.footwear.Training" -B

echo "=== Step 3: Build Docker image ==="
docker-compose build --no-cache

echo "=== Step 4: Restart service ==="
docker-compose up -d

echo "=== Deployment complete. Service running on http://localhost:8081 ==="
