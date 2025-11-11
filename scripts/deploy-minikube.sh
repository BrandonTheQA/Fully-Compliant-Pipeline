#!/usr/bin/env bash
set -euo pipefail

# Deploys all services and UI to minikube and runs Postman tests
# Usage: ./scripts/deploy-minikube.sh [--cleanup]

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

NAMESPACE="dev"
CLEANUP=false

# Parse arguments
if [[ "${1:-}" == "--cleanup" ]]; then
  CLEANUP=true
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
  echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
  echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
  echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
  log_info "Checking prerequisites..."
  
  local missing=()
  
  if ! command -v minikube >/dev/null 2>&1; then
    missing+=("minikube")
  fi
  
  if ! command -v kubectl >/dev/null 2>&1; then
    missing+=("kubectl")
  fi
  
  if ! command -v docker >/dev/null 2>&1; then
    missing+=("docker")
  fi
  
  if ! command -v newman >/dev/null 2>&1; then
    if command -v npm >/dev/null 2>&1; then
      log_info "Installing newman..."
      npm install -g newman >/dev/null 2>&1 || {
        log_error "Failed to install newman. Please install it manually: npm install -g newman"
        exit 1
      }
    else
      missing+=("newman (or npm to install it)")
    fi
  fi
  
  if [ ${#missing[@]} -ne 0 ]; then
    log_error "Missing prerequisites: ${missing[*]}"
    exit 1
  fi
  
  # Check if minikube is running
  if ! minikube status >/dev/null 2>&1; then
    log_error "Minikube is not running. Please start it with: minikube start"
    exit 1
  fi
  
  log_info "All prerequisites met"
}

# Build Docker images
build_images() {
  log_info "Building Docker images..."
  
  # Set docker environment to use minikube's docker daemon
  eval $(minikube docker-env)
  
  # Build ecompoc service
  log_info "Building ecompoc service..."
  docker build -t ecompoc:local "$ROOT_DIR/api/services/ecompoc" >/dev/null 2>&1 || {
    log_error "Failed to build ecompoc service"
    exit 1
  }
  
  # Build UI
  log_info "Building UI..."
  docker build -t ui:local "$ROOT_DIR/ui" >/dev/null 2>&1 || {
    log_error "Failed to build UI"
    exit 1
  }
  
  log_info "All images built successfully"
}

# Ensure namespace exists
ensure_namespace() {
  log_info "Ensuring namespace $NAMESPACE exists..."
  
  if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
    kubectl create namespace "$NAMESPACE"
    log_info "Created namespace $NAMESPACE"
  else
    log_info "Namespace $NAMESPACE already exists"
  fi
}

# Create dummy secret for SQL_SERVER_PASSWORD (not used with H2, but required by deployment)
create_dummy_secret() {
  log_info "Creating dummy SQL secret (not used with H2 database)..."
  
  if ! kubectl get secret sql-db-secret -n "$NAMESPACE" >/dev/null 2>&1; then
    kubectl create secret generic sql-db-secret \
      --from-literal=password=dummy \
      -n "$NAMESPACE" >/dev/null 2>&1 || {
      log_error "Failed to create dummy secret"
      exit 1
    }
    log_info "Created dummy SQL secret"
  else
    log_info "SQL secret already exists"
  fi
}

# Deploy to minikube
deploy() {
  log_info "Deploying services to minikube..."
  
  kubectl apply -k "$ROOT_DIR/k8s/overlays/minikube" || {
    log_error "Failed to deploy services"
    exit 1
  }
  
  log_info "Services deployed successfully"
}

# Wait for deployments to be ready
wait_for_deployments() {
  log_info "Waiting for deployments to be ready..."
  
  local deployments=("ecompoc" "ui")
  local timeout=300  # 5 minutes
  
  for deployment in "${deployments[@]}"; do
    log_info "Waiting for $deployment deployment..."
    if kubectl wait --for=condition=available --timeout=${timeout}s deployment/$deployment -n "$NAMESPACE" >/dev/null 2>&1; then
      log_info "$deployment is ready"
    else
      log_error "$deployment failed to become ready"
      log_error "Pod logs:"
      kubectl logs -l app=$deployment -n "$NAMESPACE" --tail=50 || true
      exit 1
    fi
  done
  
  log_info "All deployments are ready"
}

# Get service URLs
get_service_urls() {
  log_info "Getting service URLs..."
  
  log_info "Starting port-forwarding for ecompoc service..."
  cleanup_port_forwards
  
  kubectl port-forward -n "$NAMESPACE" svc/ecompoc 8080:80 >/dev/null 2>&1 &
  local ecompoc_pf=$!
  
  sleep 3
  
  if ! kill -0 $ecompoc_pf 2>/dev/null; then
    log_error "Port-forwarding failed to establish"
    cleanup_port_forwards
    exit 1
  fi
  
  ECOMPOC_URL="http://localhost:8080"
  echo $ecompoc_pf > /tmp/minikube-pf-ecompoc.pid
  log_info "eComPOC service URL: $ECOMPOC_URL"
  export ECOMPOC_URL
}

# Run Postman tests
run_tests() {
  log_info "Running Postman tests..."
  
  local collection="$ROOT_DIR/postman/IntegrationTest.postman_collection.json"
  
  if [ ! -f "$collection" ]; then
    log_error "Postman collection not found: $collection"
    exit 1
  fi
  
  # Generate unique email to avoid conflicts
  local random_suffix=$(date +%s)
  local user_email="john.doe+${random_suffix}@example.com"
  
  newman run "$collection" \
    --env-var "apiBaseUrl=$ECOMPOC_URL/api" \
    --env-var "userName=John Doe" \
    --env-var "userEmail=$user_email" \
    --env-var "userPassword=SecurePassword123" \
    --env-var "product1Name=Laptop" \
    --env-var "product1Description=High-performance laptop" \
    --env-var "product1Price=999.99" \
    --env-var "product1Quantity=10" \
    --env-var "product1Category=Electronics" \
    --env-var "product1OrderQuantity=1" \
    --env-var "product2Name=Mouse" \
    --env-var "product2Description=Wireless mouse" \
    --env-var "product2Price=29.99" \
    --env-var "product2Quantity=50" \
    --env-var "product2Category=Electronics" \
    --env-var "product2OrderQuantity=2" \
    --env-var "product3Name=Keyboard" \
    --env-var "product3Description=Mechanical keyboard" \
    --env-var "product3Price=79.99" \
    --env-var "product3Quantity=25" \
    --env-var "product3Category=Electronics" \
    --env-var "product3OrderQuantity=1" \
    --color on || {
    log_error "Postman tests failed"
    cleanup_port_forwards
    exit 1
  }
  
  log_info "All Postman tests passed!"
}

# Cleanup port-forwards
cleanup_port_forwards() {
  if [ -f /tmp/minikube-pf-ecompoc.pid ]; then
    kill $(cat /tmp/minikube-pf-ecompoc.pid) 2>/dev/null || true
    rm -f /tmp/minikube-pf-ecompoc.pid
  fi
}

# Cleanup deployments
cleanup_deployments() {
  log_info "Cleaning up deployments..."
  kubectl delete -k "$ROOT_DIR/k8s/overlays/minikube" --ignore-not-found=true || true
  cleanup_port_forwards
  log_info "Cleanup complete"
}

# Main execution
main() {
  log_info "Starting minikube deployment and testing..."
  
  if [ "$CLEANUP" = true ]; then
    cleanup_deployments
    exit 0
  fi
  
  check_prerequisites
  build_images
  ensure_namespace
  create_dummy_secret
  deploy
  wait_for_deployments
  get_service_urls
  
  # Wait a bit more for services to be fully ready
  sleep 5
  
  run_tests
  
  cleanup_port_forwards
  
  log_info "Deployment and testing completed successfully!"
  log_info "Services are running in minikube. To clean up, run: $0 --cleanup"
}

# Trap to cleanup on exit
trap cleanup_port_forwards EXIT

main "$@"

