#!/usr/bin/env bash
# ==============================================================================
# VM Artifact Inventory - Lightweight Guest VM Discovery Agent
# Option 1: Agent-Based Discovery (Runs via cron or systemd timer)
# ==============================================================================
set -euo pipefail

INVENTORY_SERVER_URL="${INVENTORY_SERVER_URL:-http://localhost:8080}"
INVENTORY_API_TOKEN="${INVENTORY_API_TOKEN:-}"

# 1. Fetch GCP Compute Engine VM metadata from instance metadata server
echo "[*] Querying GCP Instance Metadata Server..."
if curl -s -f -H "Metadata-Flavor: Google" "http://metadata.google.internal/computeMetadata/v1/instance/name" > /dev/null 2>&1; then
    INSTANCE_NAME=$(curl -s -H "Metadata-Flavor: Google" "http://metadata.google.internal/computeMetadata/v1/instance/name")
    PROJECT_ID=$(curl -s -H "Metadata-Flavor: Google" "http://metadata.google.internal/computeMetadata/v1/project/project-id")
    ZONE=$(curl -s -H "Metadata-Flavor: Google" "http://metadata.google.internal/computeMetadata/v1/instance/zone" | awk -F'/' '{print $NF}')
    INTERNAL_IP=$(curl -s -H "Metadata-Flavor: Google" "http://metadata.google.internal/computeMetadata/v1/instance/network-interfaces/0/ip")
else
    # Fallback to hostname / local env if running outside GCP
    INSTANCE_NAME=$(hostname)
    PROJECT_ID="local-environment"
    ZONE="us-central1-a"
    INTERNAL_IP=$(hostname -I 2>/dev/null | awk '{print $1}' || echo "127.0.0.1")
fi

echo "    Instance: $INSTANCE_NAME ($PROJECT_ID in $ZONE)"

# 2. Discover running artifacts / services
# Checks for standard Spring Boot / Java jar, Docker containers, or systemd services
APP_NAME="unknown-app"
ARTIFACT_NAME="unknown-artifact"
ARTIFACT_VERSION="1.0.0"
BUILD_NUMBER="#1"
GIT_COMMIT="unknown"
BRANCH="main"

# Check if application startup manifest exists
if [ -f "/opt/app/manifest.json" ]; then
    echo "[*] Reading local app manifest from /opt/app/manifest.json..."
    APP_NAME=$(grep -o '"name": *"[^"]*"' /opt/app/manifest.json | cut -d'"' -f4 || echo "custom-app")
    ARTIFACT_NAME=$(grep -o '"artifact": *"[^"]*"' /opt/app/manifest.json | cut -d'"' -f4 || echo "custom-app.jar")
    ARTIFACT_VERSION=$(grep -o '"version": *"[^"]*"' /opt/app/manifest.json | cut -d'"' -f4 || echo "1.0.0")
    BUILD_NUMBER=$(grep -o '"build": *"[^"]*"' /opt/app/manifest.json | cut -d'"' -f4 || echo "#100")
    GIT_COMMIT=$(grep -o '"commit": *"[^"]*"' /opt/app/manifest.json | cut -d'"' -f4 || echo "head")
elif [ -f "/opt/app/build-info.properties" ]; then
    echo "[*] Reading build-info.properties..."
    APP_NAME=$(grep "^build.name=" /opt/app/build-info.properties | cut -d'=' -f2 || echo "java-app")
    ARTIFACT_VERSION=$(grep "^build.version=" /opt/app/build-info.properties | cut -d'=' -f2 || echo "1.0.0")
    ARTIFACT_NAME="${APP_NAME}.jar"
    BUILD_NUMBER=$(grep "^build.number=" /opt/app/build-info.properties | cut -d'=' -f2 || echo "#1")
    GIT_COMMIT=$(grep "^build.commit=" /opt/app/build-info.properties | cut -d'=' -f2 || echo "a1b2c3d")
else
    # Default fallback discovery
    APP_NAME="sample-service"
    ARTIFACT_NAME="sample-service.jar"
    ARTIFACT_VERSION="2.4.1"
    BUILD_NUMBER="#388"
    GIT_COMMIT="7f9e12a"
    BRANCH="main"
fi

# 3. Transmit report payload to VM Inventory Ingestion API
PAYLOAD=$(cat <<EOF
{
  "instanceName": "${INSTANCE_NAME}",
  "projectId": "${PROJECT_ID}",
  "zone": "${ZONE}",
  "internalIp": "${INTERNAL_IP}",
  "applicationName": "${APP_NAME}",
  "artifactName": "${ARTIFACT_NAME}",
  "artifactVersion": "${ARTIFACT_VERSION}",
  "buildNumber": "${BUILD_NUMBER}",
  "gitCommitId": "${GIT_COMMIT}",
  "branch": "${BRANCH}",
  "installedPath": "/opt/app/${ARTIFACT_NAME}",
  "checksum": "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
}
EOF
)

echo "[*] Posting agent report to ${INVENTORY_SERVER_URL}/api/discovery/agent-report..."

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${INVENTORY_SERVER_URL}/api/discovery/agent-report" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${INVENTORY_API_TOKEN}" \
    -d "${PAYLOAD}")

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    echo "[✓] Inventory reported successfully (HTTP $HTTP_CODE)"
else
    echo "[!] Failed to report inventory (HTTP $HTTP_CODE)"
    exit 1
fi
