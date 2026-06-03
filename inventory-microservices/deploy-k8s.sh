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
KUBE_CONTEXT="docker-desktop"
NACOS_NODE_PORT=30848
NACOS_PUBLISH_MAX_RETRIES=30
NACOS_PUBLISH_RETRY_INTERVAL=5
SERVICES=("user-service" "product-service" "order-service" "gateway-service")
MAX_WAIT=300

# ---- Functions ----
log_info()  { echo -e "${CYAN}[INFO]  $*${NC}"; }
log_warn()  { echo -e "${YELLOW}[WARN]  $*${NC}"; }
log_ok()    { echo -e "${GREEN}[OK]    $*${NC}"; }
log_fail()  { echo -e "${RED}[FAIL]  $*${NC}"; exit 1; }

check_command() {
    command -v "$1" >/dev/null 2>&1 || log_fail "Command not found: $1"
}

check_docker_ready() {
    if ! docker info >/dev/null 2>&1; then
        log_fail "Docker daemon is not reachable. Start Docker Desktop and wait until it is running."
    fi
}

check_no_running_mysql_container() {
    local mysql_containers
    mysql_containers=$(docker ps --format "{{.Image}}\t{{.Names}}" 2>/dev/null | grep -iE '(mysql|mariadb|percona)' || true)
    if [ -n "${mysql_containers}" ]; then
        log_fail "MySQL must run on the host, not in Docker. Stop these database containers first:\n${mysql_containers}"
    fi
    log_ok "No running MySQL/MariaDB/Percona containers detected."
}

check_kubernetes_ready() {
    local current_context
    current_context=$(kubectl config current-context 2>/dev/null || true)
    
    if [ -z "${current_context}" ]; then
        if kubectl config get-contexts -o name | grep -q "^${KUBE_CONTEXT}$"; then
            log_info "No kubectl context is selected. Switching to ${KUBE_CONTEXT}..."
            kubectl config use-context "${KUBE_CONTEXT}" >/dev/null
        else
            log_fail "Kubernetes context is not set and '${KUBE_CONTEXT}' does not exist. Enable Kubernetes in Docker Desktop first."
        fi
    elif [ "${current_context}" != "${KUBE_CONTEXT}" ]; then
        if kubectl config get-contexts -o name | grep -q "^${KUBE_CONTEXT}$"; then
            log_info "Switching kubectl context from '${current_context}' to ${KUBE_CONTEXT}..."
            kubectl config use-context "${KUBE_CONTEXT}" >/dev/null
        fi
    fi

    if ! kubectl cluster-info >/dev/null 2>&1; then
        log_fail "Kubernetes API is not reachable. In Docker Desktop, enable Kubernetes and wait until it is fully running."
    fi
}

check_no_embedded_mysql_manifests() {
    local files=()
    if [ -f "microservices-k8s.yaml" ]; then
        files+=("microservices-k8s.yaml")
    fi
    if [ -d "k8s" ]; then
        while IFS= read -r -d '' file; do
            files+=("$file")
        done < <(find k8s -name "*.yaml" -type f -print0)
    fi

    for file in "${files[@]}"; do
        if grep -q -i -E 'image:[[:space:]]*["'"'"']?(mysql|mariadb|percona)' "${file}" || \
           grep -q -i 'name:[[:space:]]*mysql-pvc' "${file}" || \
           (grep -q 'kind:[[:space:]]*Deployment' "${file}" && grep -q 'name:[[:space:]]*mysql' "${file}"); then
            log_fail "MySQL must stay on the host. Forbidden Kubernetes resource found in ${file}."
        fi
    done
    log_ok "Kubernetes manifests contain no embedded MySQL workload."
}

check_no_existing_k8s_mysql_resources() {
    if kubectl get namespace "${NAMESPACE}" >/dev/null 2>&1; then
        local existing
        existing=$(kubectl -n "${NAMESPACE}" get deployment,statefulset,pvc -o name 2>/dev/null | grep -iE 'mysql|mysql-pvc' || true)
        if [ -n "${existing}" ]; then
            log_fail "Existing MySQL resources were found in Kubernetes: ${existing}. Delete them before deploying because MySQL must stay on the host."
        fi

        local service_type
        service_type=$(kubectl -n "${NAMESPACE}" get service mysql-service -o jsonpath='{.spec.type}' 2>/dev/null || true)
        local external_name
        external_name=$(kubectl -n "${NAMESPACE}" get service mysql-service -o jsonpath='{.spec.externalName}' 2>/dev/null || true)
        if [ -n "${service_type}" ] && { [ "${service_type}" != "ExternalName" ] || [ "${external_name}" != "host.docker.internal" ]; }; then
            log_fail "Existing mysql-service is not an ExternalName to host.docker.internal. Delete it before deploying: kubectl -n ${NAMESPACE} delete service mysql-service"
        fi
    fi
    log_ok "No existing Kubernetes MySQL workload or PVC detected."
}

test_host_mysql_from_kubernetes() {
    local pod_name="mysql-host-check"
    log_info "Testing host MySQL connectivity from inside Kubernetes via mysql-service:3306..."
    kubectl -n "${NAMESPACE}" delete pod "${pod_name}" --ignore-not-found >/dev/null 2>&1 || true

    # Run busybox pod to probe port 3306
    if kubectl -n "${NAMESPACE}" run "${pod_name}" \
        --image=busybox:1.36 \
        --restart=Never \
        --rm \
        -i \
        --attach \
        --pod-running-timeout=120s \
        --command -- sh -c "nc -zvw5 mysql-service 3306" >/dev/null 2>&1; then
        log_ok "Kubernetes can reach host MySQL through mysql-service:3306."
    else
        kubectl -n "${NAMESPACE}" delete pod "${pod_name}" --ignore-not-found >/dev/null 2>&1 || true
        log_fail "Kubernetes pods cannot reach host MySQL at mysql-service:3306. Check that MySQL is running on the host, listening on 3306, allowed through Windows Firewall, and reachable via host.docker.internal."
    fi
}

publish_nacos_common_config() {
    local config_file="nacos-common-config.yaml"
    if [ ! -f "${config_file}" ]; then
        log_fail "Missing Nacos config file: ${config_file}"
    fi

    local uri="http://localhost:${NACOS_NODE_PORT}/nacos/v1/cs/configs"
    log_info "Publishing common.yaml to Nacos via ${uri}..."
    
    # Wait and retry to ensure Nacos application is fully ready to accept API requests
    local success=false
    for i in $(seq 1 ${NACOS_PUBLISH_MAX_RETRIES}); do
        if curl -s -f -X POST "${uri}" \
            -d "dataId=common.yaml" \
            -d "group=DEFAULT_GROUP" \
            --data-urlencode "content@${config_file}" | grep -q "true"; then
            success=true
            break
        fi
        log_info "Waiting for Nacos API to accept configuration ($i/${NACOS_PUBLISH_MAX_RETRIES})..."
        sleep ${NACOS_PUBLISH_RETRY_INTERVAL}
    done

    if [ "${success}" = true ]; then
        log_ok "Nacos common.yaml published."
    else
        log_fail "Failed to publish Nacos config after ${NACOS_PUBLISH_MAX_RETRIES} retries. Ensure nacos-service NodePort ${NACOS_NODE_PORT} is reachable."
    fi
}

# ---- Pre-check ----
log_info "Checking environment..."
check_command docker
check_command kubectl
check_command java
check_no_embedded_mysql_manifests
check_docker_ready
check_no_running_mysql_container
check_kubernetes_ready
check_no_existing_k8s_mysql_resources
log_ok "Environment check passed."

# ---- Step 1: Maven Build ----
log_info "Step 1/6: Maven building JARs..."
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
log_info "Step 2/6: Building Backend Docker images..."
for service in "${SERVICES[@]}"; do
    JAR_FILE="${service}/target/*.jar"
    if ! ls ${JAR_FILE} 1>/dev/null 2>&1; then
        log_fail "JAR file not found for ${service}"
    fi
    log_info "Building image: ${service}:1.0"
    docker build --build-arg JAR_FILE="${JAR_FILE}" -t "${service}:1.0" -f Dockerfile . --quiet
    log_ok "Image ${service}:1.0 ready."
done

# ---- Step 3: Docker Build (Frontend) ----
log_info "Step 3/6: Building Frontend Docker image..."
log_info "Building image: frontend:1.0"
docker build -t "frontend:1.0" ../Frontend --quiet
log_ok "Image frontend:1.0 ready."

# ---- Step 4: K8s Deploy ----
log_info "Step 4/6: Applying Kubernetes base resources..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/middleware.yaml
log_ok "Base resources applied."
test_host_mysql_from_kubernetes

# ---- Step 5: Wait and Publish Config ----
log_info "Step 5/6: Waiting for middleware..."
MIDDLEWARE_APPS=("nacos" "redis" "seata" "sentinel")

for app in "${MIDDLEWARE_APPS[@]}"; do
    log_info "Waiting for ${app} to be ready..."
    if kubectl -n ${NAMESPACE} wait --for=condition=ready pod -l app=${app} --timeout=${MAX_WAIT}s 2>/dev/null; then
        log_ok "${app} is ready."
    else
        kubectl -n ${NAMESPACE} get pods -l app=${app}
        log_fail "${app} did not become ready. Check logs with: kubectl -n ${NAMESPACE} logs -l app=${app}"
    fi
done

publish_nacos_common_config

# ---- Step 6: Deploy Services & Rollout Restart ----
log_info "Step 6/6: Deploying business services and frontend..."
for service in "${SERVICES[@]}"; do
    kubectl apply -f "k8s/${service}.yaml"
    log_info "Restarting and waiting for ${service} rollout..."
    kubectl -n "${NAMESPACE}" rollout restart "deployment/${service}"
    kubectl -n "${NAMESPACE}" rollout status "deployment/${service}" --timeout="${MAX_WAIT}s"
    log_ok "Deployment ready: ${service}"
done

# Deploy frontend
kubectl apply -f k8s/frontend.yaml
log_info "Restarting and waiting for frontend rollout..."
kubectl -n "${NAMESPACE}" rollout restart "deployment/frontend"
kubectl -n "${NAMESPACE}" rollout status "deployment/frontend" --timeout="${MAX_WAIT}s"
log_ok "Deployment ready: frontend"

# ---- Finished ----
echo ""
log_info "=========================================="
log_ok   "All-in-One Deployment Finished!"
log_info "=========================================="
echo "Frontend URL: http://localhost:30080"
echo "Gateway URL:  http://localhost:30088"
echo "Nacos URL:    http://localhost:${NACOS_NODE_PORT}/nacos"
echo "Status:       kubectl -n ${NAMESPACE} get pods"
echo ""
log_info "---------- Optional Enhancements ----------"
echo "PDB (Pod Disruption Budget):"
echo "  kubectl apply -f k8s/pdb.yaml"
echo ""
echo "HPA (Horizontal Pod Autoscaler, requires Metrics Server):"
echo "  kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml"
echo "  kubectl apply -f k8s/hpa.yaml"
echo ""
echo "Network Policy (requires Calico or similar CNI):"
echo "  kubectl apply -f k8s/network-policy.yaml"
echo ""
