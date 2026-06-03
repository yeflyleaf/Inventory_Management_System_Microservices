# ==================================================
# Kubernetes one-click deploy script for Docker Desktop
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\deploy-k8s.ps1
# ==================================================

$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $false

# Ensure working directory is the script's own directory
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Definition
Push-Location $SCRIPT_DIR
[Environment]::CurrentDirectory = $SCRIPT_DIR

$NAMESPACE = "inventory-system"
$KUBE_CONTEXT = "docker-desktop"
$NACOS_NODE_PORT = 30848
$NACOS_PUBLISH_MAX_RETRIES = 30
$NACOS_PUBLISH_RETRY_INTERVAL = 5
$SERVICES = @("user-service", "product-service", "order-service", "gateway-service")
$MAX_WAIT = 300

function Log-Info ($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Log-Ok   ($msg) { Write-Host "[OK]   $msg" -ForegroundColor Green }
function Log-Warn ($msg) { Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Log-Fail ($msg) { Write-Host "[FAIL] $msg" -ForegroundColor Red; exit 1 }

function Test-Env ($cmd) {
    if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) {
        Log-Fail "Command not found: $cmd"
    }
}

function Invoke-Native ($label, [scriptblock] $command) {
    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        & $command
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }

    if ($exitCode -ne 0) {
        Log-Fail "$label failed."
    }
}

function Test-DockerReady {
    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        docker info 1>$null 2>$null
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }

    if ($exitCode -ne 0) {
        Log-Fail "Docker Desktop daemon is not reachable. Start Docker Desktop and wait until it is running."
    }
}

function Test-NoRunningMysqlContainer {
    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $containers = @(docker ps --format "{{.Image}}`t{{.Names}}" 2>$null)
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }

    if ($exitCode -ne 0) {
        Log-Fail "Unable to inspect running Docker containers."
    }

    $databaseContainers = @($containers | Where-Object {
        $_ -match "(?i)(^|[/\s`t])(mysql|mariadb|percona)([-_:./@\s`t]|$)"
    })

    if ($databaseContainers.Count -gt 0) {
        Log-Fail "MySQL must run on the host, not in Docker. Stop these database containers first: $($databaseContainers -join '; ')"
    }

    Log-Ok "No running MySQL/MariaDB/Percona containers detected."
}

function Test-KubernetesReady {
    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $contexts = @(kubectl config get-contexts -o name 2>$null)
        $currentContext = kubectl config current-context 2>$null
        $currentContextExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }

    if ($currentContextExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($currentContext)) {
        if ($contexts -contains $KUBE_CONTEXT) {
            Log-Info "No kubectl context is selected. Switching to $KUBE_CONTEXT..."
            Invoke-Native "kubectl context switch" { kubectl config use-context $KUBE_CONTEXT | Out-Null }
        } else {
            Log-Fail "Kubernetes context is not set and '$KUBE_CONTEXT' does not exist. Enable Kubernetes in Docker Desktop first."
        }
    } elseif ($currentContext.Trim() -ne $KUBE_CONTEXT -and $contexts -contains $KUBE_CONTEXT) {
        Log-Info "Switching kubectl context from '$($currentContext.Trim())' to $KUBE_CONTEXT..."
        Invoke-Native "kubectl context switch" { kubectl config use-context $KUBE_CONTEXT | Out-Null }
    }

    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        kubectl cluster-info 1>$null 2>$null
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }

    if ($exitCode -ne 0) {
        Log-Fail "Kubernetes API is not reachable. In Docker Desktop, enable Kubernetes and wait until it is fully running."
    }
}

function Test-NoEmbeddedMysqlManifests {
    $manifestFiles = @()
    if (Test-Path "microservices-k8s.yaml") {
        $manifestFiles += Get-Item "microservices-k8s.yaml"
    }
    if (Test-Path "k8s") {
        $manifestFiles += Get-ChildItem "k8s" -Filter "*.yaml" -File
    }

    foreach ($file in $manifestFiles) {
        $content = Get-Content $file.FullName -Raw
        $violations = @()

        if ($content -match "(?im)^\s*image:\s*[""']?(mysql|mariadb|percona)([:/@\s]|$)") {
            $violations += "database container image"
        }
        if ($content -match "(?im)^\s*name:\s*mysql-pvc\s*$") {
            $violations += "MySQL persistent volume claim"
        }
        if ($content -match "(?ims)kind:\s*(Deployment|StatefulSet)\s+.*?name:\s*mysql\s*$") {
            $violations += "MySQL workload"
        }

        if ($violations.Count -gt 0) {
            Log-Fail "MySQL must stay on the host. Forbidden Kubernetes resource found in $($file.FullName): $($violations -join ', ')."
        }
    }

    Log-Ok "Kubernetes manifests contain no embedded MySQL workload."
}

function Test-NoExistingKubernetesMysqlResources {
    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        kubectl get namespace $NAMESPACE 1>$null 2>$null
        $namespaceExists = ($LASTEXITCODE -eq 0)
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }

    if (-not $namespaceExists) {
        return
    }

    $checks = @(
        @{ Resource = "deployment"; Name = "mysql" },
        @{ Resource = "statefulset"; Name = "mysql" },
        @{ Resource = "pvc"; Name = "mysql-pvc" }
    )
    $existing = @()

    foreach ($check in $checks) {
        $previousErrorAction = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        try {
            $found = kubectl -n $NAMESPACE get $check.Resource $check.Name -o name --ignore-not-found 2>$null
        } finally {
            $ErrorActionPreference = $previousErrorAction
        }

        if (-not [string]::IsNullOrWhiteSpace($found)) {
            $existing += $found.Trim()
        }
    }

    if ($existing.Count -gt 0) {
        Log-Fail "Existing MySQL resources were found in Kubernetes: $($existing -join ', '). Delete them before deploying because MySQL must stay on the host."
    }

    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $mysqlServiceJson = kubectl -n $NAMESPACE get service mysql-service -o json 2>$null
        $serviceExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }

    if ($serviceExitCode -eq 0 -and -not [string]::IsNullOrWhiteSpace($mysqlServiceJson)) {
        $mysqlService = $mysqlServiceJson | ConvertFrom-Json
        if ($mysqlService.spec.type -ne "ExternalName" -or $mysqlService.spec.externalName -ne "host.docker.internal") {
            Log-Fail "Existing mysql-service is not an ExternalName to host.docker.internal. Delete it before deploying: kubectl -n $NAMESPACE delete service mysql-service"
        }
    }

    Log-Ok "No existing Kubernetes MySQL workload or PVC detected."
}

function Wait-PodReady ($appName) {
    Log-Info "Waiting for $appName to be ready..."
    kubectl -n $NAMESPACE wait --for=condition=ready pod -l app=$appName --timeout="${MAX_WAIT}s"
    if ($LASTEXITCODE -ne 0) {
        kubectl -n $NAMESPACE get pods -l app=$appName
        Log-Fail "$appName did not become ready. Check logs with: kubectl -n $NAMESPACE logs -l app=$appName"
    }
    Log-Ok "$appName is ready."
}

function Publish-NacosCommonConfig {
    $configPath = Join-Path (Get-Location) "nacos-common-config.yaml"
    if (-not (Test-Path $configPath)) {
        Log-Fail "Missing Nacos config file: $configPath"
    }

    $uri = "http://localhost:$NACOS_NODE_PORT/nacos/v1/cs/configs"
    $content = Get-Content $configPath -Raw
    $body = @{
        dataId = "common.yaml"
        group = "DEFAULT_GROUP"
        content = $content
    }

    Log-Info "Publishing common.yaml to Nacos via $uri..."
    $success = $false
    for ($i = 1; $i -le $NACOS_PUBLISH_MAX_RETRIES; $i++) {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Method Post -Uri $uri -Body $body -ContentType "application/x-www-form-urlencoded" -TimeoutSec 30
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300 -and $response.Content.Trim() -eq "true") {
                $success = $true
                break
            }
        } catch {
            # Nacos API may not be ready yet
        }
        Log-Info "Waiting for Nacos API to accept configuration ($i/$NACOS_PUBLISH_MAX_RETRIES)..."
        Start-Sleep -Seconds $NACOS_PUBLISH_RETRY_INTERVAL
    }

    if (-not $success) {
        Log-Fail "Failed to publish Nacos config after $NACOS_PUBLISH_MAX_RETRIES retries. Ensure nacos-service NodePort $NACOS_NODE_PORT is reachable."
    }
    Log-Ok "Nacos common.yaml published."
}

function Test-HostMysqlPort {
    if (Get-Command Test-NetConnection -ErrorAction SilentlyContinue) {
        $reachable = Test-NetConnection -ComputerName "127.0.0.1" -Port 3306 -InformationLevel Quiet
        if (-not $reachable) {
            Log-Warn "Host MySQL port 3306 is not reachable from Windows. Nacos may fail if your MySQL is not running or not bound to 0.0.0.0/localhost."
        }
    }
}

function Test-HostMysqlFromKubernetes {
    $podName = "mysql-host-check"

    Log-Info "Testing host MySQL connectivity from inside Kubernetes via mysql-service:3306..."
    kubectl -n $NAMESPACE delete pod $podName --ignore-not-found 1>$null 2>$null

    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        kubectl -n $NAMESPACE run $podName `
            --image=busybox:1.36 `
            --restart=Never `
            --rm `
            -i `
            --attach `
            --pod-running-timeout=120s `
            --command -- sh -c "nc -zvw5 mysql-service 3306"
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
        kubectl -n $NAMESPACE delete pod $podName --ignore-not-found 1>$null 2>$null
    }

    if ($exitCode -ne 0) {
        Log-Fail "Kubernetes pods cannot reach host MySQL at mysql-service:3306. Check that MySQL is running on the host, listening on 3306, allowed through Windows Firewall, and reachable via host.docker.internal."
    }

    Log-Ok "Kubernetes can reach host MySQL through mysql-service:3306."
}

function Restart-And-WaitDeployment ($deploymentName) {
    Invoke-Native "rollout restart $deploymentName" { kubectl -n $NAMESPACE rollout restart "deployment/$deploymentName" }
    Invoke-Native "rollout status $deploymentName" { kubectl -n $NAMESPACE rollout status "deployment/$deploymentName" --timeout="${MAX_WAIT}s" }
}

# ---- Pre-check ----
Log-Info "Checking environment..."
Test-Env "docker"
Test-Env "kubectl"
Test-Env "java"
Test-NoEmbeddedMysqlManifests
Test-DockerReady
Test-NoRunningMysqlContainer
Test-KubernetesReady
Test-NoExistingKubernetesMysqlResources
Test-HostMysqlPort
Log-Ok "Environment check passed."

# ---- Step 1: Maven build ----
Log-Info "Step 1/7: Maven building JARs..."
if (Test-Path "./mvnw.cmd") {
    Invoke-Native "Maven build" { & ./mvnw.cmd clean package -DskipTests -q }
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    Invoke-Native "Maven build" { mvn clean package -DskipTests -q }
} else {
    Log-Fail "Maven not found (mvnw.cmd or mvn)."
}
Log-Ok "Maven build success."

# ---- Step 2: Pre-pull base images ----
Log-Info "Step 2/7: Ensuring base images are available locally..."
$BASE_IMAGE = "eclipse-temurin:17-jre-alpine"
$MIRRORS = @(
    "eclipse-temurin:17-jre-alpine",
    "docker.m.daocloud.io/eclipse-temurin:17-jre-alpine",
    "registry.cn-hangzhou.aliyuncs.com/acs/eclipse-temurin:17-jre-alpine"
)

$baseImageReady = $false
# Check if image already exists locally
$previousErrorAction = $ErrorActionPreference
$ErrorActionPreference = "Continue"
try {
    docker image inspect $BASE_IMAGE 1>$null 2>$null
    $baseImageReady = ($LASTEXITCODE -eq 0)
} finally {
    $ErrorActionPreference = $previousErrorAction
}

if ($baseImageReady) {
    Log-Ok "Base image $BASE_IMAGE already cached locally."
} else {
    Log-Info "Base image not found locally, attempting to pull..."
    foreach ($mirror in $MIRRORS) {
        Log-Info "Trying: $mirror ..."
        $previousErrorAction = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        try {
            docker pull $mirror 2>$null
            $pullExitCode = $LASTEXITCODE
        } finally {
            $ErrorActionPreference = $previousErrorAction
        }

        if ($pullExitCode -eq 0) {
            if ($mirror -ne $BASE_IMAGE) {
                Log-Info "Tagging $mirror as $BASE_IMAGE"
                docker tag $mirror $BASE_IMAGE
            }
            $baseImageReady = $true
            Log-Ok "Base image $BASE_IMAGE is ready."
            break
        }
        Log-Warn "Failed to pull from $mirror, trying next mirror..."
    }

    if (-not $baseImageReady) {
        Log-Fail "Unable to pull base image $BASE_IMAGE from any mirror. Check your network or Docker Desktop proxy settings."
    }
}

# ---- Step 3: Docker build for backend ----
Log-Info "Step 3/7: Building backend Docker images..."
foreach ($service in $SERVICES) {
    $targetDir = Join-Path $SCRIPT_DIR "$service/target"
    $jarFiles = @(Get-ChildItem -Path $targetDir -Filter "*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch '(sources|original)' })
    if ($jarFiles.Count -eq 0) {
        Log-Fail "JAR file not found for $service (searched in $targetDir)"
    }
    
    $jarPath = "$service/target/$($jarFiles[0].Name)"

    Log-Info "Building image: ${service}:1.0"
    Invoke-Native "Docker build $service" { docker build --build-arg JAR_FILE="$jarPath" -t "${service}:1.0" . }
    Log-Ok "Image ${service}:1.0 ready."
}

# ---- Step 4: Docker build for frontend ----
Log-Info "Step 4/7: Building frontend Docker image..."
Log-Info "Building image: frontend:1.0"
Invoke-Native "Frontend Docker build" { docker build -t "frontend:1.0" ../Frontend }
Log-Ok "Image frontend:1.0 ready."

# ---- Step 5: Apply base Kubernetes resources ----
Log-Info "Step 5/7: Applying Kubernetes base resources..."
Invoke-Native "apply namespace" { kubectl apply -f k8s/namespace.yaml }
Invoke-Native "apply configmap" { kubectl apply -f k8s/configmap.yaml }
Invoke-Native "apply middleware" { kubectl apply -f k8s/middleware.yaml }
Log-Ok "Base resources applied."
Test-HostMysqlFromKubernetes

# ---- Step 6: Wait for middleware and publish Nacos config ----
Log-Info "Step 6/7: Waiting for middleware..."
Wait-PodReady "nacos"
Wait-PodReady "redis"
Wait-PodReady "seata"
Wait-PodReady "sentinel"
Publish-NacosCommonConfig

# ---- Step 7: Deploy services ----
Log-Info "Step 7/7: Deploying business services and frontend..."
foreach ($service in $SERVICES) {
    Invoke-Native "apply $service" { kubectl apply -f "k8s/$service.yaml" }
    Restart-And-WaitDeployment $service
    Log-Ok "Deployment ready: $service"
}

Invoke-Native "apply frontend" { kubectl apply -f "k8s/frontend.yaml" }
Restart-And-WaitDeployment "frontend"
Log-Ok "Deployment ready: frontend"

# ---- Finished ----
Write-Host ""
Log-Info "=========================================="
Log-Ok   "All-in-One deployment finished."
Log-Info "=========================================="
Write-Host "Frontend URL: http://localhost:30080"
Write-Host "Gateway URL:  http://localhost:30088"
Write-Host "Nacos URL:    http://localhost:$NACOS_NODE_PORT/nacos"
Write-Host "Status:       kubectl -n $NAMESPACE get pods"
Write-Host ""
Log-Info "---------- Optional Enhancements ----------"
Write-Host "PDB (Pod Disruption Budget):"
Write-Host "  kubectl apply -f k8s/pdb.yaml"
Write-Host ""
Write-Host "HPA (Horizontal Pod Autoscaler, requires Metrics Server):"
Write-Host "  kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml"
Write-Host "  kubectl apply -f k8s/hpa.yaml"
Write-Host ""
Write-Host "Network Policy (requires Calico or similar CNI):"
Write-Host "  kubectl apply -f k8s/network-policy.yaml"
Write-Host ""

Pop-Location
