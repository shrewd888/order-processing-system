#!/bin/bash

echo "🛑 Stopping all services..."
docker compose down

echo "🚀 Starting Zookeeper..."
docker compose up -d zookeeper
sleep 5

echo "🚀 Starting Kafka..."
docker compose up -d kafka
echo "⏳ Waiting 30 seconds for Kafka to be ready..."
sleep 30

echo "🚀 Starting Postgres..."
docker compose up -d postgres
sleep 10

echo "🚀 Starting all services..."
docker compose up -d

echo "✅ Done! Checking status..."
docker compose ps

echo ""
echo "📋 Watch logs with: docker compose logs -f"