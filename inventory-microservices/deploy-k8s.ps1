# ==================================================
# Kubernetes One-Click Deploy Script (Windows PowerShell)
# Usage: powershell -ExecutionPolicy Bypass -File .\deploy-k8s.ps1
# ==================================================

$ErrorActionPreference = "Stop"

$NAMESPACE = "inventory-system"
$SERVICES = @("user-service", "product-service", "order-service", "gateway-service")
$MAX_WAIT = 180

# ---- Functions ----
function Log-Info  ($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Log-Ok    ($msg) { Write-Host "[OK]   $msg" -ForegroundColor Green }
function Log-Fail  ($msg) { Write-Host "[FAIL] $msg" -ForegroundColor Red; exit 1 }

function Test-Env ($cmd) {
    if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) {
        Log-Fail "Command not found: $cmd"
    }
}

# ---- Pre-check ----
Log-Info "Checking environment..."
Test-Env "docker"
Test-Env "kubectl"
Test-Env "java"
Log-Ok "Environment check passed."

# ---- Step 1: Maven Build ----
Log-Info "Step 1/5: Maven building JARs..."
if (Test-Path "./mvnw.cmd") {
    & ./mvnw.cmd clean package -DskipTests -q
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn clean package -DskipTests -q
} else {
    Log-Fail "Maven not found (mvnw.cmd or mvn)."
}
Log-Ok "Maven build success."

# ---- Step 2: Docker Build (Backend) ----
Log-Info "Step 2/5: Building Backend Docker images..."
foreach ($service in $SERVICES) {
    $jarPath = "$service/target/*.jar"
    if (-not (Test-Path $jarPath)) {
        Log-Fail "JAR file not found for $service"
    }
    Log-Info "Building image: ${service}:1.0"
    # 使用通用的内联 Dockerfile 方式或指定后端 Dockerfile
    docker build --build-arg JAR_FILE="$jarPath" -t "${service}:1.0" . --quiet
    if ($LASTEXITCODE -ne 0) { Log-Fail "Docker build failed for $service" }
    Log-Ok "Image ${service}:1.0 ready."
}

# ---- Step 3: Docker Build (Frontend) ----
Log-Info "Step 3/5: Building Frontend Docker image..."
Log-Info "Building image: frontend:1.0"
docker build -t "frontend:1.0" ../Frontend --quiet
if ($LASTEXITCODE -ne 0) { Log-Fail "Frontend Docker build failed." }
Log-Ok "Image frontend:1.0 ready."

# ---- Step 4: K8s Deploy ----
Log-Info "Step 4/5: Applying K8s resources..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/middleware.yaml

# ---- Step 5: Wait and Deploy Services ----
Log-Info "Step 5/5: Waiting for middleware..."
$middlewareApps = @("nacos", "redis", "seata")

foreach ($app in $middlewareApps) {
    Log-Info "Waiting for $app to be ready..."
    kubectl -n $NAMESPACE wait --for=condition=ready pod -l app=$app --timeout="${MAX_WAIT}s" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Log-Ok "$app is ready."
    } else {
        Write-Host "[WARN] $app timeout, continuing anyway..." -ForegroundColor Yellow
    }
}

Log-Info "Deploying all services (Business + Frontend)..."
foreach ($service in $SERVICES) {
    kubectl apply -f "k8s/$service.yaml"
    Log-Ok "Applied: $service"
}

# 部署前端
kubectl apply -f "k8s/frontend.yaml"
Log-Ok "Applied: frontend"

# ---- Finished ----
Write-Host ""
Log-Info "=========================================="
Log-Ok   "All-in-One Deployment Finished!"
Log-Info "=========================================="
Write-Host "Frontend URL: http://localhost:30080"
Write-Host "Gateway URL:  http://localhost:30088"
Write-Host "Status:       kubectl -n $NAMESPACE get pods"
Write-Host ""
Write-Host "[WARN] Note: Ensure host MySQL is running and accessible." -ForegroundColor Yellow
