#!/usr/bin/env bash
set -euo pipefail

if [[ ${1:-} == "" ]]; then
  echo "Usage: $0 <registry> <tag> [namespace]"
  echo "Example: $0 myregistry.azurecr.io v1.2.3 retail-platform"
  exit 1
fi

REGISTRY=$1
TAG=${2:-latest}
NAMESPACE=${3:-retail-platform}

MANIFEST_DIR="api/functionapp/k8s"

echo "Applying Kubernetes manifests to namespace ${NAMESPACE}"
kubectl apply -f "${MANIFEST_DIR}/namespace.yaml"
kubectl apply -f "${MANIFEST_DIR}/user-service.yaml"
kubectl apply -f "${MANIFEST_DIR}/product-service.yaml"
kubectl apply -f "${MANIFEST_DIR}/order-service.yaml"

echo "Updating images to ${TAG} in registry ${REGISTRY}"
kubectl -n "${NAMESPACE}" set image deployment/user-service user-service="${REGISTRY}/user-service:${TAG}"
kubectl -n "${NAMESPACE}" set image deployment/product-service product-service="${REGISTRY}/product-service:${TAG}"
kubectl -n "${NAMESPACE}" set image deployment/order-service order-service="${REGISTRY}/order-service:${TAG}"

echo "Deployment triggered. Monitor rollout with: kubectl -n ${NAMESPACE} rollout status deployment/<service>"
