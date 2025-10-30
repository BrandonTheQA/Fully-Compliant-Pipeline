#!/usr/bin/env bash
set -euo pipefail

if [[ ${1:-} == "" ]]; then
  echo "Usage: $0 <registry> [tag]"
  echo "Example: $0 myregistry.azurecr.io v1.2.3"
  exit 1
fi

REGISTRY=$1
TAG=${2:-$(git rev-parse --short HEAD)}

SERVICES=(user product order)

for SERVICE in "${SERVICES[@]}"; do
  CONTEXT_DIR="api/functionapp/${SERVICE}"
  IMAGE="${REGISTRY}/${SERVICE}-service:${TAG}"

  echo "Building ${IMAGE}"
  docker build "${CONTEXT_DIR}" \
    -f "${CONTEXT_DIR}/Dockerfile" \
    -t "${IMAGE}"

  echo "Pushing ${IMAGE}"
  docker push "${IMAGE}"
done

echo "All images pushed to ${REGISTRY} with tag ${TAG}."
