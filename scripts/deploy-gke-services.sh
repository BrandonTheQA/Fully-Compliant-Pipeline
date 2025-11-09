#!/bin/bash
set -euo pipefail

# GKE Deployment Script
# This script deploys all services as green deployment to the GKE cluster

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

# Check required environment variables
if [ -z "${GCP_PROJECT:-}" ]; then
    GCP_PROJECT=$(gcloud config get-value project 2>/dev/null || echo "")
    if [ -z "$GCP_PROJECT" ]; then
        log_error "GCP_PROJECT is not set"
        exit 1
    fi
fi

if [ -z "${GKE_CLUSTER_NAME:-}" ]; then
    log_error "GKE_CLUSTER_NAME is not set"
    exit 1
fi

if [ -z "${GKE_LOCATION:-}" ]; then
    log_error "GKE_LOCATION is not set"
    exit 1
fi

if [ -z "${GCR_REGISTRY:-}" ]; then
    log_error "GCR_REGISTRY is not set"
    exit 1
fi

if [ -z "${SQL_SERVER_PASSWORD:-}" ]; then
    log_error "SQL_SERVER_PASSWORD is not set"
    log_info "Please set SQL_SERVER_PASSWORD environment variable"
    exit 1
fi

# Configuration
NAMESPACE="dev"
TARGET_COLOR="green"
ACTIVE_COLOR="blue"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

log_info "Deployment Configuration:"
log_info "  Project: $GCP_PROJECT"
log_info "  Cluster: $GKE_CLUSTER_NAME"
log_info "  Location: $GKE_LOCATION"
log_info "  Registry: $GCR_REGISTRY"
log_info "  Namespace: $NAMESPACE"
log_info "  Target Color: $TARGET_COLOR"

# Check kubectl access
if ! kubectl cluster-info &>/dev/null; then
    log_info "Configuring kubectl for GKE cluster..."
    gcloud container clusters get-credentials "$GKE_CLUSTER_NAME" \
        --location="$GKE_LOCATION" \
        --project="$GCP_PROJECT"
fi

# Ensure namespace exists
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

# Step 1: Create deployment-state ConfigMap
log_info "Step 1: Creating deployment-state ConfigMap..."
kubectl create configmap deployment-state \
    --from-literal=active-color="$ACTIVE_COLOR" \
    -n "$NAMESPACE" \
    --dry-run=client -o yaml | kubectl apply -f -
log_info "ConfigMap created with active-color=$ACTIVE_COLOR (green will deploy first)"

# Step 2: Create SQL database secret
log_info "Step 2: Creating SQL database secret..."
kubectl create secret generic sql-db-secret \
    --from-literal=password="$SQL_SERVER_PASSWORD" \
    --namespace="$NAMESPACE" \
    --dry-run=client -o yaml | kubectl apply -f -
log_info "SQL database secret created"

# Function to deploy a service
deploy_service() {
    local SERVICE_NAME=$1
    local DEPLOYMENT_FILE="$PROJECT_ROOT/k8s/$SERVICE_NAME/deployment.yaml"
    local SERVICE_FILE="$PROJECT_ROOT/k8s/$SERVICE_NAME/service.yaml"
    
    if [ ! -f "$DEPLOYMENT_FILE" ] || [ ! -f "$SERVICE_FILE" ]; then
        log_error "Deployment or service file not found for $SERVICE_NAME"
        return 1
    fi
    
    log_info "Deploying $SERVICE_NAME-$TARGET_COLOR..."
    
    # Create temporary directory for modified manifests
    local TMPDIR=$(mktemp -d)
    trap "rm -rf $TMPDIR" EXIT
    
    # Copy files to temp directory
    cp "$DEPLOYMENT_FILE" "$TMPDIR/deployment.yaml"
    cp "$SERVICE_FILE" "$TMPDIR/service.yaml"
    
    # Replace ACR image references with GCR
    # Use a placeholder commit SHA for now (CI/CD will use actual SHA)
    local IMAGE_TAG="${IMAGE_TAG:-latest}"
    sed -i.bak "s|rgpocacr9021.azurecr.io/$SERVICE_NAME:latest|$GCR_REGISTRY/$SERVICE_NAME:$IMAGE_TAG|g" "$TMPDIR/deployment.yaml"
    rm -f "$TMPDIR/deployment.yaml.bak"
    
    # Remove Azure-specific node affinity (GKE doesn't need it)
    # Use yq if available, otherwise use sed
    if command -v yq &> /dev/null; then
        # Remove the entire affinity section if it exists
        yq eval 'del(.spec.template.spec.affinity)' -i "$TMPDIR/deployment.yaml" || true
    else
        # Fallback: remove nodeAffinity section manually
        sed -i.bak '/nodeAffinity:/,/values:/d' "$TMPDIR/deployment.yaml" || true
        rm -f "$TMPDIR/deployment.yaml.bak"
    fi
    
    # Remove tolerations that are Azure-specific (keep CriticalAddonsOnly if needed)
    if command -v yq &> /dev/null; then
        # Remove Azure-specific tolerations
        yq eval 'del(.spec.template.spec.tolerations[] | select(.key == "kubernetes.azure.com/scalesetpriority"))' -i "$TMPDIR/deployment.yaml" || true
    fi
    
    # Add deployment-color label to deployment selector and pod labels
    # Install yq if not available
    if ! command -v yq &> /dev/null; then
        log_info "Installing yq for YAML manipulation..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            if command -v brew &> /dev/null; then
                brew install yq 2>/dev/null || {
                    log_warn "Could not install yq via brew, trying manual installation"
                    curl -L https://github.com/mikefarah/yq/releases/latest/download/yq_darwin_amd64 -o /tmp/yq
                    chmod +x /tmp/yq
                    sudo mv /tmp/yq /usr/local/bin/yq
                }
            else
                log_warn "Homebrew not found, trying manual installation"
                curl -L https://github.com/mikefarah/yq/releases/latest/download/yq_darwin_amd64 -o /tmp/yq
                chmod +x /tmp/yq
                sudo mv /tmp/yq /usr/local/bin/yq
            fi
        else
            wget -qO /tmp/yq https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64
            chmod +x /tmp/yq
            sudo mv /tmp/yq /usr/local/bin/yq
        fi
    fi
    
    if command -v yq &> /dev/null; then
        yq eval ".spec.selector.matchLabels.deployment-color = \"$TARGET_COLOR\"" -i "$TMPDIR/deployment.yaml"
        yq eval ".spec.template.metadata.labels.deployment-color = \"$TARGET_COLOR\"" -i "$TMPDIR/deployment.yaml"
        yq eval ".spec.selector.deployment-color = \"$TARGET_COLOR\"" -i "$TMPDIR/service.yaml"
    else
        log_error "yq is required but could not be installed"
        exit 1
    fi
    
    # Create kustomization.yaml with nameSuffix
    cat > "$TMPDIR/kustomization.yaml" << EOF
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
nameSuffix: -$TARGET_COLOR
namespace: $NAMESPACE
resources:
  - deployment.yaml
  - service.yaml
EOF
    
    # Apply kustomization
    kubectl apply -k "$TMPDIR"
    
    # Set environment variables based on service
    if [ "$SERVICE_NAME" = "order" ]; then
        log_info "Setting environment variables for order service..."
        kubectl set env deployment/order-${TARGET_COLOR} \
            DEPLOYMENT_COLOR=${TARGET_COLOR} \
            NAMESPACE=${NAMESPACE} \
            USER_SERVICE_URL=http://user-${TARGET_COLOR}.${NAMESPACE}.svc.cluster.local \
            PRODUCT_SERVICE_URL=http://product-${TARGET_COLOR}.${NAMESPACE}.svc.cluster.local \
            -n "$NAMESPACE"
    else
        kubectl set env deployment/${SERVICE_NAME}-${TARGET_COLOR} \
            DEPLOYMENT_COLOR=${TARGET_COLOR} \
            NAMESPACE=${NAMESPACE} \
            -n "$NAMESPACE"
    fi
    
    # Annotate deployment and service
    kubectl annotate deployment/$SERVICE_NAME-${TARGET_COLOR} \
        app.kubernetes.io/instance=$SERVICE_NAME-${TARGET_COLOR} \
        -n "$NAMESPACE" --overwrite || true
    
    kubectl annotate service/$SERVICE_NAME-${TARGET_COLOR} \
        app.kubernetes.io/instance=$SERVICE_NAME-${TARGET_COLOR} \
        -n "$NAMESPACE" --overwrite || true
    
    log_info "$SERVICE_NAME-$TARGET_COLOR deployed"
}

# Step 3: Deploy services in order
log_info "Step 3: Deploying services..."

# Deploy user first (dependency for order)
deploy_service "user"

# Deploy product
deploy_service "product"

# Deploy order (depends on user and product)
deploy_service "order"

# Deploy UI
deploy_service "ui"

# Step 4: Wait for rollouts
log_info "Step 4: Waiting for deployments to be ready..."
for SERVICE in user product order ui; do
    log_info "Waiting for $SERVICE-$TARGET_COLOR rollout..."
    kubectl rollout status deployment/$SERVICE-${TARGET_COLOR} -n "$NAMESPACE" --timeout=300s || {
        log_error "Rollout failed for $SERVICE-$TARGET_COLOR"
        kubectl describe deployment/$SERVICE-${TARGET_COLOR} -n "$NAMESPACE"
        kubectl logs -l app=$SERVICE,deployment-color=$TARGET_COLOR -n "$NAMESPACE" --tail=50
        exit 1
    }
done

log_info "All deployments are ready!"

# Step 5: Verify services
log_info "Step 5: Verifying services..."

# Check deployments
log_info "Checking deployments..."
kubectl get deployments -n "$NAMESPACE" | grep -- "-${TARGET_COLOR}$" || {
    log_error "Some deployments are missing"
    exit 1
}

# Check pods
log_info "Checking pods..."
kubectl get pods -n "$NAMESPACE" -l deployment-color=$TARGET_COLOR

# Check services
log_info "Checking services..."
kubectl get svc -n "$NAMESPACE" | grep -- "-${TARGET_COLOR}$"

# Wait for LoadBalancer IPs
log_info "Waiting for LoadBalancer external IPs (this may take a few minutes)..."
for SERVICE in user product order ui; do
    log_info "Waiting for $SERVICE-$TARGET_COLOR LoadBalancer IP..."
    for i in {1..60}; do
        EXTERNAL_IP=$(kubectl get svc ${SERVICE}-${TARGET_COLOR} -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
        if [ -z "$EXTERNAL_IP" ]; then
            EXTERNAL_HOSTNAME=$(kubectl get svc ${SERVICE}-${TARGET_COLOR} -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
        else
            EXTERNAL_HOSTNAME=""
        fi
        EXTERNAL_HOST=${EXTERNAL_IP:-$EXTERNAL_HOSTNAME}
        if [ -n "$EXTERNAL_HOST" ]; then
            log_info "$SERVICE-$TARGET_COLOR external endpoint: $EXTERNAL_HOST"
            break
        fi
        sleep 5
    done
    if [ -z "$EXTERNAL_HOST" ]; then
        log_warn "Timed out waiting for external IP for $SERVICE-$TARGET_COLOR"
    fi
done

# Health checks
log_info "Performing health checks..."
for SERVICE in user product order; do
    EXTERNAL_IP=$(kubectl get svc ${SERVICE}-${TARGET_COLOR} -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
    if [ -z "$EXTERNAL_IP" ]; then
        EXTERNAL_HOSTNAME=$(kubectl get svc ${SERVICE}-${TARGET_COLOR} -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
    else
        EXTERNAL_HOSTNAME=""
    fi
    EXTERNAL_HOST=${EXTERNAL_IP:-$EXTERNAL_HOSTNAME}
    
    if [ -n "$EXTERNAL_HOST" ]; then
        log_info "Checking health for $SERVICE-$TARGET_COLOR at http://${EXTERNAL_HOST}/actuator/health..."
        for i in {1..30}; do
            HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://${EXTERNAL_HOST}/actuator/health" || echo "000")
            if [ "$HTTP_CODE" = "200" ]; then
                log_info "$SERVICE-$TARGET_COLOR is healthy (HTTP 200)"
                break
            fi
            if [ $i -eq 30 ]; then
                log_warn "$SERVICE-$TARGET_COLOR health check failed. Last code: $HTTP_CODE"
            else
                sleep 5
            fi
        done
    else
        log_warn "Skipping health check for $SERVICE-$TARGET_COLOR (no external IP)"
    fi
done

# UI health check
UI_EXTERNAL_IP=$(kubectl get svc ui-${TARGET_COLOR} -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
if [ -z "$UI_EXTERNAL_IP" ]; then
    UI_EXTERNAL_HOSTNAME=$(kubectl get svc ui-${TARGET_COLOR} -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
else
    UI_EXTERNAL_HOSTNAME=""
fi
UI_EXTERNAL_HOST=${UI_EXTERNAL_IP:-$UI_EXTERNAL_HOSTNAME}

if [ -n "$UI_EXTERNAL_HOST" ]; then
    log_info "Checking health for ui-$TARGET_COLOR at http://${UI_EXTERNAL_HOST}/health..."
    for i in {1..30}; do
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://${UI_EXTERNAL_HOST}/health" || echo "000")
        if [ "$HTTP_CODE" = "200" ]; then
            log_info "ui-$TARGET_COLOR is healthy (HTTP 200)"
            break
        fi
        if [ $i -eq 30 ]; then
            log_warn "ui-$TARGET_COLOR health check failed. Last code: $HTTP_CODE"
        else
            sleep 5
        fi
    done
else
    log_warn "Skipping health check for ui-$TARGET_COLOR (no external IP)"
fi

log_info ""
log_info "=========================================="
log_info "Deployment Complete!"
log_info "=========================================="
log_info ""
log_info "All services deployed as $TARGET_COLOR deployment"
log_info ""
log_info "Service endpoints:"
kubectl get svc -n "$NAMESPACE" -l deployment-color=$TARGET_COLOR -o custom-columns=NAME:.metadata.name,EXTERNAL-IP:.status.loadBalancer.ingress[0].ip,HOSTNAME:.status.loadBalancer.ingress[0].hostname
log_info ""

