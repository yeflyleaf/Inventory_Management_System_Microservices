#!/bin/bash
# ==================================================
# Kubernetes One-Click Deploy Script (Linux / macOS)
# Usage: chmod +x deploy-k8s.sh && ./deploy-k8s.sh
# ==================================================
set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

NAMESPACE="inventory-system"
SERVICES=("user-service" "product-service" "order-service" "gateway-service")
MAX_WAIT=180

# ---- Functions ----
log_info()  { echo -e "${CYAN}[INFO]  $*${NC}"; }
log_warn()  { echo -e "${YELLOW}[WARN]  $*${NC}"; }
log_ok()    { echo -e "${GREEN}[OK]    $*${NC}"; }
log_fail()  { echo -e "${RED}[FAIL]  $*${NC}"; exit 1; }

check_command() {
    command -v "$1" >/dev/null 2>&1 || log_fail "Command not found: $1"
}

# ---- Pre-check ----
log_info "Checking environment..."
check_command docker
check_command kubectl
check_command java
log_ok "Environment check passed."

# ---- Step 1: Maven Build ----
log_info "Step 1/5: Maven building JARs..."
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    ./mvnw clean package -DskipTests -q
elif command -v mvn >/dev/null 2>&1; then
    mvn clean package -DskipTests -q
else
    log_fail "Maven not found (mvnw or mvn)."
fi
log_ok "Maven build success."

# ---- Step 2: Docker Build (Backend) ----
log_info "Step 2/5: Building Backend Docker images..."
# 这里假设后端 Dockerfile 在各服务目录下，或者在当前目录并接受 JAR_FILE 参数
# 鉴于之前脚本能运行，我们保持原样逻辑但增加错误检查
for service in "${SERVICES[@]}"; do
    JAR_FILE="${service}/target/*.jar"
    if ! ls ${JAR_FILE} 1>/dev/null 2>&1; then
        log_fail "JAR file not found for ${service}"
    fi
    log_info "Building image: ${service}:1.0"
    # 使用目录下现有的 Dockerfile
    docker build --build-arg JAR_FILE="${JAR_FILE}" -t "${service}:1.0" -f Dockerfile . --quiet
    log_ok "Image ${service}:1.0 ready."
done

# ---- Step 3: Docker Build (Frontend) ----
log_info "Step 3/5: Building Frontend Docker image..."
log_info "Building image: frontend:1.0"
docker build -t "frontend:1.0" ../Frontend --quiet
log_ok "Image frontend:1.0 ready."

# ---- Step 4: K8s Deploy ----
log_info "Step 4/5: Applying K8s resources..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/middleware.yaml

# ---- Step 5: Wait and Deploy Services ----
log_info "Step 5/5: Waiting for middleware..."
MIDDLEWARE_APPS=("nacos" "redis" "seata")

for app in "${MIDDLEWARE_APPS[@]}"; do
    log_info "Waiting for ${app} to be ready..."
    if kubectl -n ${NAMESPACE} wait --for=condition=ready pod -l app=${app} --timeout=${MAX_WAIT}s 2>/dev/null; then
        log_ok "${app} is ready."
    else
        log_warn "${app} timeout, continuing anyway..."
    fi
done

log_info "Deploying all services (Business + Frontend)..."
for service in "${SERVICES[@]}"; do
    kubectl apply -f "k8s/${service}.yaml"
    log_ok "Applied: ${service}"
done

# 部署前端
kubectl apply -f k8s/frontend.yaml
log_ok "Applied: frontend"

# ---- Finished ----
echo ""
log_info "=========================================="
log_ok   "All-in-One Deployment Finished!"
log_info "=========================================="
echo "Frontend URL: http://localhost:30080"
echo "Gateway URL:  http://localhost:30088"
echo "Status:       kubectl -n ${NAMESPACE} get pods"
echo ""
log_warn "Note: Ensure your host MySQL is running and accessible via host.docker.internal"
