# VM & Artifact Inventory System — Deployment & Operations Guide

This guide details deployment options for the VM & Artifact Inventory System, covering local Docker Compose, production Kubernetes, Helm deployments, and GCP Service Account credential configurations.

---

## 1. Prerequisites

- **Docker**: Engine version 24.0+ and Docker Compose v2+
- **Kubernetes**: v1.26+ (GKE, Minikube, or Kind)
- **Helm**: v3.12+ (if deploying via Helm)
- **kubectl**: v1.26+
- **Java**: JDK 21+ (if running backend locally outside Docker)
- **Node.js**: v20+ and npm (if running frontend locally outside Docker)

---

## 2. Default Seeded Credentials

When the system boots in development or test mode, default role-based accounts are pre-seeded:

| Username | Password | Assigned Role | Capabilities |
|---|---|---|---|
| `admin` | `Password123!` | `ROLE_ADMIN` | Full administrative control, user role management, deletions |
| `sre_lead` | `Password123!` | `ROLE_SRE` | Projects/VM management, manual sync triggers, deployments |
| `dev_lead` | `Password123!` | `ROLE_DEVELOPER` | Artifact deployments ingestion, inventory viewing |
| `viewer` | `Password123!` | `ROLE_VIEWER` | Read-only access to dashboard, inventory, and reports |

---

## 3. Local Deployment via Docker Compose

Docker Compose is the recommended mechanism for quick local validation, developer environments, and CI testing.

### 3.1 Launch Stack
In the project root directory:
```bash
# Build and start all 3 containers in detached mode
docker compose up -d --build
```

### 3.2 Containers Created
- `vm-inventory-db`: PostgreSQL 15 on port `5442` (internal `5432`)
- `vm-inventory-backend`: Spring Boot 3.3.5 API on port `8080`
- `vm-inventory-frontend`: Nginx + React 19 UI on port `3000`

### 3.3 Verify Container Status
```bash
docker compose ps
```
Both backend and database have built-in Docker healthchecks (`curl -f http://localhost:8080/actuator/health` and `pg_isready`).

### 3.4 Access the Application
- **Frontend Dashboard**: Open [http://localhost:3000](http://localhost:3000) in your browser.
- **Backend Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **API Base**: [http://localhost:8080/api](http://localhost:8080/api)

### 3.5 Stop Stack
```bash
# Stop and remove containers and network
docker compose down

# Stop and delete persistent database volume
docker compose down -v
```

---

## 4. Kubernetes Deployment (Raw Manifests)

The `kubernetes/` directory contains standard YAML manifests for container orchestration.

### 4.1 Apply Manifests in Sequence
```bash
# 1. Create Namespace
kubectl create namespace vm-inventory

# 2. Apply ConfigMap and Secrets
kubectl apply -f kubernetes/configmap.yaml -n vm-inventory

# 3. Deploy PostgreSQL StatefulSet & Service
kubectl apply -f kubernetes/postgres-statefulset.yaml -n vm-inventory

# Wait for PostgreSQL to be ready
kubectl rollout status statefulset/postgres -n vm-inventory

# 4. Deploy Spring Boot Backend
kubectl apply -f kubernetes/backend-deployment.yaml -n vm-inventory
kubectl rollout status deployment/backend -n vm-inventory

# 5. Deploy React Frontend
kubectl apply -f kubernetes/frontend-deployment.yaml -n vm-inventory
kubectl rollout status deployment/frontend -n vm-inventory

# 6. Apply Ingress (optional, for domain routing)
kubectl apply -f kubernetes/ingress.yaml -n vm-inventory
```

### 4.2 Port Forwarding for Local Kubernetes Access
If running in Minikube or Kind without an Ingress controller:
```bash
# Forward Frontend UI to localhost:3000
kubectl port-forward svc/frontend 3000:80 -n vm-inventory

# Forward Backend API to localhost:8080
kubectl port-forward svc/backend 8080:8080 -n vm-inventory
```

---

## 5. Helm Chart Deployment

A production-grade Helm chart is provided under `helm/vm-inventory/`.

### 5.1 Inspect Default Values
Review `helm/vm-inventory/values.yaml` for replica counts, resource limits, image tags, and database settings.

### 5.2 Install or Upgrade Chart
```bash
# Install release in 'vm-inventory' namespace
helm upgrade --install vm-inventory ./helm/vm-inventory \
    --namespace vm-inventory \
    --create-namespace \
    --set backend.image.tag=latest \
    --set frontend.image.tag=latest

# Verify Helm release
helm list -n vm-inventory
helm status vm-inventory -n vm-inventory
```

### 5.3 Uninstall
```bash
helm uninstall vm-inventory -n vm-inventory
```

---

## 6. Google Cloud Platform (GCP) Configuration

To enable live synchronization against Google Cloud APIs rather than mock data:

### 6.1 Required IAM Permissions
The application service account requires:
- `roles/resourcemanager.organizationViewer` or `roles/resourcemanager.folderViewer` (to discover projects)
- `roles/compute.viewer` (to list Compute Engine instances and metadata across projects)
- `roles/secretmanager.secretAccessor` (optional, if storing DB passwords in GCP Secret Manager)

### 6.2 Service Account Key Configuration
1. Generate a Service Account key in JSON format:
   ```bash
   gcloud iam service-accounts keys create sa-key.json \
       --iam-account=vm-inventory-sa@YOUR_PROJECT.iam.gserviceaccount.com
   ```
2. For Docker Compose, mount the key into the backend container and set:
   ```yaml
   environment:
     - GOOGLE_APPLICATION_CREDENTIALS=/app/credentials/sa-key.json
   volumes:
     - ./sa-key.json:/app/credentials/sa-key.json:ro
   ```
3. For Kubernetes, store the key in a Kubernetes Secret and mount as a volume:
   ```bash
   kubectl create secret generic gcp-sa-key \
       --from-file=sa-key.json=./sa-key.json \
       -n vm-inventory
   ```

---

## 7. Environment Variables Reference

| Variable | Description | Default Value |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Spring profile (`dev`, `prod`, `docker`) | `dev` |
| `SPRING_DATASOURCE_URL` | JDBC connection string | `jdbc:postgresql://localhost:5442/vminventory` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username | `vminventory` |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password | `vminventory_secret` |
| `JWT_SECRET` | 256-bit secret string for signing JWT tokens | `vmInventorySuperSecretKeyForJwtSigningMustBe256BitsLong12345!` |
| `JWT_EXPIRATION_MS` | JWT validity in milliseconds | `86400000` (24 hours) |
| `GCP_SYNC_ENABLED` | Enable automatic background sync scheduler | `true` |
| `GCP_SYNC_FIXED_RATE_MS` | Synchronization period in milliseconds | `300000` (5 minutes) |
| `VITE_API_URL` | Frontend API reverse proxy endpoint | `/api` |

---

## 8. Troubleshooting & Verification

### Actuator Health Endpoint
```bash
curl -i http://localhost:8080/actuator/health
```
Expected output:
```json
{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
```

### Smoke Test Auth & Data
```bash
# 1. Login and extract token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Password123!"}' | jq -r '.data.token')

# 2. Query Dashboard metrics
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/dashboard | jq .

# 3. Query VM instances
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/instances?size=2" | jq .
```
